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

import java.awt.Color;
import java.awt.Point;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

/**
 * This class represents the board of the Tak game.
 * This class includes some utility methods to compute neighbors positions, check if a piece can be put, and move of a {@link Piece}.
 *
 * @author Belegkarnil
 */
public class Board implements Cloneable{
	public static enum Size{
		/** Define a size of a tiny board according to the official rules */
		TINY(3),
		/** Define a size of a small board according to the official rules */
		SMALL(4),
		/** Define a size of a medium board according to the official rules */
		MEDIUM(5),
		/** Define a size of a large board according to the official rules */
		LARGE(6),
		/** Define a size of a huge board according to the official rules */
		HUGE(8);
		private Size(final int size){
			this.length = size;
		}
		/** Is the board size */
		public final int length;

		/**
		 * Count the initial capstones (depending on the board size) that a player has when the game starts
		 * @return the number of capstones (see {@link Piece})
		 */
		public int countInitialCapstones(){
			return countInitialCapstones(this.length);
		}
		/**
		 * Count the initial stones (depending on the board size) that a player has when the game starts
		 * @return the number of stones (see {@link Piece})
		 */
		public int countInitialStones(){
			return countInitialStones(this.length);
		}

		static int countInitialCapstones(final int size){
			if(size < Size.MEDIUM.length) return 0;
			if(size < Size.HUGE.length  ) return 1;
			return 2;
		}
		static int countInitialStones(final int size){
			if(size <= Size.MEDIUM.length){
				if(size != Size.MEDIUM.length) return size < Size.SMALL.length ? 10 : 15;
				return 21;
			}
			return size < Size.HUGE.length ? 30 : 50;
		}

	}

	/** Define an amount used to express that is invalid (i.e. 0) */
	public static final int INVALID_AMOUNT = 0;

	private Stack<Piece>[][] board;
	private int empty;

	/**
	 * Construct a {@link Size}.HUGE Board
	 */
	public Board(){
		this(Size.HUGE);
	}

	/**
	 * Construct a board of the given size
	 * @param size the board {@link Size}
	 */
	public Board(Size size){
		this.board = new Stack[size.length][size.length];
		this.empty = size.length*size.length;
		for(int i = 0; i < size.length; i++){
			for(int j = 0; j < size.length; j++){
				this.board[i][j] = new Stack<>();
			}
		}
	}

	/**
	 * Construct a copy of the current board
	 * @param board The board to copy
	 */
	public Board(Board board){
		final int size = board.board.length;
		this.board = new Stack[size][size];
		this.empty = board.empty;
		for(int i = 0; i < size; i++){
			for(int j = 0; j < size; j++){
				this.board[i][j] = new Stack<>();
				this.board[i][j].addAll(Arrays.asList(board.getStack(i,j)));
			}
		}
	}

	/**
	 * Reset the board, remove all pieces
	 */
	protected void reset(){
		this.empty = getSize()*getSize();
		for(Stack<Piece>[] row:this.board){
			for(Stack<Piece> cell:row)
				cell.clear();
		}
	}

	/**
	 * Know if the position is on the board
	 * @param point The position to check if it is on the board
	 * @return true iff the position is on the board, false otherwise
	 */
	public boolean inBounds(Point point){
		return inBounds(point.y,point.x);
	}
	/**
	 * Know if the position is on the board
	 * @param row The y coordinate of position to check if it is on the board
	 * @param column The x coordinate of position to check if it is on the board
	 * @return true iff the position is on the board, false otherwise
	 */
	public boolean inBounds(int row, int column){
		if(row < 0 || row >= board.length) return false;
		if(column < 0 || column >= board[row].length) return false;
		return true;
	}

