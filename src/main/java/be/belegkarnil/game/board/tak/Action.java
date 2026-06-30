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
package be.belegkarnil.game.board.tak;

import java.awt.Point;
import java.util.Arrays;

/**
 * This class represents all possible actions during a {@link Game}.
 *
 * @author Belegkarnil
 */
public class Action{
	/**
	 * Constant that is used to specify the destination value (null) if the action is not a move action
	 */
	public static final Point NO_MOVE = null;
	/**
	 * Constat that is used to specify the piece value (null) if the action is not a place action
	 */
	public static final Piece NO_PIECE = null;
	private static final int[] NO_AMOUNT = null;
	/**
	 * The piece played during the action
	 */
	public final Piece piece;
	/**
	 * The position at which to put the piece during the action, or the src of a move
	 */
	public final Point position;
	/**
	 * The destination iff action is a move, null otherwhise
	 */
	public final Point destination;

	/**
	 * The amount along of piece placed along the path (move action only)
	 */
	final int[] amount;

	private Action(Piece piece, Point position, Point destination, int[] amount){
		this.piece = piece;
		this.position = position;
		this.destination = destination;
		this.amount = amount;
	}

	/**
	 * Constructor of a Place Action, put the piece at a position
	 * @param piece the {@link Piece} to place on the {@link Board}
	 * @param position the position to place the {@link Piece}
	 */
	public Action(Piece piece, Point position){
		this(piece, position, NO_MOVE, NO_AMOUNT);
	}

	/**
	 * Constructor of a Move Action, move amount of pieces along a path
	 * @param source The position of the stack to take {@link Piece}s
	 * @param destination The final position of the move, last place to place one or several {@link Piece}s
	 * @param amount The amount of {@link Piece}s along the path from source to destination. At pos i, put amount[i] pieces from stack at the source.
	 */
	public Action(Point source,Point destination, int amount[]){
		this(NO_PIECE, source, destination, amount);
	}

	/**
	 * Construct an action to skip turn, no action
	 */
	public Action(){
		this(NO_PIECE, NO_MOVE,NO_MOVE, NO_AMOUNT);
	}

	/**
	 * Know if the action is to place a {@link Piece}
	 *
	 * @return true iff the action is to place a {@link Piece}
	 */
	public boolean isPlace(){
		return this.piece != NO_PIECE;
	}

	/**
	 * Know if the action is to replace/swap a {@link Piece}
	 *
	 * @return true iff the action is to replace/swap a {@link Piece}
	 */
	public boolean isMove(){return this.piece == NO_PIECE && this.destination != NO_MOVE; }

	/**
	 * Know if the action is to skip the turn
	 *
	 * @return true iff the action is to skip the turn
	 */
	public boolean isSkip(){
		return this.piece == NO_PIECE && this.position == null;
	}

	/**
	 * Get a copy of the amount array (move action)
	 * @return the amount array
	 */
	public int[] getAmount(){
		if(this.amount == NO_AMOUNT) return NO_AMOUNT;
		return Arrays.copyOf(this.amount, this.amount.length);
	}
}
