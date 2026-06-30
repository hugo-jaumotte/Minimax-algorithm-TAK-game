package be.belegkarnil.game.board.tak.strategy;

import be.belegkarnil.game.board.tak.Action;
import be.belegkarnil.game.board.tak.Board;
import be.belegkarnil.game.board.tak.BoardHelper;
import be.belegkarnil.game.board.tak.Piece;
import be.belegkarnil.game.board.tak.Player;
import be.belegkarnil.game.board.tak.strategy.StrategyAdapter;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

// Strategy based on the Minimax algorithm with iterative deepening.
// Alpha-beta pruning is used to reduce the search space.
// Move ordering is performed using a lightweight heuristic to improve pruning efficiency.
// A heuristic evaluation function estimates non-terminal positions.
public class hjaumotte extends StrategyAdapter {

    // Maximum depth of iterative deepening (this value is never reached anyway)
    private static final int  MAX_DEPTH  = 10;
    // 58 seconds of thinking. After that, we take the best move that has already been calculated
    private static final long TIME_LIMIT = 58_000;

    private Color  myColor;
    private Color  opColor;
    private long   startTime;
    private Player myselfRef;
    private Player opponentRef;

    // Function called by the game every turn to perform an action
    @Override
    public Action plays(Player myself, Board board, Player opponent) {
        // Definitions of game settings (player IDs, which color to play, start the 58-second timer)
        this.myselfRef   = myself;
        this.opponentRef = opponent;
        this.myColor     = myself.getColor();
        this.opColor     = opponent.getColor();
        this.startTime   = System.currentTimeMillis();

        // First move of the round: we target the opponent's dolmen directly in a corner
        // One of the best choices at the start of the game, because the central squares are stronger; the outer squares are the worst choice, just like in chess
        if (isFirstTurn(board)) {
            Action corner = playCorner(board, myself);
            if (corner != null) return corner;
        }

        // We store all legal moves in a list
        List<Action> moves = generateMoves(board, myself);
        if (moves.isEmpty()) return new Action();

        // If any action in the list results in an immediate win, it is done directly, bypassing the Minimax
        for (Action move : moves) {
            BoardHelper copy = BoardHelper.copyOf(board);
            applyMove(copy, move, myColor);
            if (pathExistsForColor(copy, myColor)) return move;
        }

        // Immediate block: if the opponent can win on the next action, we block it directly using this method, bypassing the Minimax
        if (gapToWin(board, opColor) <= 1) {
            Action block = findBestBlock(board, moves);
            if (block != null) return block;
        }

        // Order the actions based on a specific move ordering heuristic in order to optimise alpha-beta pruning
        moves.sort((a, b) -> Integer.compare(
                staticMovePriority(b, board),
                staticMovePriority(a, board)
        ));

        // If no specific situation is detected, we run the Minimax
        // Iteratively increase the search depth.
        // If the time limit is reached, return the best move found at the deepest completed search.
        Action bestMove = moves.get(0);
        for (int depth = 1; depth <= MAX_DEPTH; depth++) {
            if (timeUp()) break;
            Action candidate = searchAtDepth(board, moves, depth);
            if (candidate != null) bestMove = candidate;
        }
        return bestMove;
    }

    // Function that calculates, from among the moves provided, those that block the opponent if they are about to win (one piece missing)
    // We simulate all the moves, then simulate the opponent's response. If they still win, our move is removed from the list
    // From among the remaining moves, select the best one using an evaluation function
    private Action findBestBlock(Board board, List<Action> moves) {

        Action best = null;
        int bestScore = Integer.MIN_VALUE;

        for (Action myMove : moves) {

            BoardHelper afterMyMove = BoardHelper.copyOf(board);
            applyMove(afterMyMove, myMove, myColor);

            boolean stillLoses = false;

            List<Action> opponentMoves =
                    generateMovesForColor(afterMyMove, opColor, opponentRef);

            for (Action opMove : opponentMoves) {

                BoardHelper afterOpMove = BoardHelper.copyOf(afterMyMove);
                applyMove(afterOpMove, opMove, opColor);

                if (pathExistsForColor(afterOpMove, opColor)) {
                    stillLoses = true;
                    break;
                }
            }

            if (!stillLoses) {

                int score = evaluate(afterMyMove);

                if (score > bestScore) {
                    bestScore = score;
                    best = myMove;
                }
            }
        }

        return best;
    }

