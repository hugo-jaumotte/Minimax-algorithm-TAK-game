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

import be.belegkarnil.game.board.tak.event.*;

import java.awt.Color;
import java.awt.Point;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The class implements the behavior of the Spectrangle game (a modified two players version with skip and penalities)
 *
 * @author Belegkarnil
 */
public class Game implements Runnable{
	/**
	 * Is an enumeration that express all the winning reasons
	 */
	public static enum WinningReason{
		/**
		 * Occurs when a player has skip too much times (or bad {@link Action})
		 */
		SKIP_LIMIT,
		/**
		 * Occurs when a player completed a path (bottom-up, left-right)
		 */
		PATH_COMPLETED,
		/**
		 * Occurs when players need a tie, more dolmens (see {@link Piece}) imply win.
		 */
		DOLMEN_TIE,
		/**
		 * Occurs when players need a tie and they have same number of dolmens. Second player always wins.
		 */
		TIE_SECOND_PLAYER
	};
	/**
	 * is the maximum of allowed skip before considering to stop the game
	 */
	public static final int DEFAULT_SKIP_LIMIT = 3;
	/**
	 * is the default maximum number of seconds that each players has per turn
	 */
	public static final int DEFAULT_TIMEOUT = 60;
	/**
	 * is the number of rounds to win in order to win the whole game
	 */
	public static final int DEFAULT_NUMBER_OF_WINNING_ROUNDS = 2;
	/**
	 * is the scoring penalty if a player can put a {@link Piece} but he does not
	 */
	public static final int DEFAULT_SKIP_PENALTY = 30;

	private final int timeout, numWinningRounds, skipLimit, skipPenalty;
	private int turn, round;
	private Player[] players;
	private List<GameListener> gameListeners;
	private List<RoundListener> roundListeners;
	private List<TurnListener> turnListeners;
	private List<MisdesignListener> misdesignListeners;
	private Board board;

	/**
	 * Construct a game with default settings ({@link Game#DEFAULT_TIMEOUT}, {@link Game#DEFAULT_NUMBER_OF_WINNING_ROUNDS}, {@link Game#DEFAULT_SKIP_LIMIT}, and {@link Game#DEFAULT_SKIP_PENALTY})
	 *
	 * @param board   The board that the game will use
	 * @param player1 The first player involved in the game
	 * @param player2 The second player involved in the game
	 */
	public Game(Board board, Player player1, Player player2){
		this(board, player1, player2, DEFAULT_TIMEOUT, DEFAULT_NUMBER_OF_WINNING_ROUNDS, DEFAULT_SKIP_LIMIT, DEFAULT_SKIP_PENALTY);
	}

	/**
	 * Construct a game with custom settings
	 *
	 * @param board            The board that the game will use
	 * @param player1          The first player involved in the game
	 * @param player2          The second player involved in the game
	 * @param timeout          The custom timeout settings (time in seconds a player has per turn)
	 * @param numWinningRounds The number of rounds has to win in order to win the game
	 * @param skipLimit        The number of skip turns before to stop the game
	 * @param skipPenality     The scoring penality if a player can play but he does not
	 */
	public Game(Board board, Player player1, Player player2, int timeout, int numWinningRounds, int skipLimit, int skipPenality){
		this.board = board;
		this.players = new Player[]{player1, player2};
		this.timeout = timeout;
		this.numWinningRounds = numWinningRounds;
		this.skipLimit = skipLimit;
		this.skipPenalty = skipPenality;
		this.round = 0;
		this.turn = 0;

		gameListeners = new LinkedList<GameListener>();
		roundListeners = new LinkedList<RoundListener>();
		turnListeners = new LinkedList<TurnListener>();
		misdesignListeners = new LinkedList<MisdesignListener>();

	}

	/**
	 * Get the first player involved in the game
	 *
	 * @return the first player
	 */
	public Player getFirstPlayer(){
		return players[0];
	}

	/**
	 * Get the second player involved in the game
	 *
	 * @return the second player
	 */
	public Player getSecondPlayer(){
		return players[1];
	}

	/**
	 * Get the number of second a player has to select an {@link Action}
	 *
	 * @return the number of seconds to define a timeout
	 */
	public int getTimeout(){
		return timeout;
	}

	/**
	 * Get the number of skip turns to stop the game
	 *
	 * @return the number of skip turns
	 */
	public int getSkipLimit(){
		return skipLimit;
	}

