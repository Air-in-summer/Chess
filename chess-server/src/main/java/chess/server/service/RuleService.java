package chess.server.service;

import chess.server.model.Definition.Piece;
import chess.server.model.Position;
import chess.server.service.rule.Advisor;
import chess.server.service.rule.Cannon;
import chess.server.service.rule.Elephant;
import chess.server.service.rule.General;
import chess.server.service.rule.IPiece;
import chess.server.service.rule.Knight;
import chess.server.service.rule.Pawn;
import chess.server.service.rule.Rook;

/**
 * Class RuleService hoạt động kiểm tra luật lệ  của trò chơi. Là class tĩnh (static).
 * Chức năng cốt lõi:
 * - Cung cấp logic xác thực nước đi cho từng loại quân cờ riêng biệt thông qua Strategy Pattern (trả về IPiece).
 * - Đánh giá trạng thái tổng thể của bàn cờ: kiểm tra xem Tướng có đang bị chiếu hay không (Check), có bị chiếu hết không (Checkmate).
 * - Phát hiện và ngăn chặn các nước đi vi phạm luật (vd: luật hai Tướng đối mặt, di chuyển khiến Tướng phe mình bị chiếu).
 * Class này hoàn toàn stateless (không lưu trạng thái), chỉ tính toán dựa trên dữ liệu đầu vào.
 */
public class RuleService {
    
    public static IPiece getPieceBehaviour(Piece piece) {
        switch (piece) {
            case rP:
            case bP:
                return new Pawn();
            case rR:
            case bR:
                return new Rook();
            case rK:
            case bK:
                return new Knight();
            case rB:
            case bB:
                return new Elephant();
            case rA:
            case bA:
                return new Advisor();
            case rG:
            case bG:
                return new General();
            case rC:
            case bC:
                return new Cannon();
            default:
                throw new IllegalArgumentException("Invalid piece type");
        }
    }

    public static boolean isOpponentPiece(Piece piece, String generalColor) {
        if (piece == Piece.EMPTY) {
            return false;
        }

        if ("Red".equals(generalColor))
        {
            return piece.toString().startsWith("b"); // Quan co den la doi phuong cua do
        }
        else
        {
            return piece.toString().startsWith("r"); // Quan co do la doi phuong cua den
        }
    }

    public static boolean isGeneralInChecked(Piece[][] board, String generalColor) {
        Position generalPosition = FindGeneralPosition(board, generalColor);
        int generalX = generalPosition.getX();
        int generalY = generalPosition.getY();

        if (generalX == -1 && generalY == -1)
        {
            throw new RuntimeException("General not found on the board");
        }

        // Kiem tra tat ca cac quan co doi phuong de xem co the chieu Tuong khong
        for (int x = 0; x < 10; x++)
        {
            for (int y = 0; y < 9; y++)
            {
                Piece piece = board[x][y];
                if (piece != Piece.EMPTY && isOpponentPiece(piece, generalColor))
                {
                    IPiece behaviour = getPieceBehaviour(piece);
                    if (behaviour.checkMovement(x, y, generalX, generalY, board))
                    {
                        return true; // Tuong bi chieu
                    }
                }
            }
        }

        return false; // Tuong khong bi chieu
    }

    public static boolean isGeneralFaceToFace(Piece[][] board) {
        int redGeneralX = -1, redGeneralY = -1;
        int blackGeneralX = -1, blackGeneralY = -1;

        // Tim vi tri cua hai Tuong
        Position redGeneralPosition = FindGeneralPosition(board, "Red");
        redGeneralX = redGeneralPosition.getX();
        redGeneralY = redGeneralPosition.getY();

        Position blackGeneralPosition = FindGeneralPosition(board, "Black");
        blackGeneralX = blackGeneralPosition.getX();
        blackGeneralY = blackGeneralPosition.getY();

        // Kiem tra neu hai Tuong nam tren cung mot cot
        if (redGeneralY == blackGeneralY)
        {
            // Kiem tra xem co quan co nao o giua hai Tuong khong
            int minX = Math.min(redGeneralX, blackGeneralX);
            int maxX = Math.max(redGeneralX, blackGeneralX);

            for (int x = minX + 1; x < maxX; x++)
            {
                if (board[x][redGeneralY] != Piece.EMPTY)
                {
                    return false; // Co quan co o giua, khong cham mat
                }
            }
            return true; // Khong co quan co o giua, cham mat
        }

        return false; // Hai Tuong khong nam tren cung mot cot
    }

    // Ham tim vi tri cua Tuong tren ban co
    private static Position FindGeneralPosition(Piece[][] board, String generalColor)
    {
        Piece generalPiece = "Red".equals(generalColor) ? Piece.rG : Piece.bG;

        for (int x = 0; x < 10; x++)
        {
            for (int y = 3; y < 6; y++)
            {
                if (board[x][y] == generalPiece)
                {
                    return new Position(x, y);
                }
            }
        }

        return new Position(-1, -1); // Tuong khong duoc tim thay
    }
  

    // Kiem tra rang ben mau generalColor co con nuoc di hop le nao sau khi bi chieu tuong khong
    public static boolean HasAnyLegalMoves(Piece[][] boardGame, String generalColor)
    {
        for (int x = 0; x < 10; x++)
        {
            for (int y = 0; y < 9; y++)
            {
                Piece piece = boardGame[x][y];
                if (piece != Piece.EMPTY && !isOpponentPiece(piece, generalColor))
                {
                    IPiece behaviour = getPieceBehaviour(piece);
                    var possibleMoves = behaviour.getAllPossibleMoves(x, y, boardGame);
                    if (possibleMoves.size() > 0)
                    {
                        return true; // Tim thay it nhat mot nuoc di hop le
                    }
                }
            }
        }
        return false; // Khong tim thay nuoc di hop le nao
    }
}
