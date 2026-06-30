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
import be.belegkarnil.game.board.tak.Player;

import java.util.EventObject;

/**
 * This event is used to notify interested parties that the game state is changed during a {@link Game}.
 *
 * @author Belegkarnil
 */
public class GameEvent extends EventObject{
	/**
	 * is a static constant with null value meaning that there are not yet a winner
	 */
	public static Player NO_WINNER = null;

	/**
	 * is the {link @Player} who win the {@link Game} or {@link GameEvent#NO_WINNER}
	 */
	public final Player winner;
	/**
	 * is the first {link @Player} when the game will start
	 */
	public final Player firstPlayer;
	/**
	 * is the second/other/opponent {link @Player} when the game will start
	 */
	public final Player secondPlayer;
	/**
	 * is the {link @Game} related to this event (i.e. the {@link Game} that generates the event)
	 */
	public final Game game;

	/**
	 * Constructor that does not define a winner ({@link GameEvent#NO_WINNER}), designed for begin events.
	 *
	 * @param game         the game related to the event, it's the source of the event
	 * @param firstPlayer  is the first player to play when the game will start
	 * @param secondPlayer is the second/other/opponent player to play when the game will start
	 * @throws IllegalArgumentException if source is null
	 */
	public GameEvent(final Game game, final Player firstPlayer, final Player secondPlayer){
		this(game, firstPlayer, secondPlayer, NO_WINNER);
	}

	/**
	 * Constructor requires a winner ({@link GameEvent#NO_WINNER}), designed for end events.
	 *
	 * @param game         the game related to the event, it's the source of the event
	 * @param firstPlayer  is the first player to play when the game will start
	 * @param secondPlayer is the second/other/opponent player to play when the game will start
	 * @param winner       is the player (first/second) who win the {@link Game} or {@link GameEvent#NO_WINNER}
	 * @throws IllegalArgumentException if source is null
	 */
	public GameEvent(final Game game, final Player firstPlayer, final Player secondPlayer, final Player winner){
		super(game);
		this.firstPlayer = firstPlayer;
		this.secondPlayer = secondPlayer;
		this.winner = winner;
		this.game = game;
	}
}
