package be.belegkarnil.game.board.tak.gui;

import be.belegkarnil.game.board.tak.Constants;
import be.belegkarnil.game.board.tak.Piece;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

public class CellDrawer extends JPanel{
	public static final int DEFAULT_CELL_SIZE = 40;
	public static final Color BACKGROUND_COLOR = Color.WHITE;

	private static final Dimension CELL_SIZE = new Dimension(DEFAULT_CELL_SIZE, DEFAULT_CELL_SIZE);
	private static final Color SOURCE_COLOR = Color.ORANGE;
	private static final Color PATH_COLOR = Color.BLACK;
	private static final Color NO_COLOR = Color.WHITE;
	private static final Color TRANSPARENT_COLOR = new Color(0,0,0,0);
	private static final Shape NO_SHAPE = null;
	private static final int NO_AMOUNT = -1;
	private static final Font DEFAULT_FONT = new JLabel().getFont().deriveFont(Font.BOLD, 28);

	private Color color;
	private Shape shape;
	private int amount;

	private Object lock = new Object();

	public CellDrawer(){
		this.shape	= NO_SHAPE;
		this.color	= NO_COLOR;
		this.amount	= NO_AMOUNT;
		setSize(CELL_SIZE);
		setPreferredSize(CELL_SIZE);
	}

	public void markAsSource(){
		synchronized(lock){
			this.color	= SOURCE_COLOR;
			this.amount	= NO_AMOUNT;
			this.shape	= createDolmenShape(getWidth(),getHeight());
		}
	}

	public void markAsEmpty(){
		synchronized(lock){
			this.color = NO_COLOR;
			this.amount = NO_AMOUNT;
			this.shape = createDolmenShape(getWidth(),getHeight());
		}
	}

	public void markAsPath(int amount){
		synchronized(lock){
			this.color = PATH_COLOR;
			this.shape = createDolmenShape(getWidth(),getHeight());
			this.amount = amount > 0 ? amount : NO_AMOUNT;
		}
	}

	public void markAs(Piece piece){
		synchronized(lock){
			switch(piece){
				case DOLMEN_BLACK:
				case MENHIR_BLACK:
				case CAPSTONE_BLACK:
					this.color = Constants.BLACK_PLAYER;
					break;
				default:
					this.color = Constants.WHITE_PLAYER;
					break;
			}
			switch(piece){
				case CAPSTONE_BLACK:
				case CAPSTONE_WHITE:
					this.shape = createCapstoneShape(/*getWidth(),getHeight()*/);
					break;
				case DOLMEN_BLACK:
				case DOLMEN_WHITE:
					this.shape = createDolmenShape(/*getWidth(),getHeight()*/);
					break;
				default:
					this.shape = createMenhirShape(/*getWidth(),getHeight()*/);
					break;
			}
			this.amount = NO_AMOUNT;
		}
	}

	@Override
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;

		final int width = getWidth();	
		final int height = getHeight();

		double scaling = Math.min(width / CELL_SIZE.getWidth(), height / CELL_SIZE.getHeight());
		final AffineTransform transform = AffineTransform.getScaleInstance(scaling,scaling);

		synchronized(lock){
			final Shape scaledShape = transform.createTransformedShape(shape);
			draw(g2d,BACKGROUND_COLOR,color,scaledShape,amount,width,height);
		}
	}

	private static void draw(Graphics2D g2d,Color bgColor,Color color,Shape shape, int amount,int maxWidth,int maxHeight){
		g2d.setBackground(bgColor);
		g2d.clearRect(0, 0, maxWidth, maxHeight);

		Color fontColor = bgColor.equals(Color.BLACK) ? Color.WHITE : Color.BLACK;

		if(shape != NO_SHAPE){
			g2d.setColor(color);
			g2d.fill(shape);
			fontColor = color.equals(Color.BLACK) ? Color.WHITE : Color.BLACK;
		}

		if(amount != NO_AMOUNT){
			g2d.setColor(fontColor);
			final String text = String.valueOf(amount);
			g2d.setFont(DEFAULT_FONT);
			final int width = g2d.getFontMetrics().stringWidth(text);
			g2d.drawString(text, (maxWidth-width)>>1, (maxHeight)>>1);
		}
	}

	private static Icon createIcon(Color color,Shape shape, int amount,int maxWidth,int maxHeight){
		BufferedImage image = new BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = image.createGraphics();
		draw(g2d, TRANSPARENT_COLOR,color, shape, amount, maxWidth, maxHeight);
		return new ImageIcon(image);
	}
	public static Icon createIcon(Piece piece){
		switch(piece){
			case MENHIR_BLACK:	return createMenhirIcon(Constants.BLACK_PLAYER,NO_AMOUNT,DEFAULT_CELL_SIZE,DEFAULT_CELL_SIZE);
			case MENHIR_WHITE:	return createMenhirIcon(Constants.WHITE_PLAYER,NO_AMOUNT,DEFAULT_CELL_SIZE,DEFAULT_CELL_SIZE);
			case DOLMEN_BLACK:	return createMenhirIcon(Constants.BLACK_PLAYER,NO_AMOUNT,DEFAULT_CELL_SIZE,DEFAULT_CELL_SIZE);
			case DOLMEN_WHITE:	return createMenhirIcon(Constants.WHITE_PLAYER,NO_AMOUNT,DEFAULT_CELL_SIZE,DEFAULT_CELL_SIZE);
			case CAPSTONE_BLACK:		return createMenhirIcon(Constants.BLACK_PLAYER,NO_AMOUNT,DEFAULT_CELL_SIZE,DEFAULT_CELL_SIZE);
			case CAPSTONE_WHITE:		return createMenhirIcon(Constants.WHITE_PLAYER,NO_AMOUNT,DEFAULT_CELL_SIZE,DEFAULT_CELL_SIZE);
		}
		return null;
	}
	public static Icon createCapstoneIcon(Color color, int amount, int width, int height){
		return createIcon(color, createCapstoneShape(width,height),amount,width,height);
	}
	public static Icon createMenhirIcon(Color color,int amount,int width,int height){
		return createIcon(color,createMenhirShape(width,height),amount,width,height);
	}

	public static Icon createDolmenIcon(Color color,int amount,int width,int height){
		return createIcon(color,createDolmenShape(width,height),amount,width,height);
	}

	private static Shape createDolmenShape(int width,int height){
		return new Rectangle(width,height);
	}
	private static Shape createCapstoneShape(int width, int height){
		final int centerX = width >> 1;
		final int centerY = height >> 1;
		final int radius = Math.min(height, width)>>1;
		return new Ellipse2D.Double(centerX - radius, centerY - radius, radius<<1, radius<<1);
	}
	private static Shape createMenhirShape(int width,int height){
		return new Rectangle(width>>2,0, width>>1,height);
	}
	private static Shape createCapstoneShape(){
		return createCapstoneShape(CELL_SIZE.width,CELL_SIZE.height);
	}
	private static Shape createMenhirShape(){
		return createMenhirShape(CELL_SIZE.width,CELL_SIZE.height);
	}
	private static Shape createDolmenShape(){
		return createDolmenShape(CELL_SIZE.width,CELL_SIZE.height);
	}
}
