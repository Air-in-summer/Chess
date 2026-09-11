import { Piece } from '../types/protocol';
import type { Position } from '../types/protocol';

export class RuleEngine {

    public static isOpponentPiece(piece: Piece, generalColor: "Red" | "Black"): boolean {
        if (piece === Piece.EMPTY) return false;
        if (generalColor === "Red") {
            return piece.startsWith("b");
        } else {
            return piece.startsWith("r");
        }
    }

    private static findGeneralPosition(board: Piece[][], generalColor: "Red" | "Black"): Position {
        const generalPiece = generalColor === "Red" ? Piece.rG : Piece.bG;
        for (let x = 0; x < 10; x++) {
            for (let y = 3; y < 6; y++) {
                if (board[x][y] === generalPiece) {
                    return { x, y };
                }
            }
        }
        return { x: -1, y: -1 };
    }

    public static isGeneralInChecked(board: Piece[][], generalColor: "Red" | "Black"): boolean {
        const generalPos = this.findGeneralPosition(board, generalColor);
        const { x: generalX, y: generalY } = generalPos;

        if (generalX === -1 && generalY === -1) return false;

        for (let x = 0; x < 10; x++) {
            for (let y = 0; y < 9; y++) {
                const piece = board[x][y];
                if (piece !== Piece.EMPTY && this.isOpponentPiece(piece, generalColor)) {
                    if (this.checkMovementBasic(piece, x, y, generalX, generalY, board)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static isGeneralFaceToFace(board: Piece[][]): boolean {
        const redGeneralPos = this.findGeneralPosition(board, "Red");
        const blackGeneralPos = this.findGeneralPosition(board, "Black");

        const redGeneralX = redGeneralPos.x;
        const redGeneralY = redGeneralPos.y;
        const blackGeneralX = blackGeneralPos.x;
        const blackGeneralY = blackGeneralPos.y;

        if (redGeneralX !== -1 && blackGeneralX !== -1 && redGeneralY === blackGeneralY) {
            const minX = Math.min(redGeneralX, blackGeneralX);
            const maxX = Math.max(redGeneralX, blackGeneralX);
            
            for (let x = minX + 1; x < maxX; x++) {
                if (board[x][redGeneralY] !== Piece.EMPTY) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Kiểm tra luật di chuyển cơ bản của từng quân cờ (không xét đến việc bị chiếu Tướng hay đối mặt Tướng)
     */
    public static checkMovementBasic(piece: Piece, startX: number, startY: number, endX: number, endY: number, board: Piece[][]): boolean {
        const pieceColor = piece.startsWith("r") ? "Red" : "Black";
        
        // --- 1. Tốt (Pawn) ---
        if (piece === Piece.rP || piece === Piece.bP) {
            const direction = piece === Piece.rP ? -1 : 1;
            
            // Tiến lên
            if (endX === startX + direction && endY === startY) return true;
            
            // Đi ngang (khi đã qua sông)
            const hasCrossedRiver = (piece === Piece.rP && startX <= 4) || (piece === Piece.bP && startX >= 5);
            if (hasCrossedRiver && endX === startX && Math.abs(endY - startY) === 1) {
                return true;
            }
            return false;
        }

        // --- 2. Xe (Rook) ---
        if (piece === Piece.rR || piece === Piece.bR) {
            if (startX !== endX && startY !== endY) return false;
            
            const stepX = endX === startX ? 0 : (endX - startX) / Math.abs(endX - startX);
            const stepY = endY === startY ? 0 : (endY - startY) / Math.abs(endY - startY);
            
            let currentX = startX + stepX;
            let currentY = startY + stepY;
            
            while (currentX !== endX || currentY !== endY) {
                if (board[currentX][currentY] !== Piece.EMPTY) return false;
                currentX += stepX;
                currentY += stepY;
            }
            return true;
        }

        // --- 3. Mã (Knight) ---
        if (piece === Piece.rK || piece === Piece.bK) {
            const dx = Math.abs(endX - startX);
            const dy = Math.abs(endY - startY);
            
            if ((dx === 2 && dy === 1) || (dx === 1 && dy === 2)) {
                // Kiểm tra cản chân Mã
                if (dx === 2 && dy === 1) {
                    const blockX = startX + (endX - startX) / 2;
                    if (board[blockX][startY] !== Piece.EMPTY) return false;
                } else {
                    const blockY = startY + (endY - startY) / 2;
                    if (board[startX][blockY] !== Piece.EMPTY) return false;
                }
                return true;
            }
            return false;
        }

        // --- 4. Tượng (Elephant) ---
        if (piece === Piece.rB || piece === Piece.bB) {
            const dx = Math.abs(endX - startX);
            const dy = Math.abs(endY - startY);
            
            // Tượng không được qua sông
            if (piece === Piece.rB && endX <= 4) return false;
            if (piece === Piece.bB && endX >= 5) return false;

            if (dx === 2 && dy === 2) {
                // Kiểm tra cản mắt Tượng
                const blockX = startX + (endX - startX) / 2;
                const blockY = startY + (endY - startY) / 2;
                if (board[blockX][blockY] !== Piece.EMPTY) return false;
                return true;
            }
            return false;
        }

        // --- 5. Sĩ (Advisor) ---
        if (piece === Piece.rA || piece === Piece.bA) {
            const dx = Math.abs(endX - startX);
            const dy = Math.abs(endY - startY);
            
            const inPalace = (endX >= 0 && endX <= 2 && endY >= 3 && endY <= 5) || (endX >= 7 && endX <= 9 && endY >= 3 && endY <= 5);
            
            if (dx === 1 && dy === 1 && inPalace) return true;
            return false;
        }

        // --- 6. Tướng (General) ---
        if (piece === Piece.rG || piece === Piece.bG) {
            if (Math.abs(endX - startX) + Math.abs(endY - startY) !== 1) return false;
            
            const inPalace = piece === Piece.rG 
                ? (endX >= 7 && endX <= 9 && endY >= 3 && endY <= 5)
                : (endX >= 0 && endX <= 2 && endY >= 3 && endY <= 5);
            
            if (inPalace) return true;
            return false;
        }

        // --- 7. Pháo (Cannon) ---
        if (piece === Piece.rC || piece === Piece.bC) {
            if (startX !== endX && startY !== endY) return false;
            
            const stepX = endX === startX ? 0 : (endX - startX) / Math.abs(endX - startX);
            const stepY = endY === startY ? 0 : (endY - startY) / Math.abs(endY - startY);
            
            let currentX = startX + stepX;
            let currentY = startY + stepY;
            let obstacleCount = 0;
            
            while (currentX !== endX || currentY !== endY) {
                if (board[currentX][currentY] !== Piece.EMPTY) {
                    obstacleCount++;
                }
                currentX += stepX;
                currentY += stepY;
            }
            
            // Không có chướng ngại vật: chỉ được đi vào ô trống
            if (obstacleCount === 0) {
                return board[endX][endY] === Piece.EMPTY;
            } 
            // Có 1 chướng ngại vật: bắt buộc phải ăn quân đối phương
            else if (obstacleCount === 1) {
                return board[endX][endY] !== Piece.EMPTY && this.isOpponentPiece(board[endX][endY], pieceColor);
            }
            return false;
        }

        return false;
    }

    /**
     * Trả về danh sách tất cả các nước đi HỢP LỆ (bao gồm kiểm tra chiếu Tướng)
     */
    public static getAllPossibleMoves(startX: number, startY: number, board: Piece[][]): Position[] {
        const possibleMoves: Position[] = [];
        const piece = board[startX][startY];
        if (piece === Piece.EMPTY) return possibleMoves;

        const pieceColor = piece.startsWith("r") ? "Red" : "Black";

        // Duyệt toàn bộ bàn cờ (90 ô) để tìm nước đi hợp lệ
        for (let endX = 0; endX < 10; endX++) {
            for (let endY = 0; endY < 9; endY++) {
                if (startX === endX && startY === endY) continue;
                
                const targetPiece = board[endX][endY];
                // Không được ăn quân của mình
                if (targetPiece !== Piece.EMPTY && !this.isOpponentPiece(targetPiece, pieceColor)) {
                    continue;
                }

                // Kiểm tra luật đi cơ bản
                if (this.checkMovementBasic(piece, startX, startY, endX, endY, board)) {
                    
                    // Giả lập đi cờ để kiểm tra xem Tướng có bị chiếu hoặc chạm mặt không
                    board[startX][startY] = Piece.EMPTY;
                    board[endX][endY] = piece;
                    
                    const isGeneralInCheck = this.isGeneralInChecked(board, pieceColor);
                    const isFacingGenerals = this.isGeneralFaceToFace(board);
                    
                    // Phục hồi bàn cờ
                    board[startX][startY] = piece;
                    board[endX][endY] = targetPiece;
                    
                    if (!isGeneralInCheck && !isFacingGenerals) {
                        possibleMoves.push({ x: endX, y: endY });
                    }
                }
            }
        }

        return possibleMoves;
    }
}
