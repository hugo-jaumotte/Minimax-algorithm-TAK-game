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

/**
 * Game constants (colors)
 *
 * @author Belegkarnil
 */
public final class Constants{
	private Constants(){}

	/**
	 * The color associated to the black player
	 */
	public static final Color BLACK_PLAYER = new Color(87,227,137);
	/**
	 * The color associated to the white player
	 */
	public static final Color WHITE_PLAYER = Color.PINK;

	/**
	 * The color of the player who begins, which is the black player
	 */
	public static final Color FIRST_PLAYER = BLACK_PLAYER;
	/**
	 * The color of the second player, which is the white player
	 */
	public static final Color SECOND_PLAYER = WHITE_PLAYER;
}
