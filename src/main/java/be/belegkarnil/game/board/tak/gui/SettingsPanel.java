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

import be.belegkarnil.game.board.tak.BelegTak;
import be.belegkarnil.game.board.tak.Board;
import be.belegkarnil.game.board.tak.Game;
import be.belegkarnil.game.board.tak.Player;
import be.belegkarnil.game.board.tak.event.TurnAdapter;
import be.belegkarnil.game.board.tak.strategy.Strategy;
import be.belegkarnil.game.board.tak.event.GameAdapter;
import be.belegkarnil.game.board.tak.event.GameEvent;
import be.belegkarnil.game.board.tak.event.TurnEvent;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * This class is a GUI component of the Game. It is the settings panel ({@link InnerPanel}).
 *
 * @author Belegkarnil
 */
public class SettingsPanel extends InnerPanel implements ActionListener{
	public static final Board.Size DEFAULT_BOARD_SIZE = Board.Size.HUGE;

	private static final String START_GAME_COMMAND = "STAT_GAME";
	private static final String STOP_GAME_COMMAND = "STOP_GAME";
	private static final String SELECT_FIRST_PLAYER_COMMAND = "SELECT_FIRST_PLAYER";
	private static final String SELECT_SECOND_PLAYER_COMMAND = "SELECT_SECOND_PLAYER";
	private static final String SELECT_SIZE_COMMAND				= "SELECT_SIZE_COMMAND";
	private JComboBox firstPlayerName, secondPlayerName, sizes;
	private JTextField timeout, winningRounds, skip, penality;
	private JSlider speed;
	private JButton play, stop;
	private ExecutorService executor;
	private Future thread;
	private Object threadLock = new Object();

	public SettingsPanel(){
		this.executor = Executors.newSingleThreadExecutor();
		this.thread = null;
		setLayout(new FlowLayout());
		createStrategyPanel();
		createConfigurationPanel();
		createActionPanel();
	}

	private void checkFinished(){
		boolean checkAgain = true;
		synchronized(threadLock){
			if(this.thread != null && this.thread.isDone()){
				checkAgain = false;
				freeze(false);
				initGame(null);
			}
		}
		if(checkAgain){
			new Timer().schedule(new TimerTask(){
				@Override
				public void run(){
					SettingsPanel.this.checkFinished();
				}
			}, 500);
		}
	}

	private void freeze(boolean freeze){
		this.stop.setEnabled(freeze);
		freeze = !freeze;

		this.play.setEnabled(freeze);
		this.firstPlayerName.setEnabled(freeze);
		this.secondPlayerName.setEnabled(freeze);
		this.sizes.setEnabled(freeze);
		this.timeout.setEnabled(freeze);
		this.winningRounds.setEnabled(freeze);
		this.skip.setEnabled(freeze);
		this.penality.setEnabled(freeze);
	}

