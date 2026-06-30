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

import be.belegkarnil.game.board.tak.Board;
import be.belegkarnil.game.board.tak.Game;
import be.belegkarnil.game.board.tak.Player;
import be.belegkarnil.game.board.tak.strategy.SkipStrategy;

import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class is a GUI component of the Game. It is the main frame that create the GUI ({@link InnerPanel}).
 *
 * @author Belegkarnil
 */
public class MainFrame extends JFrame implements ActionListener{
	public MainFrame(){
		super("Tak");
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		final JPanel contentPane = new JPanel(new BorderLayout());

		final BoardPanel boardPanel = new BoardPanel();
		final LogPanel logPanel = new LogPanel();
		final SettingsPanel settingsPanel = new SettingsPanel();
		final PiecesPanel piecesPanel = new PiecesPanel();
		final TimePanel timePanel = new TimePanel();
		final ScorePanel scorePanel = new ScorePanel();

		contentPane.add(boardPanel, BorderLayout.CENTER);
		contentPane.add(settingsPanel, BorderLayout.NORTH);
		contentPane.add(logPanel, BorderLayout.WEST);

		contentPane.add(piecesPanel, BorderLayout.EAST);

		final JPanel south = new JPanel(new GridLayout(2, 1));
		south.add(scorePanel);
		south.add(timePanel);
		contentPane.add(south, BorderLayout.SOUTH);

		setContentPane(contentPane);
	}

	@Override
	public void actionPerformed(ActionEvent e){
		Player p1 = new Player("John", new SkipStrategy());
		Player p2 = new Player("Jane", new SkipStrategy());
		Board board = new Board();
		Game game = new Game(board, p1, p2);
		game.run();
	}
}
