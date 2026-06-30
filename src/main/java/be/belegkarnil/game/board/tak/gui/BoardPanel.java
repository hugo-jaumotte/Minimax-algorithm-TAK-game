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
import be.belegkarnil.game.board.tak.Piece;
import be.belegkarnil.game.board.tak.Player;
import be.belegkarnil.game.board.tak.Constants;
import be.belegkarnil.game.board.tak.event.GameAdapter;
import be.belegkarnil.game.board.tak.event.GameEvent;
import be.belegkarnil.game.board.tak.event.TurnAdapter;
import be.belegkarnil.game.board.tak.event.TurnEvent;
import be.belegkarnil.game.board.tak.event.RoundAdapter;
import be.belegkarnil.game.board.tak.event.RoundEvent;
import be.belegkarnil.game.board.tak.strategy.HMIStrategy;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This class is a GUI component of the Game. It is the board panel ({@link InnerPanel}).
 *
 * @author Belegkarnil
 */
public class BoardPanel extends InnerPanel implements MouseListener, MouseMotionListener{
	public static final int BORDER_THICKNESS = 2;
	public static final Color BORDER = Color.BLACK;
	private static final Piece NO_PIECE_DEFINED = null;
	private static final Point NO_SOURCE_DEFINED = null;

	private static final Comparator<Point> RIGHT_COMPARATOR = new Comparator<Point>() {
		@Override
		public int compare(Point a, Point b){
			return a.x - b.x; // Leftmost first
		}
	};
	private static final Comparator<Point> LEFT_COMPARATOR = new Comparator<Point>() {
		@Override
		public int compare(Point a, Point b){
			return b.x - a.x; // Rightmost first
		}
	};
	private static final Comparator<Point> UP_COMPARATOR = new Comparator<Point>() {
		@Override
		public int compare(Point a, Point b){
			return b.y - a.y; // Bottommost first
		}
	};
	private static final Comparator<Point> DOWN_COMPARATOR = new Comparator<Point>() {
		@Override
		public int compare(Point a, Point b){
			return a.y - b.y; // Topmost first
		}
	};

	private Board board;
	private CellDrawer[][] cells;
	private JPanel preview;

	private Piece currentPiece;
	private Map<Point,Integer> currentMove;
	private Point currentSrc;
	private Player hmi;
	private final Object lock;
	private Color color;


