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

import be.belegkarnil.game.board.tak.Game;

/**
 * An abstract adapter class for registering events.
 * The methods in this class are empty. This class exists as convenience for creating strategy objects.
 * See {@link be.belegkarnil.game.board.tak.event.GameListener}, {@link be.belegkarnil.game.board.tak.event.MisdesignListener}, {@link be.belegkarnil.game.board.tak.event.RoundListener}, and {@link be.belegkarnil.game.board.tak.event.TurnListener}
 *
 * @author Belegkarnil
 */
public abstract class StrategyAdapter implements Strategy{
	/**
	 * See {@link Strategy#register}
	 *
	 * @param game See {@link Strategy#register}
	 */
	@Override
	public void register(Game game){
	}

	/**
	 * See {@link Strategy#unregister}
	 *
	 * @param game See {@link Strategy#unregister}
	 */
	@Override
	public void unregister(Game game){
	}
}