	/**
	 * Know if the position is free on the board
	 * @param point The position to check if it is free on the board
	 * @return true iff the position is free on the board, false otherwise
	 */
	public boolean isFree(Point point){
		return isFree(point.y,point.x);
	}
	/**
	 * Know if the position is free on the board
	 * @param row The y coordinate of position to check if it is free on the board
	 * @param column The x coordinate of position to check if it is free on the board
	 * @return true iff the position is free on the board, false otherwise
	 */
	public boolean isFree(int row, int column){
		return this.board[row][column].isEmpty();
	}
	/**
	 * Know if the position is under the control of a player
	 * @param player The color of the player to check
	 * @param point The position to check
	 * @return true iff the position is under the control of the player, false otherwise
	 */
	public boolean isUnderControl(Color player,Point point){
		return isUnderControl(player,point.y,point.x);
	}
	/**
	 * Know if the position is under the control of a player
	 * @param player The color of the player to check
	 * @param row The y coordinate of position to check
	 * @param column The x coordinate of position to check
	 * @return true iff the position is under the control of the player, false otherwise
	 */
	public boolean isUnderControl(Color player,int row, int column){
		if(isFree(row,column)) return false;
		return this.board[row][column].peek().color == player;
	}

	/**
	 * Know if a {@link Piece} can be placed at a given position on the board
	 * @param piece The {@link Piece} to place
	 * @param point The position to check
	 * @return true iff the piece can be placed, false otherwise
	 */
	public boolean canPlace(Piece piece, Point point){
		return canPlace(piece,point.y,point.x);
	}
	/**
	 * Know if a {@link Piece} can be placed at a given position on the board
	 * @param piece The {@link Piece} to place
	 * @param row The y coordinate of position to check
	 * @param column The x coordinate of position to check
	 * @return true iff the piece can be placed, false otherwise
	 */
	public boolean canPlace(Piece piece, int row, int column){
		if(!inBounds(row, column) || !isFree(row, column)) return false;
		return true;
	}

	/**
	 * Know if the board is completed (no cells are free)
	 * @return true iff countEmpty() == 0, false otherwise
	 */
	public boolean isCompleted(){
		return this.empty == 0;
	}

	/**
	 * Count how much cells are free
	 * @return Amount of free (empty) cells of the board
	 */
	public int countEmpty(){
		return this.empty;
	}

	/**
	 * Place a {@link Piece} on the board at a given position
	 * @param piece the {@link Piece} to place
	 * @param point the board position to place the piece
	 * @return Color of the player (see {@link Constants}) that completed a path, null otherwise
	 */
	protected Color place(Piece piece, Point point){
		if(this.board[point.y][point.x].isEmpty()){
			this.empty--;
		}
		this.board[point.y][point.x].push(piece);
		if(pathExists(point, piece.color)) return piece.color;
		final Color opponent = Constants.BLACK_PLAYER == piece.color ? Constants.WHITE_PLAYER : Constants.BLACK_PLAYER;
		if(pathExists(point, opponent)) return opponent;
		return null;
	}

	/**
	 * Get neighbors positions of a cell in the board
	 * @param cell A position on the board
	 * @return The list of existing (i.e. inBounds(.)) positions
	 */
	public List<Point> getNeighbors(Point cell){
		List<Point> neighbors = new LinkedList<Point>();
		Point neighbor;
		neighbor = cell.getLocation();
		neighbor.translate(0,1);
		if(inBounds(neighbor)) neighbors.add(neighbor);

		neighbor = cell.getLocation();
		neighbor.translate(0,-1);
		if(inBounds(neighbor)) neighbors.add(neighbor);

		neighbor = cell.getLocation();
		neighbor.translate(-1,0);
		if(inBounds(neighbor)) neighbors.add(neighbor);

		neighbor = cell.getLocation();
		neighbor.translate(1,0);
		if(inBounds(neighbor)) neighbors.add(neighbor);
		return neighbors;
	}

