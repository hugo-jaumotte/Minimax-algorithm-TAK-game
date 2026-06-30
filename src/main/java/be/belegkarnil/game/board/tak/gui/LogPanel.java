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

import be.belegkarnil.game.board.tak.Action;
import be.belegkarnil.game.board.tak.Game;
import be.belegkarnil.game.board.tak.Piece;
import be.belegkarnil.game.board.tak.event.*;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class is a GUI component of the Game. It is the log panel ({@link InnerPanel}).
 *
 * @author Belegkarnil
 */
public class LogPanel extends InnerPanel implements ActionListener, GameListener, RoundListener, TurnListener{
	private final JTextArea content;

	public LogPanel(){
		setLayout(new BorderLayout());
		content = new JTextArea();
		content.setEditable(false);
		add(new JScrollPane(content), BorderLayout.CENTER);
		add(createMenu(), BorderLayout.SOUTH);
		content.setEditable(false);
		final Dimension dimension = new Dimension(200, 0);
		setMinimumSize(dimension);
		setPreferredSize(dimension);
	}

	@Override
	void register(Game game){
		if(game == null) return;
		game.addGameListener(this);
		game.addRoundListener(this);
		game.addTurnListener(this);
	}

	@Override
	void onPieceSelected(String name){

	}

	private JPanel createMenu(){
		final JPanel panel = new JPanel(new FlowLayout());
		final JButton export = new JButton("Export");
		export.addActionListener(this);
		panel.add(export);
		return panel;
	}

	@Override
	public void actionPerformed(ActionEvent e){
		System.out.println("TODO actionPerformed@" + getClass().getSimpleName());
	}

	@Override
	public void onGameBegins(GameEvent ge){
		content.setText("");
		content.append("Player1=" + ge.game.getFirstPlayer().getName() + "\n");
		content.append("Player2=" + ge.game.getSecondPlayer().getName() + "\n");
		content.append("Timeout=" + ge.game.getTimeout() + "\n");
		content.append("Skip limit=" + ge.game.getSkipLimit() + "\n");
		content.append("Winning rounds=" + ge.game.getWinningRounds() + "\n");
	}

	@Override
	public void onGameEnds(GameEvent ge){
		if(ge.winner != null){
			content.append("\nWinner is " + ge.winner.getName() + "\n");
		}else{
			content.append("\nNo winner\n");
		}
	}

	@Override
	public void onRoundBegins(RoundEvent re){
		content.append("\nRound " + re.round + "\n");
		content.append("Player1=" + re.startPlayer.getName() + "\n");
		content.append("Player2=" + re.opponent.getName() + "\n");
	}

	@Override
	public void onRoundEnds(RoundEvent re){
		if(re.winner != null){
			content.append("\nWinner is " + re.winner.getName() + "\n");
			content.append("Player1 score is " + re.startPlayer.getScore() + "\n");
			content.append("Player2 score is " + re.opponent.getScore() + "\n");
			if(re.reason != null) content.append("Reason is " + re.reason + "\n");
		}else{
			content.append("\nNo winner\n");
		}
	}

	@Override
	public void onTurnBegins(TurnEvent te){
		content.append("Turn " + te.turn + "\n");
	}

	@Override
	public void onTurnEnds(TurnEvent te){
		if(te.action != null){
			final Action action = te.action;

			if(action.isSkip()){
				content.append("Action=Skip\n");
			}else if(action.isPlace()){
				content.append("Action=Place, piece=" + action.piece.name() + ", at="+action.position+"\n");
			}else{
				content.append("Action=Move, from=" + action.position+", to=" + action.destination+ ", amount=[");
				final int[] amount =
						  action.getAmount();
				content.append(String.valueOf(amount[0]));
				for(int i=1; i<amount.length; i++){
					content.append(","+amount[i]);
				}
				content.append("]\n");
			}

		}else{
			content.append("Action=none\n");
		}
	}
/*
    @Override
    public void onNoAction(SkipEvent se) {
        content.append("Event: no action\n");
    }

    @Override
    public void onInvalidPiece(SkipEvent se) {
        content.append("Event: invalid piece\n");
    }

    @Override
    public void onInvalidPosition(SkipEvent se) {
        content.append("Event: invalid position\n");
    }

    @Override
    public void onTimeout(SkipEvent se) {
        content.append("Event: timeout\n");
    }

    @Override
    public void onException(SkipEvent se) {
        content.append("Event: Exception("+se.exception.getMessage()+")\n");
    }*/
}