    // Return true if there is 0 or 1 piece on the board (to detect if we are playing the first turn)
    private boolean isFirstTurn(Board board) {
        return board.countEmpty() >= board.getSize() * board.getSize() - 1;
    }

    // We play the first available piece on a corner (corner are far from the center, so its a low value play)
    // Since we have to play an opponent's piece on the first time, it can be useful in this specific square
    private Action playCorner(Board board, Player player) {
        int size = board.getSize();
        int[][] corners = {{0,0},{0,size-1},{size-1,0},{size-1,size-1}};
        Piece chosen = null;
        for (Map.Entry<Piece, Integer> e : player.getPieces().entrySet())
            if (e.getValue() > 0) { chosen = e.getKey(); break; }
        if (chosen == null) return null;
        for (int[] c : corners)
            if (board.isFree(c[0], c[1]) && board.canPlace(chosen, c[0], c[1]))
                return new Action(chosen, new Point(c[1], c[0]));
        return null;
    }

    // Return true at the end of the time
    private boolean timeUp() {
        return System.currentTimeMillis() - startTime > TIME_LIMIT;
    }

    // Call the minimax for each move, use limited depth minimax and limit global search time, provide a respond at the end of the timer, no matter the reached depth
    // Timer is started at the start of the turn and finished after 58 sec
    private Action searchAtDepth(Board board, List<Action> moves, int depth) {
        Action best  = null;
        int    bestS = Integer.MIN_VALUE;
        int    alpha = Integer.MIN_VALUE;
        int    beta  = Integer.MAX_VALUE;
        // For each action, we make a copy of the board, and we do the action on that board copy. Then, we call minimax to evaluate the action.
        for (Action move : moves) {
            if (timeUp()) return best;
            BoardHelper copy = BoardHelper.copyOf(board);
            applyMove(copy, move, myColor);
            int score = minimax(copy, depth - 1, alpha, beta, false);
            if (score > bestS) { bestS = score; best = move; }
            alpha = Math.max(alpha, bestS);
        }
        return best;
    }

    // Minimax with alpha-beta pruning
    private int minimax(BoardHelper board, int depth, int alpha, int beta, boolean maximizing) {

        // If the depth is reached or the timer finished, we evaluate the position
        if (depth == 0 || timeUp()) return evaluate(board);

        // Determine which player play at this level
        Color currentColor = maximizing ? myColor : opColor;
        Player currentRef  = maximizing ? myselfRef : opponentRef;

        // Generate all legal actions for the current player
        List<Action> moves = generateMovesForColor(board, currentColor, currentRef);
        if (moves.isEmpty()) return evaluate(board);

        // Order moves using an move ordering heuristic to improve alpha-beta pruning.
        if (depth >= 2)
            moves.sort((a, b) -> Integer.compare(
                    staticMovePriority(b, board),
                    staticMovePriority(a, board)));

        if (maximizing) {
            // We want to maximise our score in our turn
            int best = Integer.MIN_VALUE;

            for (Action move : moves) {
                if (timeUp()) break;

                BoardHelper copy = BoardHelper.copyOf(board);
                applyMove(copy, move, myColor);

                best = Math.max(best, minimax(copy, depth - 1, alpha, beta, false));
                alpha = Math.max(alpha, best);

                // Alpha-beta pruning
                if (beta <= alpha) break;
            }
            return best;

        } else {
            // We assume that the opponent play optimally (minimizing our score)
            int best = Integer.MAX_VALUE;

            for (Action move : moves) {
                if (timeUp()) break;

                BoardHelper copy = BoardHelper.copyOf(board);
                applyMove(copy, move, opColor);

                best = Math.min(best, minimax(copy, depth - 1, alpha, beta, true));
                beta = Math.min(beta, best);

                if (beta <= alpha) break;
            }
            return best;
        }
    }