	/**
	 * Know if a path exists to link left to right or top to down
	 * @param point A point on the path (i.e. new {@link Piece} or {@link Piece} moved)
	 * @param color The color of the {@link Player} that attempted to create a path
	 * @return true iff a path exists from point with the given color, false otherwise
	 */
	public boolean pathExists(Point point, Color color){
		boolean linkToTop		= false;
		boolean linkToBottom	= false;
		boolean linkToLeft	= false;
		boolean linkToRight	= false;
		final int last = getSize()-1;

		final boolean[][] visited = new boolean[getSize()][getSize()];
		for(int i = 0; i < getSize(); i++){
			Arrays.fill(visited[i], false);
		}

		Queue<Point> frontier = new LinkedList();
		frontier.add(point);
		while(!frontier.isEmpty()){
			Point current = frontier.poll();
			Piece top = getTop(current);

			if(!visited[current.y][current.x] && top != null && top.color.equals(color) && !top.isMenhir()){
				if(current.x == 0) linkToLeft = true;
				else if(current.x == last) linkToRight = true;
				if(current.y == 0) linkToTop = true;
				else if(current.y == last) linkToBottom = true;

				for(Point neighbor : getNeighbors(current)){
					frontier.add(neighbor);
				}
				visited[current.y][current.x] = true;
			}
		}
		return (linkToTop && linkToBottom) || (linkToLeft && linkToRight);
	}

	/**
	 * Know if a path exists to link left to right or top to down
	 * @param row is the y coordinate of a point on the path (i.e. new {@link Piece} or {@link Piece} moved)
	 * @param column is the x coordinate of a point on the path (i.e. new {@link Piece} or {@link Piece} moved)
	 * @param color The color of the {@link Player} that attempted to create a path
	 * @return true iff a path exists from point with the given color, false otherwise
	 */
	public boolean pathExists(int row, int column, Color color){
		return pathExists(new Point(column,row),color);
	}

	/**
	 * Know if a player can make the given move
	 * @param player The {@link Player}'s {@link Color} (see {@link Constants})
	 * @param src The initial position of the path
	 * @param amount The amount of {@link Piece}s taken from initial position and placed along the path
	 * @param dst The final position of the path
	 * @return true iff the move is valid, false otherwise
	 */
	public boolean canMove(Color player,Point src, int[] amount, Point dst){
		return canMove(player, src.y, src.x, amount, dst.y, dst.x);
	}

	/**
	 * Compute total amount of a move action (path)
	 * @param amount The amount array of a move action
	 * @return total amount iff move seems valid (i.e. each amount > 0 and amount.length > 0), false otherwise
	 */
	protected static final int computeAmount(int[] amount){
		if(amount.length < 1) return INVALID_AMOUNT;
		int sum = 0;
		for(int i = 0; i < amount.length; i++){
			if(amount[i] < 1) return INVALID_AMOUNT;
			sum += amount[i];
		}
		return sum;
	}

