import React, { useRef, useEffect, useState, useCallback } from 'react';
import { Piece, Side } from '../types/protocol';
import type { Position } from '../types/protocol';
import {
    CANVAS_WIDTH, CANVAS_HEIGHT, CELL_SIZE, PADDING, PIECE_RADIUS,
    drawBoard, drawPieces, drawSelection, drawLegalMoves, drawLastMove, toLogic
} from '../utils/boardRenderer';
import { RuleEngine } from '../utils/ruleEngine';
import './ChessBoard.css';

interface Props {
    board: Piece[][];
    mySide: Side;
    isMyTurn: boolean;
    onMove: (fromX: number, fromY: number, toX: number, toY: number) => void;
    lastMove?: { fromX: number; fromY: number; toX: number; toY: number } | null;
    checkSide?: string | null;
}

export const ChessBoard: React.FC<Props> = ({ board, mySide, isMyTurn, onMove, lastMove, checkSide }) => {
    const canvasRef = useRef<HTMLCanvasElement>(null);
    const [selectedPiece, setSelectedPiece] = useState<{ row: number; col: number } | null>(null);
    const [legalMoves, setLegalMoves] = useState<Position[]>([]);

    const isFlipped = mySide === Side.BLACK;

    // Kiểm tra quân cờ có phải của mình không
    const isMyPiece = useCallback((piece: Piece): boolean => {
        if (piece === Piece.EMPTY) return false;
        if (mySide === Side.RED) return piece.startsWith('r');
        return piece.startsWith('b');
    }, [mySide]);

    // Vẽ lại toàn bộ Canvas
    const redraw = useCallback(() => {
        const canvas = canvasRef.current;
        if (!canvas) return;
        const ctx = canvas.getContext('2d');
        if (!ctx) return;

        ctx.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        // 1. Vẽ bàn cờ
        drawBoard(ctx);

        // 2. Vẽ highlight nước đi trước (nếu có)
        if (lastMove) {
            drawLastMove(ctx, lastMove.fromX, lastMove.fromY, lastMove.toX, lastMove.toY, isFlipped);
        }

        // 3. Vẽ quân cờ
        drawPieces(ctx, board, isFlipped);

        // 4. Vẽ selection + legal moves
        if (selectedPiece) {
            drawSelection(ctx, selectedPiece.row, selectedPiece.col, isFlipped);
            drawLegalMoves(ctx, legalMoves, board, isFlipped);
        }
    }, [board, isFlipped, selectedPiece, legalMoves, lastMove]);

    // Vẽ lại khi state thay đổi
    useEffect(() => {
        redraw();
    }, [redraw]);

    // Reset selection khi board thay đổi (nước đi mới)
    useEffect(() => {
        setSelectedPiece(null);
        setLegalMoves([]);
    }, [board]);

    // Xử lý click chuột trên Canvas
    const handleClick = useCallback((e: React.MouseEvent<HTMLCanvasElement>) => {
        const canvas = canvasRef.current;
        if (!canvas) return;

        const rect = canvas.getBoundingClientRect();
        const scaleX = CANVAS_WIDTH / rect.width;
        const scaleY = CANVAS_HEIGHT / rect.height;
        const px = (e.clientX - rect.left) * scaleX;
        const py = (e.clientY - rect.top) * scaleY;

        const pos = toLogic(px, py, isFlipped);
        if (!pos) return;

        const { row, col } = pos;
        const clickedPiece = board[row][col];

        // Nếu CHƯA chọn quân nào
        if (!selectedPiece) {
            if (isMyTurn && isMyPiece(clickedPiece)) {
                setSelectedPiece({ row, col });
                const moves = RuleEngine.getAllPossibleMoves(row, col, board);
                setLegalMoves(moves);
            }
            return;
        }

        // Nếu ĐÃ chọn quân
        // Click vào chính quân đang chọn → bỏ chọn
        if (selectedPiece.row === row && selectedPiece.col === col) {
            setSelectedPiece(null);
            setLegalMoves([]);
            return;
        }

        // Click vào quân CỦA MÌNH → đổi chọn sang quân mới
        if (isMyPiece(clickedPiece)) {
            setSelectedPiece({ row, col });
            const moves = RuleEngine.getAllPossibleMoves(row, col, board);
            setLegalMoves(moves);
            return;
        }

        // Click vào ô hợp lệ → gửi nước đi
        const isLegalMove = legalMoves.some(m => m.x === row && m.y === col);
        if (isLegalMove) {
            onMove(selectedPiece.row, selectedPiece.col, row, col);
            setSelectedPiece(null);
            setLegalMoves([]);
        }
    }, [board, isFlipped, isMyTurn, isMyPiece, selectedPiece, legalMoves, onMove]);

    return (
        <div className="chessboard-wrapper">
            <canvas
                ref={canvasRef}
                width={CANVAS_WIDTH}
                height={CANVAS_HEIGHT}
                className="chessboard-canvas"
                onClick={handleClick}
            />
            {checkSide && (
                <div className="check-indicator">
                    Chiếu Tướng!
                </div>
            )}
        </div>
    );
};