	public BoardPanel(){
		this.hmi					= null;
		this.board				= new Board(SettingsPanel.DEFAULT_BOARD_SIZE);
		this.lock				= new Object();
		this.color				= Constants.FIRST_PLAYER;
		this.currentPiece		= NO_PIECE_DEFINED;
		this.currentSrc		= NO_SOURCE_DEFINED;
		this.currentMove		= new HashMap<Point,Integer>();

		clearContent();
		createContent();

		final Dimension size = new Dimension(600, 600);
		setPreferredSize(size);
		setMinimumSize(size);

		addMouseListener(this);
		addMouseMotionListener(this);
	}
	private void clearContent(){
		removeAll();
	}
	private void createContent(){
		setLayout(new BorderLayout());

		final Dimension size = new Dimension(CellDrawer.DEFAULT_CELL_SIZE << 1, CellDrawer.DEFAULT_CELL_SIZE);
		preview = new JPanel();
		preview.setSize(size);
		preview.setMinimumSize(size);
		preview.setPreferredSize(size);
		add(preview, BorderLayout.WEST);

		JPanel content = new JPanel(new GridBagLayout());
		content.setBackground(BORDER);

		GridBagConstraints c = new GridBagConstraints();
		c.fill	= GridBagConstraints.HORIZONTAL;

		cells = new CellDrawer[this.board.getSize()][this.board.getSize()];
		for(int y=0; y<this.cells.length;y++){
			for(int x=0; x<this.cells[y].length;x++){
				cells[y][x] = createCell();
				cells[y][x].addMouseListener(this);
				c.gridx = x;
				c.gridy = y;
				content.add(cells[y][x],c);
			}
		}
		content.setBorder(BorderFactory.createLineBorder(Color.black,BORDER_THICKNESS));

		final JPanel resizable = new JPanel(new FlowLayout());
		resizable.add(content);
		add(resizable,BorderLayout.CENTER);

		resizable.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				final int boardSize = BoardPanel.this.board.getSize();
				int size = Math.min(resizable.getWidth(), resizable.getHeight()) / board.getSize() - 1;
				content.setSize(size*boardSize,size*boardSize);
				for(JPanel[] row:cells)
					for(JPanel cell:row) {
						cell.setPreferredSize(new Dimension(size, size));
						cell.setSize(size, size);
					}
				content.revalidate();
			}
		});

		this.revalidate();
		this.repaint();
	}

	private CellDrawer createCell(){
		CellDrawer panel = new CellDrawer();
		panel.setBorder(BorderFactory.createLineBorder(Color.BLACK,1));
		return panel;
	}

	public Point getCellPosition(/*Point panelPoint*/ Component component){ // x,y => i,j
		for(int row = 0; row < cells.length; row++){
			for(int col = 0; col < cells[row].length; col++){
				if(cells[row][col] == component)
					return new Point(col, row);
			}
		}
		return null;
	}

	public void reset(){
		this.currentPiece = NO_PIECE_DEFINED;
		this.currentSrc	= NO_SOURCE_DEFINED;

		for(int row=0; row<cells.length; row++){
			for(int col=0; col<cells[row].length; col++){
				update(row, col);
			}
		}

		this.currentMove.clear();
	}

	@Override
	public void mouseClicked(MouseEvent e){
		final Point pos = getCellPosition(e.getComponent());

		if(pos == null){
			resetCurrentAction();
		}else if(currentPiece == NO_PIECE_DEFINED){
			// No piece already defined => Move action
			if(e.getButton() == MouseEvent.BUTTON1){ // Left click => increment counter on path
				if(e.getClickCount() < 2){ // Single click
					if(this.currentSrc == NO_SOURCE_DEFINED){ // first define the src
						this.currentSrc = pos;
						this.cells[pos.y][pos.x].markAsSource();
					}else{ // then the path
						if(pos.equals(this.currentSrc)){
							// Cannot override the source
							JOptionPane.showMessageDialog(getParent(), "The source cannot be override","Source error",JOptionPane.WARNING_MESSAGE);
						}else{
							if(!this.currentMove.containsKey(pos)) this.currentMove.put(pos, Integer.valueOf(0));
							final int newValue = this.currentMove.get(pos).intValue() + 1;
							this.currentMove.put(pos, newValue);
							this.cells[pos.y][pos.x].markAsPath(newValue);
						}
					}
				}else if(e.getClickCount() == 2){ // Double click => define the end of move and send
					if(this.currentSrc == NO_SOURCE_DEFINED){
						// src must be defined
						JOptionPane.showMessageDialog(getParent(), "The source position must be defined","Source error",JOptionPane.WARNING_MESSAGE);
					}else if(!this.currentMove.containsKey(pos)){
						// dst must be part of the path
						JOptionPane.showMessageDialog(getParent(), "The destination position must be part of the path","Source error",JOptionPane.WARNING_MESSAGE);
					}else if(this.currentMove.size() < 1){
						// path has no size
						JOptionPane.showMessageDialog(getParent(), "The path must have a size bigger than 0","Source error",JOptionPane.WARNING_MESSAGE);
					}else{
						// Last click, define the move
						int[] amount = new int[this.currentMove.size()];
						Comparator<Point> comparator;
						if(currentSrc.y == pos.y){
							comparator = currentSrc.x < pos.x ? RIGHT_COMPARATOR : LEFT_COMPARATOR;
						}else{
							comparator = currentSrc.y < pos.y ? DOWN_COMPARATOR : UP_COMPARATOR;
						}
						SortedSet<Point> positions = new TreeSet(comparator);
						positions.addAll(this.currentMove.keySet());
						int i = 0;
						for(Point p : positions){
							amount[i] = currentMove.get(p).intValue();
							i++;
						}

						Color color = null;
						synchronized(lock){
							if(this.hmi != null) color = this.hmi.getColor();
						}
						if(board.canMove(color, currentSrc, amount, pos)){
							synchronized(lock){
								if(this.hmi != null)
									((HMIStrategy) (this.hmi.getStrategy())).setMoveAction(currentSrc,pos,amount);
							}
						}else{
							JOptionPane.showMessageDialog(getParent(), "The move is not allowed","Invalid move",JOptionPane.WARNING_MESSAGE);
						}
						resetCurrentAction();

					}
				}
			}else{ // Right click => decrement counter on path
				if(pos.equals(this.currentSrc)){
					// Cannot override the source
					JOptionPane.showMessageDialog(getParent(), "The source cannot be override","Source error",JOptionPane.WARNING_MESSAGE);
				}else if(this.currentMove.containsKey(pos)){
					final int newValue = this.currentMove.get(pos).intValue()-1;
					this.currentMove.put(pos,Integer.valueOf(newValue));
					if(newValue > 0) this.cells[pos.y][pos.x].markAsPath(newValue);
					else update(pos.y,pos.x);
				}
			}
		}else{// Piece defined => Place action
			if(e.getButton() == MouseEvent.BUTTON1){ // Left click => Place
				if(board.canPlace(this.currentPiece, pos)){
					synchronized(lock){
						if(this.hmi != null)
							((HMIStrategy) (this.hmi.getStrategy())).setPiece(this.currentPiece, pos);
					}
					resetCurrentAction();
				}else{
					JOptionPane.showMessageDialog(getParent(), "The piece cannot be set at that position","Invalid place",JOptionPane.WARNING_MESSAGE);
				}
			}else{ // Other mouse button, reset action
				resetCurrentAction();
			}
		}
		this.repaint();
	}

	private void resetCurrentAction(){
		if(currentSrc != NO_SOURCE_DEFINED){
			update(currentSrc);
		}
		currentPiece	= NO_PIECE_DEFINED;
		currentSrc		= NO_SOURCE_DEFINED;
		for(Point pos: currentMove.keySet()){
			update(pos);
		}
		currentMove.clear();
	}

	private void update(Point pos){
		update(pos.y,pos.x);
	}
	private void update(int row, int col){
		final Piece piece = this.board.getTop(row,col);
		if(piece == NO_PIECE_DEFINED){
			this.cells[row][col].markAsEmpty();
		}else{
			this.cells[row][col].markAs(piece);
		}
		this.cells[row][col].repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e){}

	@Override
	public void mousePressed(MouseEvent e){}

	@Override
	public void mouseReleased(MouseEvent e){}

	@Override
	public void mouseEntered(MouseEvent e){
		final Point pos = getCellPosition(e.getComponent());
		if(pos == null) return;
		drawPreview(this.board.getStack(pos));
	}

	private void drawPreview(Piece[] stack){
		preview.setLayout(new BoxLayout(preview, BoxLayout.Y_AXIS));
		final Dimension size = new Dimension(CellDrawer.DEFAULT_CELL_SIZE, CellDrawer.DEFAULT_CELL_SIZE);
		for(Piece piece:stack){
			final JLabel button = new JLabel(CellDrawer.createIcon(piece));
			button.setPreferredSize(size);
			button.setMinimumSize(size);
			preview.add(button);
		}
		preview.revalidate();
		preview.repaint();
	}

	@Override
	public void mouseExited(MouseEvent e){
		clearPreview();
	}

	private void clearPreview(){
		preview.removeAll();
	}

	@Override
	public void mouseDragged(MouseEvent e){

	}

	@Override
	void register(Game game){
		if(game == null){
			reset();
			return;
		}


		game.addGameListener(new GameAdapter(){
			@Override
			public void onGameBegins(GameEvent re){
				clearContent();
				BoardPanel.this.board = re.game.getBoard();
				createContent();
			}
		});

		game.addRoundListener(new RoundAdapter(){
			@Override
			public void onRoundBegins(RoundEvent re){
				reset();
			}
		});

		game.addTurnListener(new TurnAdapter(){
			@Override
			public void onTurnBegins(TurnEvent te){
				color = (te.turn % 2 == 0 ? Constants.FIRST_PLAYER : Constants.SECOND_PLAYER);
				currentPiece	= NO_PIECE_DEFINED;
				currentSrc		= NO_SOURCE_DEFINED;
				if(te.current.getStrategy() instanceof HMIStrategy){
					synchronized(lock){
						hmi = te.current;
					}
				}
			}

			@Override
			public void onTurnEnds(TurnEvent te){
				reset();
				synchronized(lock){
					hmi = null;
				}
				if(te.action != null && !te.action.isSkip()){
					update(te.action.position);
				}
				BoardPanel.this.repaint();
			}
		});
	}

	@Override
	void onPieceSelected(String name){
		synchronized(lock){
			if(hmi != null){
				Piece piece = null;
				try{
					piece = Piece.valueOf(name);
				}catch(IllegalArgumentException e){
					// Button (skip)
				}
				if(piece == NO_PIECE_DEFINED){
					((HMIStrategy) (this.hmi.getStrategy())).setSkipAction();
					this.currentPiece = NO_PIECE_DEFINED;
					this.currentSrc	= NO_SOURCE_DEFINED;
					this.currentMove.clear();
				}else{
					this.currentPiece = piece;
				}
			}
		}
		repaint();
	}
}
