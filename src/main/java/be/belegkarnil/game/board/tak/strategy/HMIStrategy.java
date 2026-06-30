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
import be.belegkarnil.game.board.tak.Piece;
import be.belegkarnil.game.board.tak.Player;

import java.awt.Point;

/**
 * This class is a special a {@link Strategy} class that is recognized by the GUI. This class allow
 * a human player to select a {@link Piece} and a position {@link Point} in order to play an {@link Action}.
 *
 * @author Belegkarnil
 */
public class HMIStrategy extends StrategyAdapter{
	private final Object lock;
	private Action action;
	private boolean undefined;

	/**
	 * Constructs a {@link Strategy} that interact with the GUI
	 */
	public HMIStrategy(){
		lock = new Object();
		undefined = true;
		action = null;
	}

	/**
	 * Defines the next action as a skip action
	 */
	public void setSkipAction(){
		setAction(new Action());
	}

	/**
	 * Defines the next action to play
	 *
	 * @param piece    the piece to play
	 * @param position the position at which put the piece
	 */
	public void setPiece(Piece piece, Point position){
		setAction(new Action(piece, position));
	}

	/**
	 * TODO
	 */
	public void setMoveAction(Point src,Point dst,int[] amount){
		setAction(new Action(src,dst,amount));
	}

	/**
	 * Defines the next action to play
	 *
	 * @param action the action to play
	 */
	public void setAction(Action action){
		synchronized(lock){
			this.action = action;
			undefined = false;
		}
	}

	/**
	 * Override the {@link Strategy#plays(Player, Board, Player)} and return what defined by the GUI interface after calling {@link HMIStrategy#setAction(Action)}
	 *
	 * @param myself   see {@link Strategy#plays}
	 * @param board    see {@link Strategy#plays}
	 * @param opponent see {@link Strategy#plays}
	 * @return The {@link Action} defined by the GUI after calling {@link HMIStrategy#setAction(Action)}
	 */
	@Override
	public Action plays(Player myself, Board board, Player opponent){
		Action playing = null;
		boolean undefined = true;
		while(undefined){
			synchronized(lock){
				undefined = this.undefined;
				if(!undefined){
					playing = action;
					action = null;
					this.undefined = true;
				}
			}
			if(undefined) Thread.yield();
		}
		return playing;
	}
}
