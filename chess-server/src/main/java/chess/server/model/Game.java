package chess.server.model;

import chess.server.model.Definition.Piece;
import chess.server.model.Definition.GameResult;

public class Game {

    private static final long INITIAL_TIME_MILLIS = 10 * 60 * 1000L;

    private Piece[][] board;

    private boolean isRedTurn;
    private boolean isRedInCheck;
    private boolean isBlackInCheck;

    private long redTimeMillis;
    private long blackTimeMillis;
    private long turnStartTime;

    private GameResult result;

    private boolean isGameOver;

    public Game() {
        newGame();
    }

    public void newGame() {
        this.board = new Piece[10][9];
        initializeBoard();
        isRedTurn = true;
        isRedInCheck = false;
        isBlackInCheck = false;
        redTimeMillis = INITIAL_TIME_MILLIS;
        blackTimeMillis = INITIAL_TIME_MILLIS;
        turnStartTime = System.currentTimeMillis();
        result = GameResult.ONGOING;
        isGameOver = false;
    }

    // Đổi lượt và trừ thời gian của người vừa đi. Hai việc này bắt buộc phải đi cùng nhau.
    public void switchTurn() {
        long elapsed = System.currentTimeMillis() - turnStartTime;
        if (isRedTurn) {
            redTimeMillis -= elapsed;
        } else {
            blackTimeMillis -= elapsed;
        }
        isRedTurn = !isRedTurn;
        turnStartTime = System.currentTimeMillis();
    }

    // Kết thúc game. Cả result và isGameOver phải được set cùng lúc, không được tách rời.
    public void endGame(GameResult result) {
        this.result = result;
        this.isGameOver = true;
    }

    private void initializeBoard() {
        clearBoard();
        board[0][0] = Piece.bR;
        board[0][1] = Piece.bK;
        board[0][2] = Piece.bB;
        board[0][3] = Piece.bA;
        board[0][4] = Piece.bG;
        board[0][5] = Piece.bA;
        board[0][6] = Piece.bB;
        board[0][7] = Piece.bK;
        board[0][8] = Piece.bR;
        board[2][1] = Piece.bC;
        board[2][7] = Piece.bC;
        board[3][0] = Piece.bP;
        board[3][2] = Piece.bP;
        board[3][4] = Piece.bP;
        board[3][6] = Piece.bP;
        board[3][8] = Piece.bP;

        board[9][0] = Piece.rR;
        board[9][1] = Piece.rK;
        board[9][2] = Piece.rB;
        board[9][3] = Piece.rA;
        board[9][4] = Piece.rG;
        board[9][5] = Piece.rA;
        board[9][6] = Piece.rB;
        board[9][7] = Piece.rK;
        board[9][8] = Piece.rR;
        board[7][1] = Piece.rC;
        board[7][7] = Piece.rC;
        board[6][0] = Piece.rP;
        board[6][2] = Piece.rP;
        board[6][4] = Piece.rP;
        board[6][6] = Piece.rP;
        board[6][8] = Piece.rP;
    }

    private void clearBoard() {
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                board[row][col] = Piece.EMPTY;
            }
        }
    }

    public Piece[][] getBoard() {
        return board;
    }

    public Piece getPiece(int x, int y) {
        return board[x][y];
    }

    public void setPiece(int x, int y, Piece piece) {
        board[x][y] = piece;
    }

    public boolean isRedTurn() {
        return isRedTurn;
    }

    public boolean isRedInCheck() {
        return isRedInCheck;
    }

    public void setRedInCheck(boolean redInCheck) {
        isRedInCheck = redInCheck;
    }

    public boolean isBlackInCheck() {
        return isBlackInCheck;
    }

    public void setBlackInCheck(boolean blackInCheck) {
        isBlackInCheck = blackInCheck;
    }

    public long getRedTimeMillis() {
        return redTimeMillis;
    }

    public long getBlackTimeMillis() {
        return blackTimeMillis;
    }

    public GameResult getResult() {
        return result;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public long getTurnStartTime() {
        return turnStartTime;
    }
}
