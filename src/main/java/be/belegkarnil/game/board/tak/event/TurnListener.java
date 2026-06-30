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
package be.belegkarnil.game.board.tak.event;

import be.belegkarnil.game.board.tak.Game;

import java.util.EventListener;

/**
 * The listener interface for receiving "interesting" turn events (begin, and end) during a game.
 * The class that is interested in processing a turn event either implements this interface (and all the methods it contains) or extends the abstract {@link TurnAdapter} class (overriding only the methods of interest).
 * <p>
 * The listener object created from that class is then registered in a {@link be.belegkarnil.game.board.tak.strategy.Strategy}'s {@link be.belegkarnil.game.board.tak.strategy.Strategy#register(Game)} on the {@link Game} object using {@link Game#addTurnListener(TurnListener)}.
 * Please, do not forget to unregister the listener when the {@link be.belegkarnil.game.board.tak.strategy.Strategy}'s {@link be.belegkarnil.game.board.tak.strategy.Strategy#unregister(Game)} method is called by using {@link Game#removeTurnListener(TurnListener)} on the {@link Game} object.
 * A turn event is generated when a player has to play, and has played. When a turn event occurs, the relevant method in the listener object is invoked, and the {@link TurnEvent} is passed to it.
 *
 * @author Belegkarnil
 */
public interface TurnListener extends EventListener{
	/**
	 * Invoked when a turn begins.
	 *
	 * @param event The event that contains all information about the current turn
	 */
	public void onTurnBegins(TurnEvent event);

	/**
	 * Invoked when a turn ends.
	 *
	 * @param event The event that contains all information about the current turn
	 */
	public void onTurnEnds(TurnEvent event);
}