	/**
	 * Know if a player can make the given move
	 * @param player The {@link Player}'s {@link Color} (see {@link Constants})
	 * @param srcRow is the y coordinate of the initial position of the path
	 * @param srcColumn is the x coordinate of the initial position of the path
	 * @param amount The amount of {@link Piece}s taken from initial position and placed along the path
	 * @param dstRow is the y coordinate of the final position of the path
	 * @param dstColumn is the x coordinate of the final position of the path
	 * @return true iff the move is valid, false otherwise
	 */
	public boolean canMove(Color player,int srcRow, int srcColumn, int amount[], int dstRow, int dstColumn){
		if(!inBounds(srcRow, srcColumn) || !inBounds(dstRow, dstColumn)) return false;
		if(!isUnderControl(player,srcRow,srcColumn)) return false;
		if(!isStraightPath(srcRow,srcColumn,dstRow,dstColumn)) return false;
		final int total = computeAmount(amount);
		if(total == INVALID_AMOUNT) return false;
		if(total > getLoadLimit()) return false;
		if(board[srcRow][srcColumn].size() < total) return false;
		final boolean topStackIsCapstone = board[srcRow][srcColumn].peek().isCapstone();
		if(srcRow == dstRow){
			if(srcColumn == dstColumn) return false;// no move
			int moves = dstColumn - srcColumn;
			if(Math.abs(moves) != amount.length) return false;
			final int sign = moves < 0 ? -1 : 1;
			int i = 0;
			moves = Math.abs(moves);
			while(moves>1){
				i +=sign;
				moves--;
				if(!board[srcRow][srcColumn + i].empty() && !board[srcRow][srcColumn + i].peek().isDolmen()) return false;
			}
			if(board[dstRow][dstColumn].empty()) return true;
			final Piece top = board[dstRow][dstColumn].peek();
			if(top.isDolmen()) return true;
			return top.isMenhir() && amount[amount.length-1] == 1 && topStackIsCapstone;
		}
		if(srcColumn == dstColumn){
			if(srcRow == dstRow) return false;// no move
			int moves = dstRow - srcRow;
			if(Math.abs(moves) != amount.length) return false;
			final int sign = moves < 0 ? -1 : 1;
			int i = 0;
			moves = Math.abs(moves);
			while(moves>1){
				i +=sign;
				moves--;
				if(!board[srcRow + i][srcColumn].empty() && !board[srcRow + i][srcColumn].peek().isDolmen()) return false;
			}
			if(board[dstRow][dstColumn].empty()) return true;
			final Piece top = board[dstRow][dstColumn].peek();
			if(top.isDolmen()) return true;
			return top.isMenhir() && amount[amount.length-1] == 1 && topStackIsCapstone;
		}
		return false;// No move
	}

	/**
	 * Know if exists a straight path (horizontal or vertical) between two positions
	 * @param srcRow is the y coordinate of the initial position of the path
	 * @param srcColumn is the x coordinate of the initial position of the path
	 * @param dstRow is the y coordinate of the final position of the path
	 * @param dstColumn is the x coordinate of the final position of the path
	 * @return true iff the path is going straight, false otherwise
	 */
	public static boolean isStraightPath(int srcRow, int srcColumn, int dstRow, int dstColumn){
		return srcRow == dstRow || srcColumn == dstColumn;
	}

	/**
	 * Know if exists a straight path (horizontal or vertical) between two positions
	 * @param src The initial position of the path
	 * @param dst The final position of the path
	 * @return true iff the path is going straight, false otherwise
	 */
	public static boolean isStraightPath(Point src, Point dst){
		return isStraightPath(src.y,src.x,dst.y,dst.x);
	}

	/**
	 * Move some {@link Piece}s on the board according to the given path
	 * @param src The initial position of the path
	 * @param amount The amount of {@link Piece}s taken from initial position and placed along the path
	 * @param dst The final position of the path
	 * @return Color of the player (see {@link Constants}) that completed a path, null otherwise
	 */
	protected Color move(Point src, int[] amount, Point dst){
		final int deltaX = src.y == dst.y ? ((dst.x - src.x) < 0 ? -1 : 1) : 0;
		final int deltaY = src.x == dst.x ? ((dst.y - src.y) < 0 ? -1 : 1) : 0;
		final Color player	= Constants.BLACK_PLAYER == board[src.y][src.x].peek().color ? Constants.BLACK_PLAYER : Constants.WHITE_PLAYER;
		final Color opponent	= Constants.BLACK_PLAYER == player ? Constants.WHITE_PLAYER : Constants.BLACK_PLAYER;

		Color firstCompleted = null;

		final int totalCarry = computeAmount(amount);
		Stack<Piece> stack = new Stack<>();
		for(int i = 0; i < totalCarry; i++){
			stack.add(this.board[src.y][src.x].pop());
		}
		if(board[src.y][src.x].isEmpty()) this.empty++;

		int row = src.y, col = src.x;
		for(int i = 0; i < amount.length-1; i++){
			row += deltaY;
			col += deltaX;
			if(board[row][col].isEmpty()) this.empty--;
			for(int count=0; count < amount[i]; count++){
				board[row][col].push(stack.pop());
			}
			if(firstCompleted == null && pathExists(row, col, player)) firstCompleted=player;
			if(firstCompleted == null && pathExists(row, col, opponent)) firstCompleted=opponent;
		}
		row += deltaY;
		col += deltaX;
		if(board[row][col].isEmpty()) this.empty--;
		if(stack.size() == 1 && stack.peek().isCapstone() && !board[row][col].isEmpty() && board[row][col].peek().isMenhir()){
			switch(board[row][col].pop()){
				case MENHIR_BLACK: board[row][col].push(Piece.DOLMEN_BLACK); break;
				case MENHIR_WHITE: board[row][col].push(Piece.DOLMEN_WHITE); break;
			}
			board[row][col].push(stack.pop());
		}else{
			for(int count = 0; count < amount[amount.length - 1]; count++){
				board[row][col].push(stack.pop());
			}
		}
		if(firstCompleted == null && pathExists(row, col, player)) firstCompleted=player;
		if(firstCompleted == null && pathExists(row, col, opponent)) firstCompleted=opponent;
		return firstCompleted;
	}

