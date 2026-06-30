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
import static be.belegkarnil.game.board.tak.Constants.BLACK_PLAYER;
import static be.belegkarnil.game.board.tak.Constants.WHITE_PLAYER;

/**
 * TODO comments
 *
 * @author Belegkarnil
 */
public enum Piece{
	/** This piece is a dolmen associated to the black player (see {@link Constants}) */
	DOLMEN_BLACK(BLACK_PLAYER),
	/** This piece is a dolmen associated to the white player (see {@link Constants}) */
	DOLMEN_WHITE(WHITE_PLAYER),
	/** This piece is a menhir associated to the black player (see {@link Constants}) */
	MENHIR_BLACK(BLACK_PLAYER),
	/** This piece is a menhir associated to the white player (see {@link Constants}) */
	MENHIR_WHITE(WHITE_PLAYER),
	/** This piece is a capstone associated to the black player (see {@link Constants}) */
	CAPSTONE_BLACK(BLACK_PLAYER),
	/** This piece is a capstone associated to the white player (see {@link Constants}) */
	CAPSTONE_WHITE(WHITE_PLAYER),
	;
	/**
	 * The color of the piece which is either black or white (see {@link Constants})
	 */
	public final Color color;

	private Piece(Color color){
		this.color		= color;
	}

	/**
	 * Know if the current piece is associated to the WHITE (see {@link Constants}) {@link Player}
	 * @return true iff the piece is associated to the white player.
	 */
	public boolean isWhite(){
		return color == WHITE_PLAYER;
	}

	/**
	 * Know if the current piece is associated to the BLACK (see {@link Constants}) {@link Player}
	 * @return true iff the piece is associated to the black player.
	 */
	public boolean isBlack(){
		return color == BLACK_PLAYER;
	}

	/**
	 * Know if the current piece is a menhir
	 * @return true iff the piece is a menhir
	 */
	public boolean isMenhir(){
		return this.name().startsWith("MENHIR");
	}

	/**
	 * Know if the current piece is a dolmen
	 * @return true iff the piece is a dolmen
	 */
	public boolean isDolmen(){
		return this.name().startsWith("DOLMEN");
	}

	/**
	 * Know if the current piece is a capstone
	 * @return true iff the piece is a capstone
	 */
	public boolean isCapstone(){
		return this.name().startsWith("CAPSTONE");
	}
}