	@Override
	void register(Game game){
		if(game == null) return;
		game.addGameListener(new GameAdapter(){
			@Override
			public void onGameEnds(GameEvent ge){
				checkFinished();
			}
		});
		game.addTurnListener(new TurnAdapter(){
			@Override
			public void onTurnEnds(TurnEvent te){
				int delay = speed.getValue() * 1000;
				if(delay < 1) return;
				try{
					Thread.sleep(delay);
				}catch(InterruptedException e){
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	void onPieceSelected(String name){

	}

	private void createStrategyPanel(){
		//final JPanel panel = new JPanel(new GridLayout(2,2));
		final JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateGaps(true);

		panel.setBorder(BorderFactory.createTitledBorder("Strategies"));
		final JLabel first = new JLabel("First player:");
		final JLabel second = new JLabel("Second player:");

		firstPlayerName = new JComboBox(BelegTak.listStrategies());
		secondPlayerName = new JComboBox(BelegTak.listStrategies());
		firstPlayerName.setActionCommand(SELECT_FIRST_PLAYER_COMMAND);
		secondPlayerName.setActionCommand(SELECT_SECOND_PLAYER_COMMAND);

		GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
		GroupLayout.Group yLabelGroup = layout.createParallelGroup(GroupLayout.Alignment.TRAILING);
		hGroup.addGroup(yLabelGroup);

		GroupLayout.Group yFieldGroup = layout.createParallelGroup();
		hGroup.addGroup(yFieldGroup);
		layout.setHorizontalGroup(hGroup);

		GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
		layout.setVerticalGroup(vGroup);

		yLabelGroup.addComponent(first);
		yLabelGroup.addComponent(second);

		yFieldGroup.addComponent(firstPlayerName);
		yFieldGroup.addComponent(secondPlayerName);

		vGroup.addGroup(layout.createParallelGroup().addComponent(first).addComponent(firstPlayerName));
		vGroup.addGroup(layout.createParallelGroup().addComponent(second).addComponent(secondPlayerName));

		add(panel);
	}

	private void createConfigurationPanel(){
		final JPanel config = new JPanel(new BorderLayout());

		final JPanel panel = new JPanel(new GridLayout(2, 5));
		panel.setBorder(BorderFactory.createTitledBorder("Settings"));
		panel.add(new JLabel("Timeout (s):"));
		panel.add(new JLabel("# Winning rounds:"));
		panel.add(new JLabel("Skip limit:"));
		panel.add(new JLabel("Skip penality:"));
		panel.add(new JLabel("Size:"));

		timeout = new JTextField(String.valueOf(Game.DEFAULT_TIMEOUT));
		winningRounds = new JTextField(String.valueOf(Game.DEFAULT_NUMBER_OF_WINNING_ROUNDS));
		skip = new JTextField(String.valueOf(Game.DEFAULT_SKIP_LIMIT));
		penality = new JTextField(String.valueOf(Game.DEFAULT_SKIP_PENALTY));
		sizes = new JComboBox(Board.Size.values());
		sizes.setActionCommand(SELECT_SIZE_COMMAND);
		sizes.setSelectedItem(DEFAULT_BOARD_SIZE);

		panel.add(timeout);
		panel.add(winningRounds);
		panel.add(skip);
		panel.add(penality);
		panel.add(sizes);

		final JPanel speed = new JPanel(new BorderLayout());
		speed.setBorder(BorderFactory.createTitledBorder("Idle delay"));
		this.speed = new JSlider(0, 30, 0);
		this.speed.setPaintTrack(true);
		this.speed.setPaintTicks(true);
		this.speed.setPaintLabels(true);
		this.speed.setMajorTickSpacing(5);
		this.speed.setMinorTickSpacing(1);
		speed.add(this.speed, BorderLayout.CENTER);

		final JPanel size = new JPanel(new FlowLayout());

		config.add(panel, BorderLayout.CENTER);
		config.add(speed, BorderLayout.SOUTH);
		add(config);
	}

	private void createActionPanel(){
		final JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("Actions"));
		play = new JButton("Play");
		stop = new JButton("Stop");
		play.setActionCommand(START_GAME_COMMAND);
		play.addActionListener(this);
		stop.setActionCommand(STOP_GAME_COMMAND);
		stop.addActionListener(this);
		stop.setEnabled(false);
		panel.add(stop);
		panel.add(play);
		add(panel);
	}

	@Override
	public void actionPerformed(ActionEvent ae){
		switch(ae.getActionCommand()){
			case START_GAME_COMMAND:
				startGame();
				break;
			case STOP_GAME_COMMAND:
				stopGame();
				break;
		}
	}

	private void stopGame(){
		this.stop.setEnabled(false); // Avoid duplicate calls
		synchronized(threadLock){
			if(this.thread != null)
				this.thread.cancel(true);
		}
		checkFinished();
	}

	private void startGame(){
		int timeout = -1;
		int winningRounds = -1;
		int skip = -1;
		int penality = -1;

		try{
			timeout = Integer.parseInt(this.timeout.getText());
		}catch(Exception e){
			JOptionPane.showMessageDialog(this, "Timeout setting must be an integer", "Parsing error", JOptionPane.ERROR_MESSAGE);
		}
		try{
			winningRounds = Integer.parseInt(this.winningRounds.getText());
		}catch(Exception e){
			JOptionPane.showMessageDialog(this, "Number of winning rounds setting must be an integer", "Parsing error", JOptionPane.ERROR_MESSAGE);
		}
		try{
			skip = Integer.parseInt(this.skip.getText());
		}catch(Exception e){
			JOptionPane.showMessageDialog(this, "Skip setting must be an integer", "Parsing error", JOptionPane.ERROR_MESSAGE);
		}
		try{
			penality = Integer.parseInt(this.penality.getText());
		}catch(Exception e){
			JOptionPane.showMessageDialog(this, "Penality setting must be an integer", "Parsing error", JOptionPane.ERROR_MESSAGE);
		}

		if(timeout <= 0 || winningRounds <= 0 || skip <= 0 || penality < 0){
			JOptionPane.showMessageDialog(this, "Setting must be an integer greater than 0", "Parsing error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Create players
		Strategy firstStrategy = null, secondStrategy = null;
		try{
			Class<Strategy> klass = (Class<Strategy>) (firstPlayerName.getSelectedItem());
			Constructor<?> constructor = klass.getConstructor();
			firstStrategy = (Strategy) constructor.newInstance();
		}catch(Exception e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(getParent(),"Cannot instanciante "+firstPlayerName.getSelectedItem()+" class", "Strategy error", JOptionPane.ERROR_MESSAGE);
		}
		try{
			Class<Strategy> klass = (Class<Strategy>) (secondPlayerName.getSelectedItem());
			Constructor<?> constructor = klass.getConstructor();
			secondStrategy = (Strategy) constructor.newInstance();
		}catch(Exception e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(getParent(),"Cannot instanciante "+secondPlayerName.getSelectedItem()+" class", "Strategy error", JOptionPane.ERROR_MESSAGE);
		}
		if(firstStrategy == null || secondStrategy == null) return;

		// create the game and pass it to Game class
		final Player first = new Player(firstStrategy.getClass().getName(), firstStrategy);
		final Player second = new Player(secondStrategy.getClass().getName(), secondStrategy);
		final Game game = new Game(new Board((Board.Size) this.sizes.getSelectedItem()), first, second, timeout, winningRounds, skip, penality);

		// freeze GUI settings
		freeze(true);

		InnerPanel.initGame(game);

		synchronized(threadLock){
			this.thread = this.executor.submit(game);
		}
	}
}