	/**
	 * Get the load limit of a stack defined in the rules (i.e. board size)
	 * @return The maximum load of each stack
	 */
	public int getLoadLimit(){
		return getSize();
	}

	/**
	 * Get the board size
	 * @return The board size
	 */
	public int getSize(){
		return this.board.length;
	}

	/**
	 * Count how much times a type of {@link Piece} (color and type) is on (the top of) the board
	 * @param piece A {@link Piece} (color and type) that defines the type
	 * @return the counter
	 */
	public int countTopPieces(Piece piece){
		int counter = 0;
		for(int row = 0; row < this.board.length; row++){
			for(int col = 0; col < this.board.length; col++){
				if(!isFree(row,col) && board[row][col].peek().equals(piece)) counter++;
			}
		}
		return counter;
	}

	/**
	 * Get the stack of {@link Piece}s at a given position (bottom is at position 0, top at stack length -1)
	 * @param row The y coordinate of the position to fetch the stack
	 * @param col The x coordinate of the position to fetch the stack
	 * @return A copy of {@link Piece}s that are at the given position
	 */
	public Piece[] getStack(int row, int col){
		Piece[] copy = new Piece[this.board[row][col].size()];
		board[row][col].copyInto(copy);
		return copy;
	}

	/**
	 * Get the stack of {@link Piece}s at a given position (bottom is at position 0, top at stack length -1)
	 * @param position The position to fetch the stack
	 * @return A copy of {@link Piece}s that are at the given position
	 */
	public Piece[] getStack(Point position){
		return getStack(position.y, position.x);
	}

	/**
	 * Get the Piece (at top of a stack) at a given position
	 * @param position The position to fetch the stack
	 * @return A copy of the {@link Piece} that is at the given position
	 */
	public Piece getTop(Point position){
		return getTop(position.y, position.x);
	}
	/**
	 * Get the Piece (at top of a stack) at a given position
	 * @param row The y coordinate of the position to fetch the stack
	 * @param col The x coordinate of the position to fetch the stack
	 * @return A copy of the {@link Piece} that is at the given position
	 */
	public Piece getTop(int row, int col){
		if(board[row][col].isEmpty()) return null;
		return board[row][col].peek();
	}

	/**
	 * Count the initial capstones (depending on the board size) that a player has when the game starts
	 * @return the number of capstones (see {@link Piece})
	 */
	public int countInitialCapstones(){
		return Size.countInitialCapstones(getSize());
	}
	/**
	 * Count the initial stones (depending on the board size) that a player has when the game starts
	 * @return the number of stones (see {@link Piece})
	 */
	public int countInitialStones(){
		return Size.countInitialStones(getSize());
	}

	/**
	 * Clone (see {@link Cloneable}) the current board
	 * @return A copy of the current boar
	 */
	@Override
	public Board clone(){
		return new Board(this);
	}
}
