package chess.server.service.rule;

import chess.server.model.Definition.Piece;
import chess.server.model.Position;
import chess.server.service.RuleService;

import java.util.ArrayList;
import java.util.List;

public class Pawn implements IPiece {

    @Override
    public boolean checkMovement(int startX, int startY, int endX, int endY, Piece[][] board) {
        // Kiểm tra xem quân cờ tại vị trí (startX, startY) có phải là Tốt (Pawn) không
        Piece piece = board[startX][startY];
        String pieceColor = piece.toString().startsWith("r") ? "Red" : "Black";
        if (piece != Piece.rP && piece != Piece.bP)
        {
            return false; // Không phải Tốt
        }

        int direction = (piece == Piece.rP) ? -1 : 1; // Hướng di chuyển của Tốt đỏ là lên (-1), Tốt đen là xuống (+1)

        // Kiểm tra di chuyển thẳng
        if (endX == startX + direction && endY == startY && (board[endX][endY] == Piece.EMPTY || RuleService.isOpponentPiece(board[endX][endY], pieceColor)))
        {
            return true; // Di chuyển thẳng một ô
        }

        // Kiểm tra di chuyển ngang khi đã qua sông
        boolean hasCrossedRiver = (piece == Piece.rP && startX <= 4) || (piece == Piece.bP && startX >= 5);
        if (hasCrossedRiver && endX == startX && Math.abs(endY - startY) == 1)
        {
            // Kiểm tra nếu ô đích có quân cờ đối phương
            if (board[endX][endY] != Piece.EMPTY && RuleService.isOpponentPiece(board[endX][endY], pieceColor))
            {
                return true; // Di chuyển ngang ăn quân đối phương
            }
            else if (board[endX][endY] == Piece.EMPTY)
            {
                return true; // Di chuyển ngang vào ô trống sau khi qua sông
            }
        }

        return false; // Nước đi không hợp lệ
    }

    @Override
    public List<Position> getAllPossibleMoves(int x, int y, Piece[][] board) {
        List<Position> possibleMoves = new ArrayList<>();
        Piece piece = board[x][y];
        String pieceColor = piece.toString().startsWith("r") ? "Red" : "Black";
        if (piece != Piece.rP && piece != Piece.bP)
        {
            return possibleMoves; // Không phải Tốt
        }

        int direction = (piece == Piece.rP) ? -1 : 1; // Hướng di chuyển của Tốt
        // Di chuyển thẳng một ô
        if (x + direction >= 0 && x + direction < 10)
        {
            // Kiểm tra nếu di chuyển quân cờ không làm Tướng bị chiếu, không làm chạm mặt Tướng
            Piece originalPiece = board[x + direction][y];
            boolean isGeneralInCheck = false;
            if (RuleService.isOpponentPiece(originalPiece, pieceColor) || originalPiece == Piece.EMPTY)
            {
                board[x + direction][y] = piece;
                board[x][y] = Piece.EMPTY;
                isGeneralInCheck = RuleService.isGeneralInChecked(board, piece.toString().startsWith("r") ? "Red" : "Black");
                //boolean isFacingGenerals = RuleService.isGeneralFaceToFace(board);
                board[x][y] = piece;
                board[x + direction][y] = originalPiece;
            }

            if ((board[x + direction][y] == Piece.EMPTY || RuleService.isOpponentPiece(originalPiece, pieceColor)) && !isGeneralInCheck)
            {
                possibleMoves.add(new Position(x + direction, y));
            }
            
        }

        // Kiểm tra di chuyển ngang khi đã qua sông
        boolean hasCrossedRiver = (piece == Piece.rP && x <= 4) || (piece == Piece.bP && x >= 5);
        if (hasCrossedRiver)
        {          
            // Di chuyển ngang sang trái
            if (x >= 0 && x < 10 && y - 1 >= 0)
            {
                // Kiểm tra nếu di chuyển quân cờ không làm Tướng bị chiếu, không làm chạm mặt Tướng
                Piece originalPiece = board[x][y - 1];
                boolean isGeneralInCheck = false, isFacingGenerals = false;
                if (RuleService.isOpponentPiece(originalPiece, pieceColor) || originalPiece == Piece.EMPTY)
                {
                    board[x][y - 1] = piece;
                    board[x][y] = Piece.EMPTY;
                    isGeneralInCheck = RuleService.isGeneralInChecked(board, piece.toString().startsWith("r") ? "Red" : "Black");
                    isFacingGenerals = RuleService.isGeneralFaceToFace(board);
                    board[x][y] = piece;
                    board[x][y - 1] = originalPiece;
                }
                if ((board[x][y - 1] == Piece.EMPTY || RuleService.isOpponentPiece(board[x][y - 1], pieceColor)) && !isGeneralInCheck && !isFacingGenerals)
                {
                    possibleMoves.add(new Position(x, y - 1));
                }
            }
            // Di chuyển sang phải
            if (x >= 0 && x < 10 && y + 1 < 9)
            {
                // Kiểm tra nếu di chuyển quân cờ không làm Tướng bị chiếu, không làm chạm mặt Tướng
                Piece originalPiece = board[x][y + 1];
                boolean isGeneralInCheck = false, isFacingGenerals = false;
                if (RuleService.isOpponentPiece(originalPiece, pieceColor) || originalPiece == Piece.EMPTY)
                {
                    board[x][y + 1] = piece;
                    board[x][y] = Piece.EMPTY;
                    isGeneralInCheck = RuleService.isGeneralInChecked(board, piece.toString().startsWith("r") ? "Red" : "Black");
                    isFacingGenerals = RuleService.isGeneralFaceToFace(board);
                    board[x][y] = piece;
                    board[x][y + 1] = originalPiece;
                }
                if ((board[x][y + 1] == Piece.EMPTY || RuleService.isOpponentPiece(board[x][y + 1], pieceColor)) && !isGeneralInCheck && !isFacingGenerals)
                {
                    possibleMoves.add(new Position(x, y + 1));
                }
            }
        }
        return possibleMoves;
    }
}
