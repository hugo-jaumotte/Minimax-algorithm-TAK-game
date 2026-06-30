/*
 *  Copyright 2025 Belegkarnil
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 *  associated documentation files (the “Software”), to deal in the Software without restriction,
 *  including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *  and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do
 *  so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all copies or substantial
 *  portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 *  OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package be.belegkarnil.game.board.tak.strategy;

import be.belegkarnil.game.board.tak.Action;
import be.belegkarnil.game.board.tak.Board;
import be.belegkarnil.game.board.tak.Constants;
import be.belegkarnil.game.board.tak.Game;
import be.belegkarnil.game.board.tak.Piece;
import be.belegkarnil.game.board.tak.Player;
import be.belegkarnil.game.board.tak.event.RoundAdapter;
import be.belegkarnil.game.board.tak.event.RoundEvent;
import be.belegkarnil.game.board.tak.event.RoundListener;

import java.awt.Color;
import java.awt.Point;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * This {@link Strategy} represents a strategy that randomly plays.
 * Pieces are randomly sorted, position are randomly sorted, rotation are randomly sorted.
 * Then for each combination, if the move is valid ({@link Board#canPlace(Piece, Point)}) then play.
 * Otherwise, ask to replace/swap if the bag if not empty.
 * In other cases, return the skip action.
 *
 * @author Belegkarnil
 */
public class RandomStrategy implements Strategy, RoundListener{
	/**
	 * This constant defines an {@link Action} object which means to skip
	 */
	private static final Action SKIP_ACTION = new Action();
	private Random random;
	private boolean firstAction;

	/**
	 * Initialize the RandomStrategy (i.e. a random generator)
	 */
	public RandomStrategy(){
		random = new Random();
		firstAction = false;
	}

	/**
	 * Override the {@link Strategy#plays(Player, Board, Player)} and try to play randomy a valid action.
	 *
	 * @param myself   see {@link Strategy#plays}
	 * @param board    see {@link Strategy#plays}
	 * @param opponent see {@link Strategy#plays}
	 * @return a valid {Action#Action(Piece, Point, int)}
	 */
	@Override
	public Action plays(Player myself, Board board, Player opponent){
		if(!myself.hasPieces()) return SKIP_ACTION;

		Point[] freePositions = getFreePosition(board);

		if(firstAction){
			firstAction = false;
			return place(new Piece[]{opponent.getColor() == Constants.BLACK_PLAYER ? Piece.DOLMEN_BLACK : Piece.DOLMEN_WHITE}, freePositions);
		}

		Piece[] availablePieces = getAvailablePieces(myself);
		Point[] ownedPositions = getOwnedPosition(board, myself.getColor());

		Action myAction = SKIP_ACTION;


		double threasholdPlace = freePositions.length / (freePositions.length + ownedPositions.length);
		if(random.nextDouble() < threasholdPlace){
			// first, try to place if possible. Then try to move
			myAction = place(availablePieces, freePositions);
			if(myAction == SKIP_ACTION) myAction = move(ownedPositions, board);
		}else{
			// first, try to place if possible. Then try to move
			myAction = move(ownedPositions, board);
			if(myAction == SKIP_ACTION) myAction = place(availablePieces, freePositions);

		}
		return myAction;
	}

	private static final Point[] getDirections(){
		return new Point[]{new Point(0,1),new Point(1,0),new Point(-1,0),new Point(0,-1)};
	}
	private Action move(Point[] ownedPositions, Board board){
		Point[] directions = getDirections();

		shuffle(ownedPositions);
		shuffle(directions);

		for(Point position : ownedPositions){
			final Piece[] stack = board.getStack(position);
			final int maxLoad = Math.min(board.getLoadLimit(),stack.length);
			Integer[] distances = new Integer[maxLoad-1];
			if(distances.length > 0){
				for(int distance = 1; distance <= maxLoad; distance++){
					distances[distance - 1] = distance;
				}
			}
			shuffle(distances);

			for(Integer distance : distances){
				for(Point direction : directions){
					final int[] amount = randomMove(board,position,distance.intValue(),maxLoad,direction);
					if(amount != null) return new Action(position,translate(position,direction,distance.intValue()),amount);
				}
			}
		}
		return SKIP_ACTION;
	}

