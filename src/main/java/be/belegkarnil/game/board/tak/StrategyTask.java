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

import be.belegkarnil.game.board.tak.strategy.Strategy;

/**
 * This class is a core class of the Game and represents the decision made by a {@link Strategy}.
 * The strategy can choose a {@link Piece} to play, but within a limited time. This class is a thread
 * ensuring that a strategy returns a {@link Piece} or null at timeout.
 *
 * @author Belegkarnil
 */
class StrategyTask extends Thread{
	private final Player current, opponent;
	private final Board board;
	private Action action;

	private final Object lock = new Object();

	public StrategyTask(Player current, Board board, Player opponent){
		this.current = current;
		this.opponent = opponent;
		this.board = board;
		this.action = null;
	}

	@Override
	public void run(){
		final Action action = current.behaves(board, opponent);
		synchronized(lock){
			this.action = action;
		}
	}

	public Action getAction(){
		final Action action;
		synchronized(lock){
			action = this.action;
		}
		return action;
	}
}
