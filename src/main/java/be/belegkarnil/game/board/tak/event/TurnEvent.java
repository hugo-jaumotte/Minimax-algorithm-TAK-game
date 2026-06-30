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

import be.belegkarnil.game.board.tak.Action;
import be.belegkarnil.game.board.tak.Game;
import be.belegkarnil.game.board.tak.Player;

import java.util.EventObject;

/**
 * This event is used to notify interested parties that the turn state is changed during a {@link Game}.
 *
 * @author Belegkarnil
 */
public class TurnEvent extends EventObject{
	/**
	 * is the static constant that represents there are no action (null)
	 */
	public static final Action NO_ACTION = null;

	/**
	 * is the first {link @Player} when the turn will start, the event is related to this player
	 */
	public final Player current;
	/**
	 * is the second/other/opponent {link @Player} when turn round will start
	 */
	public final Player opponent;
	/**
	 * is the identifier (counter) of the related round.
	 */
	public final int round;
	/**
	 * is the identifier (counter) of the related turn.
	 */
	public final int turn;
	/**
	 * is the chosen {#link Action}, what the current {@link Player}'s strategy plays
	 */
	public final Action action;
	/**
	 * is the {link @Game} related to this event (i.e. the {@link Game} that generates the event)
	 */
	public final Game game;

	/**
	 * Constructor that does not define a winner ({@link GameEvent#NO_WINNER}), designed for begin events.
	 *
	 * @param game     the game related to the event, it's the source of the event
	 * @param current  is the current player related to this event
	 * @param opponent is the second/other/opponent player
	 * @param round    is the round identifier related to the event
	 * @param turn     is the turn identifier related to the event
	 * @throws IllegalArgumentException if source is null
	 */
	public TurnEvent(final Game game, final Player current, final Player opponent, final int round, final int turn){
		this(game, current, opponent, round, turn, NO_ACTION);
	}

	/**
	 * Constructor requires an action ({@link TurnEvent#NO_ACTION}), designed for end events.
	 *
	 * @param game     the game related to the event, it's the source of the event
	 * @param current  is the current player related to this event
	 * @param opponent is the second/other/opponent player
	 * @param round    is the round identifier related to the event
	 * @param turn     is the turn identifier related to the event
	 * @param action   is the action taken by the current player or {@link TurnEvent#NO_ACTION}
	 * @throws IllegalArgumentException if source is null
	 */
	public TurnEvent(final Game game, final Player current, final Player opponent, final int round, final int turn, final Action action){
		super(game);
		this.current = current;
		this.opponent = opponent;
		this.round = round;
		this.turn = turn;
		this.action = action;
		this.game = game;
	}
}