	private static Point translate(Point init, Point direction, int distance){
		return new Point(direction.x*distance + init.x,direction.y*distance + init.y);
	}

	private int[] randomMove(Board board, Point position, int distance, int maxLoad, Point direction){
		final Point dst = translate(position,direction,distance);
		if(!board.inBounds(dst)) return null;
		if(board.getTop(dst) != null && board.getTop(dst).isCapstone()) return null; // cannot put on a Dome
		// condition along the path except the last one (particular, see code above and below)
		for(int dist=1; dist<distance; dist++){
			Point current = translate(position,direction,dist);
			if(board.getTop(current) != null && !board.getTop(current).isDolmen()) return null;
		}
		// if path is ok, assign random elements
		final int[] amount = new int[distance];
		int rest = maxLoad;
		int total = 0;
		for(int reserved=distance, i=0; i < amount.length; i++, reserved--){
			int count = random.nextInt(rest-reserved+1)+1;
			amount[i] = count;
			total += count;
			rest -= count;
		}
		if(board.getTop(dst) != null && board.getTop(dst).isMenhir()){
			// last pos is a menhir, need a dome to modify the menhir as a dolmen
			Piece[] stack = board.getStack(position);
			if(!stack[stack.length-total].isCapstone()) return null;
		}
		return amount;
	}

	private Action place(Piece[] availablePiece, Point[] freePositions){
		if(availablePiece.length == 0) return SKIP_ACTION;
		if(freePositions.length == 0) return SKIP_ACTION;
		Piece p = availablePiece[random.nextInt(availablePiece.length)];
		Point pos = freePositions[random.nextInt(freePositions.length)];
		return new Action(p, pos);
	}

	private Point[] getOwnedPosition(Board board, Color player){
		LinkedList<Point> positions = new LinkedList<>();
		for(int row=0; row<board.getSize(); row++){
			for(int col=0; col<board.getSize(); col++){
				if(board.isUnderControl(player,row,col)) positions.add(new Point(col,row));
			}
		}
		return positions.toArray(new Point[positions.size()]);
	}

	private Point[] getFreePosition(Board board){
		LinkedList<Point> positions = new LinkedList<>();
		for(int row=0; row<board.getSize(); row++){
			for(int col=0; col<board.getSize(); col++){
				if(board.isFree(row, col)) positions.add(new Point(col,row));
			}
		}
		return positions.toArray(new Point[positions.size()]);
	}

	private Piece[] getAvailablePieces(Player player){
		List<Piece> availablePieces = new LinkedList<Piece>();
		if(player.countCapstones()>0){
			availablePieces.add(player.getColor() == Constants.BLACK_PLAYER ? Piece.CAPSTONE_BLACK : Piece.CAPSTONE_WHITE);
		}
		if(player.countStones()>0){
			if(player.getColor() == Constants.BLACK_PLAYER){
				availablePieces.add(Piece.DOLMEN_BLACK);
				availablePieces.add(Piece.MENHIR_BLACK);
			}else{
				availablePieces.add(Piece.DOLMEN_WHITE);
				availablePieces.add(Piece.MENHIR_WHITE);
			}
		}
		return availablePieces.toArray(new Piece[availablePieces.size()]);
	}

	private <T> void shuffle(T[] data){
		T temp;
		int swap;
		for(int i = 0; i < data.length; i++){
			swap = random.nextInt(data.length);
			temp = data[i];
			data[i] = data[swap];
			data[swap] = temp;
		}
	}


	@Override
	public void register(Game game){
		game.addRoundListener(this);
	}

	@Override
	public void unregister(Game game){
		game.removeRoundListener(this);
	}

	@Override
	public void onRoundBegins(RoundEvent event){
		this.firstAction = true;
	}

	@Override
	public void onRoundEnds(RoundEvent event){ /*nothing to do*/ }
}