    // Move ordering heuristic
    private int staticMovePriority(Action action, Board board) {
        // Determine where the action ends
        int size   = board.getSize();
        int center = size / 2;
        int row    = action.isPlace() ? action.position.y    : action.destination.y;
        int col    = action.isPlace() ? action.position.x    : action.destination.x;

        // Reward actions ending near the center of the board (just as in chess, controlling center is really important)
        int score = (size - Math.abs(row - center)) + (size - Math.abs(col - center));

        // Negative points if we move a piece since it can lead numerical disadvantage (see rules about moving pieces)
        if (action.isMove()) {
            score -= 8;
            if (isFlatColor(board, action.position.y, action.position.x, myColor))
                score -= 5;
        } else if (!action.piece.isMenhir()) {
            score += 5;
        }

        // We earn a reward if our action blocks opponent's road
        if (board.isFree(row, col)) {
            int before = gapToWin(board, opColor);
            int afterH = gapToCompleteBlocked(board, opColor, size, true,  row, col);
            int afterV = gapToCompleteBlocked(board, opColor, size, false, row, col);
            int blockGain = Math.min(afterH, afterV) - before;
            if (blockGain > 0) score += 400 * blockGain;
            else if (blockGain == 0 && before <= 3) score += 80;
        }

        // We earn a reward if the piece we played reinforce a road
        if (board.isFree(row, col)) {
            int[] dr = {-1, 1, 0, 0};
            int[] dc = { 0, 0,-1, 1};
            for (int d = 0; d < 4; d++) {
                int nr = row + dr[d], nc = col + dc[d];
                if (board.inBounds(nr, nc) && isFlatColor(board, nr, nc, myColor)) {
                    score += 150; break;
                }
            }
        }
        return score;
    }