	/**
	 * Get the number of rounds to be won in order to win the game
	 *
	 * @return the number of winning rounds
	 */
	public int getWinningRounds(){
		return numWinningRounds;
	}

	/**
	 * Get the current round identifier (counter)
	 *
	 * @return the round identifier
	 */
	public int getRound(){
		return round;
	}

	/**
	 * Get the current turn identifier (counter)
	 *
	 * @return the turn identifier
	 */
	public int getTurn(){
		return turn;
	}

	/**
	 * Execute the game mechanic of a single turn: handle events, scoring, and players
	 */
	protected void executeTurn(){
		final Player current			= players[turn & 1];
		final Player opponent		= players[1 - (turn & 1)];
		fireTurnBegins(new TurnEvent(this, current, opponent, round, turn));

		Action action = null;
		boolean readAction = true;
		final StrategyTask task = new StrategyTask(current, board, opponent);
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future future = executor.submit(task);
		try{
			future.get(timeout, TimeUnit.SECONDS);
		}catch(TimeoutException e){
			future.cancel(true);
			readAction = false;
			current.skip();
			fireTimeout(new MisdesignEvent(current, board));
		}catch(InterruptedException ie){
			Thread.currentThread().interrupt();
			return;
		}catch(Exception e){
			readAction = false;
			current.skip();
			fireException(new MisdesignEvent(current, board, e));
		}finally{
			executor.shutdownNow();
			if(readAction){
				action = task.getAction();
			}
		}
		boolean penality = false;
		if(action != null && !action.isSkip()){
			if(action.isPlace()){
				if(!current.hasPiece(action.piece)){ // cheat, piece is not available
					final Piece tmp = action.piece;
					action = null;
					current.skip();
					penality = true;
					fireInvalidPiece(new MisdesignEvent(current, board, tmp));
				}else{
					if(board.canPlace(action.piece, action.position)){// allowed move
						Color winner = board.place(action.piece, action.position);
						current.plays(action.piece);
						if(winner != null){
							final int boardSize = board.getSize();
							if(winner == current.getColor())
								current.setScore(boardSize * boardSize + current.countPieces());
							else
								opponent.setScore(boardSize * boardSize + opponent.countPieces());
						}
					}else{// kind of cheat, place is not allowed
						final Piece tmp = action.piece; // cheat
						final Point pos = action.position;
						action = null;
						current.skip();
						penality = true;
						fireInvalidAction(new MisdesignEvent(current, board, tmp, pos));
					}
				}
			}else if(action.isMove()){
				if(board.canMove(current.getColor(),action.position, action.amount, action.destination)){// allowed move
					Color winner = board.move(action.position, action.amount, action.destination);
					if(winner != null){
						final int boardSize = board.getSize();
						if(winner == current.getColor())
							current.setScore(boardSize * boardSize + current.countPieces());
						else
							opponent.setScore(boardSize * boardSize + opponent.countPieces());
					}
				}else{// kind of cheat, place is not allowed
					final Piece tmp = action.piece; // cheat
					final Point pos = action.position;
					action = null;
					current.skip();
					penality = true;
					fireInvalidAction(new MisdesignEvent(current, board, tmp, pos));
				}
			}
		}else if(readAction){// action is null || action.isSkip()
			if(canPlay(board, current)){ // player can play but does not
				current.skip();
				penality = true;
			}else{
				current.skip();
			}
		}

		if(penality){
			current.setScore(current.getScore() - skipPenalty);
		}

		fireTurnEnds(new TurnEvent(this, current, opponent, round, turn, action));
		turn++;
	}

