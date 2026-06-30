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
import be.belegkarnil.game.board.tak.Player;

/**
 * This {@link Strategy} represents a strategy that always skip (i.e. does not play)
 *
 * @author Belegkarnil
 */
public class SkipStrategy extends StrategyAdapter{
	/**
	 * This constant defines an {@link Action} object which means to skip
	 */
	public static final Action SKIP_ACTION = new Action();

	/**
	 * Override the {@link Strategy#plays(Player, Board, Player)} and always return {@link SkipStrategy#SKIP_ACTION}
	 *
	 * @param myself   see {@link Strategy#plays}
	 * @param board    see {@link Strategy#plays}
	 * @param opponent see {@link Strategy#plays}
	 * @return {@link SkipStrategy#SKIP_ACTION}
	 */
	@Override
	public Action plays(Player myself, Board board, Player opponent){
		return SKIP_ACTION;
	}
}