    // Heuristic evaluation function.
    // Called when the search reaches the maximum depth or the time limit.
    // Also used in the urgent blocking function to define which blocking action to use
    private int evaluate(Board board) {
        // If a player is about to win, we directly return extremely large scores and do not lose time in calculation
        if (pathExistsForColor(board, myColor)) return  100000;
        if (pathExistsForColor(board, opColor)) return -100000;

        // Calculation of the central squares based on the board size
        int size   = board.getSize();
        int center = size / 2;
        int score  = 0;
        int myCtrl = 0, opCtrl = 0;

        // Score depending on which player has a better control of the center
        // (just like in chess, controlling the central squares of the board has more impact than controlling the edges)
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (!board.isFree(row, col)) {
                    Piece top = board.getTop(row, col);
                    int cent = (size - Math.abs(row - center)) + (size - Math.abs(col - center));
                    int val  = top.isDolmen() ? cent : top.isCapstone() ? cent/2 : cent/4;

                    if (top.color.equals(myColor)) {
                        myCtrl++;
                        score += val;
                        // Penalite : piece de chemin alliee enfouie
                        for (Piece p : board.getStack(row, col))
                            if (p != top && p.color.equals(myColor) && !p.isMenhir())
                                score -= cent;
                    } else {
                        opCtrl++;
                        score -= val;
                    }
                }
            }
        }

        // Numeric advantage (which player has more pieces on the board)
        score += 10 * (myCtrl - opCtrl);
        // Size of our roads
        score += 80 * bestPathScore(board, myColor);
        // Size of opponent roads
        score -= 80 * bestPathScore(board, opColor);
        // Reward board coverage across rows and columns
        score += 40 * globalBorderScore(board, myColor);
        score -= 80 * globalBorderScore(board, opColor);

        // Is there a missing piece to place to win
        int myGap = gapToWin(board, myColor);
        int opGap = gapToWin(board, opColor);

        // Scores for imminent threats/chances of victory
        if (opGap == 1) return -90000;
        if (myGap == 1) return  90000;
        if (opGap == 2) score -= 20000;
        if (opGap == 3) score -=  5000;
        if (myGap == 2) score +=  20000;
        if (myGap == 3) score +=  5000;

        // Remaining pieces in reserve.
        score -= 2 * myselfRef.countPieces();
        score += 2 * opponentRef.countPieces();
        return score;
    }

    // Score based on the size of the player's largest connected road.
    private int bestPathScore(Board board, Color color) {
        int size = board.getSize();
        boolean[][] visited = new boolean[size][size];
        int best = 0;
        int[] dr = {-1, 1, 0, 0}, dc = {0, 0, -1, 1};

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (!visited[row][col] && isFlatColor(board, row, col, color)) {
                    boolean[] rows = new boolean[size], cols = new boolean[size];
                    Queue<int[]> q = new LinkedList<>();
                    q.add(new int[]{row, col});
                    visited[row][col] = true;
                    while (!q.isEmpty()) {
                        int[] cur = q.poll();
                        rows[cur[0]] = true; cols[cur[1]] = true;
                        for (int d = 0; d < 4; d++) {
                            int nr = cur[0]+dr[d], nc = cur[1]+dc[d];
                            if (board.inBounds(nr, nc) && !visited[nr][nc]
                                    && isFlatColor(board, nr, nc, color)) {
                                visited[nr][nc] = true;
                                q.add(new int[]{nr, nc});
                            }
                        }
                    }
                    best = Math.max(best, countTrue(rows) + countTrue(cols));
                }
            }
        }
        return best;
    }

    // Give a score based on how the pieces are scattered on the board (useful for heuristic calculation)
    private int globalBorderScore(Board board, Color color) {
        int size = board.getSize();
        boolean[] covH = new boolean[size], covV = new boolean[size];
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                if (isFlatColor(board, r, c, color)) { covH[c] = true; covV[r] = true; }
        return Math.max(countTrue(covH), countTrue(covV));
    }

    // Compute the number of pieces to play to win
    private int gapToWin(Board board, Color color) {
        int size = board.getSize();
        return Math.min(
                gapToComplete(board, color, size, true),
                gapToComplete(board, color, size, false));
    }

    private int gapToComplete(Board board, Color color, int size, boolean horizontal) {
        return gapToCompleteBlocked(board, color, size, horizontal, -1, -1);
    }

    // Compute how many actions left to win
    private int gapToCompleteBlocked(Board board, Color color, int size,
                                     boolean horizontal, int blockR, int blockC) {
        int[][] dist = new int[size][size];
        for (int[] row : dist) Arrays.fill(row, Integer.MAX_VALUE);
        ArrayDeque<int[]> deque = new ArrayDeque<>();
        int[] dr = {-1, 1, 0, 0}, dc = {0, 0, -1, 1};

        for (int i = 0; i < size; i++) {
            int r = horizontal ? i : 0, c = horizontal ? 0 : i;
            if (r == blockR && c == blockC) continue;
            if (!isWall(board, r, c, color)) {
                int cost = isFlatColor(board, r, c, color) ? 0 : 1;
                if (cost < dist[r][c]) {
                    dist[r][c] = cost;
                    if (cost == 0) deque.addFirst(new int[]{r, c});
                    else           deque.addLast(new int[]{r, c});
                }
            }
        }

        while (!deque.isEmpty()) {
            int[] cur = deque.pollFirst();
            int r = cur[0], c = cur[1], d = dist[r][c];
            if (horizontal && c == size-1) return d;
            if (!horizontal && r == size-1) return d;
            for (int dir = 0; dir < 4; dir++) {
                int nr = r+dr[dir], nc = c+dc[dir];
                if (nr < 0 || nr >= size || nc < 0 || nc >= size) continue;
                if (nr == blockR && nc == blockC) continue;
                if (isWall(board, nr, nc, color)) continue;
                int nd = d + (isFlatColor(board, nr, nc, color) ? 0 : 1);
                if (nd < dist[nr][nc]) {
                    dist[nr][nc] = nd;
                    if (isFlatColor(board, nr, nc, color)) deque.addFirst(new int[]{nr, nc});
                    else                                    deque.addLast(new int[]{nr, nc});
                }
            }
        }
        return Integer.MAX_VALUE;
    }

    // Detect opponetµnt pieces presence
    private boolean isWall(Board board, int r, int c, Color color) {
        if (board.isFree(r, c)) return false;
        return !board.getTop(r, c).color.equals(color);
    }

    // Victory detection (edge-to-edge)
    private boolean pathExistsForColor(Board board, Color color) {
        int size = board.getSize();
        boolean[][] visited;
        Queue<int[]> queue;
        int[] dr = {-1,1,0,0}, dc = {0,0,-1,1};

        // Horizontal
        visited = new boolean[size][size];
        queue   = new LinkedList<>();
        for (int r = 0; r < size; r++)
            if (isFlatColor(board, r, 0, color)) { visited[r][0]=true; queue.add(new int[]{r,0}); }
        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            if (cur[1] == size-1) return true;
            for (int d = 0; d < 4; d++) {
                int nr = cur[0]+dr[d], nc = cur[1]+dc[d];
                if (nr>=0 && nr<size && nc>=0 && nc<size && !visited[nr][nc]
                        && isFlatColor(board, nr, nc, color)) {
                    visited[nr][nc]=true; queue.add(new int[]{nr,nc});
                }
            }
        }

        // Vertical
        visited = new boolean[size][size];
        queue   = new LinkedList<>();
        for (int c = 0; c < size; c++)
            if (isFlatColor(board, 0, c, color)) { visited[0][c]=true; queue.add(new int[]{0,c}); }
        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            if (cur[0] == size-1) return true;
            for (int d = 0; d < 4; d++) {
                int nr = cur[0]+dr[d], nc = cur[1]+dc[d];
                if (nr>=0 && nr<size && nc>=0 && nc<size && !visited[nr][nc]
                        && isFlatColor(board, nr, nc, color)) {
                    visited[nr][nc]=true; queue.add(new int[]{nr,nc});
                }
            }
        }
        return false;
    }

    // Detect if we control the squares and if we can use it to make a path, so not a menhir (see rules and pieces functions)
    private boolean isFlatColor(Board board, int r, int c, Color color) {
        if (board.isFree(r, c)) return false;
        Piece top = board.getTop(r, c);
        return top.color.equals(color) && !top.isMenhir();
    }

    // Actions generation
    private List<Action> generateMoves(Board board, Player player) {
        return generateMovesForColor(board, player.getColor(), player);
    }

    // Function that generate the list of potencial actions
    private List<Action> generateMovesForColor(Board board, Color color, Player ref) {
        List<Action> moves = new ArrayList<>();
        int size = board.getSize();

        if (ref != null) {
            boolean added = false;
            for (Map.Entry<Piece, Integer> e : ref.getPieces().entrySet()) {
                Piece piece = e.getKey();
                if (e.getValue() > 0 && piece.color.equals(color)) {
                    for (int r = 0; r < size; r++)
                        for (int c = 0; c < size; c++)
                            if (board.isFree(r, c) && board.canPlace(piece, r, c)) {
                                moves.add(new Action(piece, new Point(c, r)));
                                added = true;
                            }
                }
            }
            // First turn : we play with the opposite color (see rules)
            if (!added)
                for (Map.Entry<Piece, Integer> e : ref.getPieces().entrySet()) {
                    Piece piece = e.getKey();
                    if (e.getValue() > 0)
                        for (int r = 0; r < size; r++)
                            for (int c = 0; c < size; c++)
                                if (board.isFree(r, c) && board.canPlace(piece, r, c))
                                    moves.add(new Action(piece, new Point(c, r)));
                }
        }

        if (color != null) {
            int[] dr = {-1,1,0,0}, dc = {0,0,-1,1};
            for (int row = 0; row < size; row++)
                for (int col = 0; col < size; col++)
                    if (!board.isFree(row, col) && board.isUnderControl(color, row, col)) {
                        int maxCarry = Math.min(board.getStack(row,col).length, board.getLoadLimit());
                        for (int dir = 0; dir < 4; dir++)
                            for (int steps = 1; steps <= maxCarry; steps++) {
                                int nr = row+dr[dir]*steps, nc = col+dc[dir]*steps;
                                if (!board.inBounds(nr, nc)) break;
                                int[] amount = buildSimpleAmount(steps, Math.max(1, maxCarry-steps+1));
                                if (amount == null) continue;
                                Point src = new Point(col, row), dst = new Point(nc, nr);
                                if (board.canMove(color, src, amount, dst))
                                    moves.add(new Action(src, dst, amount));
                            }
                    }
        }
        return moves;
    }

    // Return the number of pièce moved in one time (see the rules on moving stacks)
    private int[] buildSimpleAmount(int steps, int carry) {
        if (steps < 1 || carry < 1) return null;
        int[] amount = new int[steps];
        for (int i = 0; i < steps-1; i++) amount[i] = 1;
        amount[steps-1] = carry;
        return amount;
    }

    // Function that apply a action in a board copy
    private void applyMove(BoardHelper board, Action action, Color color) {
        if (action.isPlace()) {
            if (board.canPlace(action.piece, action.position))
                board.applyPlace(action.piece, action.position);
        } else if (action.isMove()) {
            int[] amount = action.getAmount();
            if (amount != null && board.canMove(color, action.position, amount, action.destination))
                board.applyMove(action.position, amount, action.destination);
        }
    }

    // Return the number of trues in a boolean array
    private int countTrue(boolean[] arr) {
        int n = 0; for (boolean b : arr) if (b) n++; return n;
    }
}