	/**
	 * Check if a player has a {@link Piece} to play on the game {@link Board}
	 *
	 * @param board   the current game board
	 * @param current the player to check if he can play
	 * @return true iff the player can put at least one {@link Piece}
	 */
	public static boolean canPlay(Board board, Player current){
		if(current.hasPieces()){
			for(int row = 0; row < board.getSize(); row++){
				for(int col = 0; col < board.getSize(); col++){
					if(board.isFree(row,col)) return true;
				}
			}
		}
		final int[] amount = {1};
		for(int row = 0; row < board.getSize(); row++){
			for(int col = 0; col < board.getSize(); col++){
				if(!board.isFree(row,col)){
					if(board.inBounds(row-1,col) && board.canMove(current.getColor(),row,col,amount,row-1,col)) return true;
					if(board.inBounds(row,col-1) && board.canMove(current.getColor(),row,col,amount,row,col-1)) return true;
					if(board.inBounds(row+1,col) && board.canMove(current.getColor(),row,col,amount,row+1,col)) return true;
					if(board.inBounds(row,col+1) && board.canMove(current.getColor(),row,col,amount,row,col+1)) return true;
				}
			}
		}
		return false;
	}
	/**
	 * Execute the game mechanic of a single round: handle events, turns, scoring, and players
	 */
	protected void executeRound(){
		board.reset();
		for(Player player : players){
			player.initialize(board.countInitialStones(),board.countInitialCapstones());
		}
		players[0].setColor(Constants.FIRST_PLAYER);
		players[1].setColor(Constants.SECOND_PLAYER);
		fireRoundBegins(new RoundEvent(this, players[0], this.players[1], round));
		Player winner = null;
		WinningReason reason = null;
		this.turn = 0;
		int prevScoreP1, prevScoreP2;
		do{
			if(Thread.currentThread().isInterrupted()) return;
			prevScoreP1	= players[0].getScore();
			prevScoreP2	= players[1].getScore();
			executeTurn();
			if(players[0].countSkip() >= skipLimit){
				winner = players[1];
				reason = WinningReason.SKIP_LIMIT;
			}else if(players[1].countSkip() >= skipLimit){
				winner = players[0];
				reason = WinningReason.SKIP_LIMIT;
			}else if(players[0].getScore() > prevScoreP1){
				winner = players[0];
				reason = WinningReason.PATH_COMPLETED;
			}else if(players[1].getScore() > prevScoreP2){
				winner = players[1];
				reason = WinningReason.PATH_COMPLETED;
			}else if(board.isCompleted() || players[0].countPieces()<1 || players[1].countPieces()<1 || !canPlay(board, players[0]) || !canPlay(board, players[1])){
				final int dolmenBlack = board.countTopPieces(Piece.DOLMEN_BLACK);
				final int dolmenWhite = board.countTopPieces(Piece.DOLMEN_WHITE);
				reason = WinningReason.DOLMEN_TIE;
				if(dolmenBlack > dolmenWhite) winner = players[0].getColor() == Constants.BLACK_PLAYER ? players[0] : players[1];
				else if(dolmenBlack < dolmenWhite) winner = players[0].getColor() == Constants.WHITE_PLAYER ? players[0] : players[1];
				else{
					winner = players[1]; // tie, always second player because he does not start (disaventage)
					reason = WinningReason.TIE_SECOND_PLAYER;
				}
			}
		}while(winner == null);
		winner.win();
		fireRoundEnds(new RoundEvent(this, players[0], this.players[1], round, winner, reason));
		round++;
		Player swap = players[0];
		players[0] = players[1];
		players[1] = swap;
	}

	/**
	 * Execute the game mechanic: handle events, rounds, turns, scoring, and players
	 */
	protected void executeGame(){
		this.round = 0;
		for(Player player : players){
			player.getStrategy().register(this);
		}
		fireGameBegins(new GameEvent(this, this.players[0], this.players[1]));

		while(!Thread.currentThread().isInterrupted() && players[0].countWin() < numWinningRounds && players[1].countWin() < numWinningRounds){
			executeRound();
		}
		fireGameEnds(new GameEvent(this, this.players[0], this.players[1], players[0].countWin() >= numWinningRounds ? players[0] : players[1]));
		for(Player player : players){
			player.getStrategy().unregister(this);
		}
	}

	/**
	 * is the method called by the Java {@link Thread} mechanism which consist only in calling {@link Game#executeGame()}
	 */
	@Override
	public void run(){
		executeGame();
	}

	/**
	 * Send an event on all {@link MisdesignListener}, calling {@link MisdesignListener#onTimeout(MisdesignEvent)}
	 *
	 * @param event the event to send
	 */
	protected void fireTimeout(final MisdesignEvent event){
		for(MisdesignListener listener : misdesignListeners)
			listener.onTimeout(event);
	}

	/**
	 * Send an event on all {@link MisdesignListener}, calling {@link MisdesignListener#onException(MisdesignEvent)}
	 *
	 * @param event the event to send
	 */
	protected void fireException(final MisdesignEvent event){
		for(MisdesignListener listener : misdesignListeners)
			listener.onException(event);
	}

	/**
	 * Send an event on all {@link MisdesignListener}, calling {@link MisdesignListener#onInvalidAction(MisdesignEvent)}
	 *
	 * @param event the event to send
	 */
	protected void fireInvalidAction(final MisdesignEvent event){
		for(MisdesignListener listener : misdesignListeners)
			listener.onInvalidAction(event);
	}

