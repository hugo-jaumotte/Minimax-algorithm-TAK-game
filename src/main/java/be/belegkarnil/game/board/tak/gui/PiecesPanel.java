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
package be.belegkarnil.game.board.tak.gui;

import be.belegkarnil.game.board.tak.Constants;
import be.belegkarnil.game.board.tak.Game;
import be.belegkarnil.game.board.tak.Piece;
import be.belegkarnil.game.board.tak.Player;
import be.belegkarnil.game.board.tak.event.TurnAdapter;
import be.belegkarnil.game.board.tak.event.TurnEvent;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

/**
 * This class is a GUI component of the Game. It is the pieces panel ({@link InnerPanel}) that enables GUI interaction with a player (see {@link be.belegkarnil.game.board.tak.strategy.HMIStrategy}).
 *
 * @author Belegkarnil
 */
public class PiecesPanel extends InnerPanel implements ActionListener{
	public static final String SKIP_ACTION = "Skip";
	private static final int BUTTON_WIDTH	= 80;
	private static final int BUTTON_HEIGHT	= 80;
	/*private static final String DOLMEN_COMMAND	= "DOLMEN_COMMAND";
	private static final String MENHIR_COMMAND	= "MENHIR_COMMAND";
	private static final String CAPSTONE_COMMAND = "CAPSTONE_COMMAND";*/
	private static final String SKIP_COMMAND		= "SKIP_COMMAND";

	private final JPanel content;
	private JButton dolmenButton,menhirButton, capstoneButton;
	//private Color currentColor;

	public PiecesPanel(){
		setLayout(new BorderLayout());

		content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

		final JButton skipButton = new JButton(SKIP_ACTION);
		skipButton.setActionCommand(SKIP_COMMAND);
		dolmenButton	= new JButton();
		menhirButton	= new JButton();
		capstoneButton = new JButton();
		/*dolmenButton.setActionCommand(DOLMEN_COMMAND);
		menhirButton.setActionCommand(MENHIR_COMMAND);
		capstoneButton.setActionCommand(CAPSTONE_COMMAND);*/
		dolmenButton.addActionListener(this);
		menhirButton.addActionListener(this);
		capstoneButton.addActionListener(this);
		content.add(skipButton);
		content.add(dolmenButton);
		content.add(menhirButton);
		content.add(capstoneButton);

		//currentColor = Constants.FIRST_PLAYER;

		final Dimension dimension = new Dimension( BUTTON_WIDTH, BUTTON_HEIGHT);
		skipButton.setMinimumSize(dimension);
		skipButton.setMaximumSize(dimension);
		skipButton.setPreferredSize(dimension);
		skipButton.setSize(dimension);
		dolmenButton.setMinimumSize(dimension);
		dolmenButton.setMaximumSize(dimension);
		dolmenButton.setPreferredSize(dimension);
		dolmenButton.setSize(dimension);
		menhirButton.setMinimumSize(dimension);
		menhirButton.setMaximumSize(dimension);
		menhirButton.setPreferredSize(dimension);
		menhirButton.setSize(dimension);
		capstoneButton.setMinimumSize(dimension);
		capstoneButton.setMaximumSize(dimension);
		capstoneButton.setPreferredSize(dimension);
		capstoneButton.setSize(dimension);

		add(content, BorderLayout.CENTER);

		reset();
	}

	@Override
	void register(Game game){
		if(game == null){
			reset();
			return;
		}
		game.addTurnListener(new TurnAdapter(){
			@Override
			public void onTurnBegins(TurnEvent te){
				update(te.current);
			}
		});

	}

	@Override
	void onPieceSelected(String name){

	}

	public void reset(){
		update((Player) null);
	}

	public void update(Player player){
		dolmenButton.setEnabled(false);
		menhirButton.setEnabled(false);
		capstoneButton.setEnabled(false);

		if(player == null) return;

		final Map<Piece,Integer> pieces = player.getPieces();
		for(final Piece piece: pieces.keySet()){
			final int amount = pieces.get(piece);
			if(piece.isDolmen()){
				dolmenButton.setIcon(CellDrawer.createDolmenIcon(piece.color,amount,BUTTON_WIDTH,BUTTON_HEIGHT));
				dolmenButton.setActionCommand(piece.name());
				if(amount > 0) dolmenButton.setEnabled(true);
			}else if(piece.isMenhir()){
				menhirButton.setIcon(CellDrawer.createMenhirIcon(piece.color,amount,BUTTON_WIDTH,BUTTON_HEIGHT));
				menhirButton.setActionCommand(piece.name());
				if(amount > 0) menhirButton.setEnabled(true);
			}else if(piece.isCapstone()){
				capstoneButton.setIcon(CellDrawer.createCapstoneIcon(piece.color,amount,BUTTON_WIDTH,BUTTON_HEIGHT));
				capstoneButton.setActionCommand(piece.name());
				if(amount > 0) capstoneButton.setEnabled(true);
			}
		}

	}

	@Override
	public void actionPerformed(ActionEvent actionEvent){
		final String command = actionEvent.getActionCommand();
		firePieceSelected(command);
	}
}