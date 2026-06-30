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
import be.belegkarnil.game.board.tak.Game;
import be.belegkarnil.game.board.tak.Player;
import be.belegkarnil.game.board.tak.event.GameListener;
import be.belegkarnil.game.board.tak.event.MisdesignListener;
import be.belegkarnil.game.board.tak.event.RoundListener;
import be.belegkarnil.game.board.tak.event.TurnListener;

/**
 * This interface represents a generic strategy behavior (i.e. choose an action to play by a {@link Player}).
 * This interface allows to listens game events, see {@link be.belegkarnil.game.board.tak.event.GameListener}, {@link be.belegkarnil.game.board.tak.event.MisdesignListener}, {@link be.belegkarnil.game.board.tak.event.RoundListener}, and {@link be.belegkarnil.game.board.tak.event.TurnListener}
 *
 * @author Belegkarnil
 */
public interface Strategy{

	/**
	 * The method represents the choice made by the strategy based on the current player the use the strategy, the current board status and his opponent
	 *
	 * @param myself   The current player that plays the strategy
	 * @param board    The current board status
	 * @param opponent The opponent player
	 * @return The action to play by myself #{@link Player}
	 */
	public Action plays(Player myself, Board board, Player opponent);

	/**
	 * This method is called before a game starts in order to allow the strategy to listen events.
	 * (see {@link Game#addGameListener(GameListener)}, {@link Game#addMisdesignListener(MisdesignListener)}, {@link Game#addRoundListener(RoundListener)}, and {@link Game#addTurnListener(TurnListener)})
	 *
	 * @param game the new game to listen events
	 */
	public void register(Game game);

	/**
	 * This method is called after a game finishes in order to clear and the strategy stops to listen events.
	 * (see {@link Game#removeGameListener(GameListener)}, {@link Game#removeMisdesignListener(MisdesignListener)}, {@link Game#removeRoundListener(RoundListener)}, and {@link Game#removeTurnListener(TurnListener)})
	 *
	 * @param game the finished game to stop listening events
	 */
	public void unregister(Game game);
}