	/**
	 * Send an event on all {@link MisdesignListener}, calling {@link MisdesignListener#onInvalidPiece(MisdesignEvent)}
	 *
	 * @param event the event to send
	 */
	protected void fireInvalidPiece(final MisdesignEvent event){
		for(MisdesignListener listener : misdesignListeners)
			listener.onInvalidPiece(event);
	}

	/**
	 * Send an event on all {@link GameListener}, calling {@link GameListener#onGameBegins(GameEvent)}
	 *
	 * @param event the event to send
	 */
	protected void fireGameBegins(final GameEvent event){
		for(GameListener listener : gameListeners)
			listener.onGameBegins(event);
	}

	/**
	 * Send an event on all {@link GameListener}, calling {@link GameListener#onGameEnds(GameEvent)}
	 *
	 * @param event the event to send
	 */
	protected void fireGameEnds(final GameEvent event){
		for(GameListener listener : gameListeners)
			listener.onGameEnds(event);
	}

	/**
	 * Send an event on all {@link RoundListener}, calling {@link RoundListener#onRoundBegins(RoundEvent)}
	 *
	 * @param event the event to send
	 */
	protected void fireRoundBegins(final RoundEvent event){
		for(RoundListener listener : roundListeners)
			listener.onRoundBegins(event);
	}

	/**
	 * Send an event on all {@link RoundListener}, calling {@link RoundListener#onRoundEnds(RoundEvent)}
	 *
	 * @param event the event to send
	 */
	protected void fireRoundEnds(final RoundEvent event){
		for(RoundListener listener : roundListeners)
			listener.onRoundEnds(event);
	}

	/**
	 * Send an event on all {@link TurnListener}, calling {@link TurnListener#onTurnBegins(TurnEvent)}
	 *
	 * @param event the event to send
	 */
	protected void fireTurnBegins(final TurnEvent event){
		for(TurnListener listener : turnListeners)
			listener.onTurnBegins(event);
	}

	/**
	 * Send an event on all {@link TurnListener}, calling {@link TurnListener#onTurnEnds(TurnEvent)}
	 *
	 * @param event the event to send
	 */
	protected void fireTurnEnds(final TurnEvent event){
		for(TurnListener listener : turnListeners)
			listener.onTurnEnds(event);
	}

	/**
	 * Register a {@link GameListener} to forward all {@link GameEvent}
	 *
	 * @param listener the listener that will receive events
	 */
	public void addGameListener(GameListener listener){
		gameListeners.add(listener);
	}

	/**
	 * Unregister a {@link GameListener} to stop forwarding {@link GameEvent}
	 *
	 * @param listener the listener that will no more receive events
	 */
	public void removeGameListener(GameListener listener){
		gameListeners.remove(listener);
	}

	/**
	 * Register a {@link RoundListener} to forward all {@link RoundEvent}
	 *
	 * @param listener the listener that will receive events
	 */
	public void addRoundListener(RoundListener listener){
		roundListeners.add(listener);
	}

	/**
	 * Unregister a {@link RoundListener} to stop forwarding {@link RoundEvent}
	 *
	 * @param listener the listener that will no more receive events
	 */
	public void removeRoundListener(RoundListener listener){
		roundListeners.remove(listener);
	}

	/**
	 * Register a {@link TurnListener} to forward all {@link TurnEvent}
	 *
	 * @param listener the listener that will receive events
	 */
	public void addTurnListener(TurnListener listener){
		turnListeners.add(listener);
	}

	/**
	 * Unregister a {@link TurnListener} to stop forwarding {@link TurnEvent}
	 *
	 * @param listener the listener that will no more receive events
	 */
	public void removeTurnListener(TurnListener listener){
		turnListeners.remove(listener);
	}

	/**
	 * Register a {@link MisdesignListener} to forward all {@link MisdesignEvent}
	 *
	 * @param listener the listener that will receive events
	 */
	public void addMisdesignListener(MisdesignListener listener){
		misdesignListeners.add(listener);
	}

	/**
	 * Unregister a {@link MisdesignListener} to stop forwarding {@link MisdesignEvent}
	 *
	 * @param listener the listener that will no more receive events
	 */
	public void removeMisdesignListener(MisdesignListener listener){
		misdesignListeners.remove(listener);
	}

	/**
	 * Get the current board used by the running game
	 *
	 * @return the board used by the game
	 */
	public Board getBoard(){
		return this.board;
	}
}
