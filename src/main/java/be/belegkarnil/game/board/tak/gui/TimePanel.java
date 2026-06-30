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

import be.belegkarnil.game.board.tak.Game;
import be.belegkarnil.game.board.tak.event.TurnEvent;
import be.belegkarnil.game.board.tak.event.TurnListener;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class is a GUI component of the Game. It is the timeout panel ({@link InnerPanel}).
 *
 * @author Belegkarnil
 */
public class TimePanel extends InnerPanel{
	private int delay;
	private Timer timer;

	private int remaining;
	private final Object lock;
	private static final Color REMAINING_TIME = Color.GREEN.darker();
	private static final Color ELAPSED_TIME = Color.RED.darker();

	public TimePanel(){
		this.delay = -1;
		this.remaining = this.delay;
		this.lock = new Object();
		this.timer = null;
		final Dimension dim = new Dimension(0, 40);
		setBackground(ELAPSED_TIME);
		setPreferredSize(dim);
		setMinimumSize(dim);
		setMaximumSize(dim);
		setFont(getFont().deriveFont(Font.BOLD, 28));
	}

	@Override
	protected void paintComponent(Graphics g){
		super.paintComponent(g);
		final int value;
		synchronized(lock){
			value = remaining;
		}
		final Dimension dim = getSize();

		g.setColor(REMAINING_TIME);
		g.fillRect(0, 0, dim.width * value / delay, dim.height);

		if(value < 0) return;

		final String text = Integer.toString(value);
		g.setColor(Color.WHITE);
		g.drawString(text, (dim.width - g.getFontMetrics().stringWidth(text)) >> 1, 28);
	}

	@Override
	void register(Game game){
		if(game == null){
			timer.cancel();
			timer.purge();
			return;
		};

		this.delay = game.getTimeout();

		game.addTurnListener(new TurnListener(){
			@Override
			public void onTurnBegins(TurnEvent te){
				final TimerTask task = new TimerTask(){
					public void run(){
						synchronized(lock){
							remaining--;
						}
						repaint();
					}
				};
				synchronized(lock){
					remaining = delay;
				}
				timer = new Timer();
				timer.scheduleAtFixedRate(task, 1000, 1000);
				repaint();
			}

			@Override
			public void onTurnEnds(TurnEvent te){
				timer.cancel();
				timer.purge();
			}
		});
	}

	@Override
	void onPieceSelected(String name){
	}
}
