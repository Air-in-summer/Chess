import { Piece } from '../types/protocol';

export interface PieceInfo {
    char: string;       // Ký tự hán
    color: string;      // Màu chữ và viền
    fillColor: string;  // Màu nền quân cờ
}

const RED_COLOR = '#c0392b';
const BLACK_COLOR = '#2c3e50';
const RED_FILL = '#fdf2e9';
const BLACK_FILL = '#fdf2e9';

export const PIECE_MAP: Record<string, PieceInfo> = {
    // Bên Đỏ
    [Piece.rG]: { char: '帥', color: RED_COLOR, fillColor: RED_FILL },
    [Piece.rA]: { char: '仕', color: RED_COLOR, fillColor: RED_FILL },
    [Piece.rB]: { char: '相', color: RED_COLOR, fillColor: RED_FILL },
    [Piece.rK]: { char: '傌', color: RED_COLOR, fillColor: RED_FILL },
    [Piece.rR]: { char: '俥', color: RED_COLOR, fillColor: RED_FILL },
    [Piece.rC]: { char: '炮', color: RED_COLOR, fillColor: RED_FILL },
    [Piece.rP]: { char: '兵', color: RED_COLOR, fillColor: RED_FILL },

    // Bên Đen
    [Piece.bG]: { char: '將', color: BLACK_COLOR, fillColor: BLACK_FILL },
    [Piece.bA]: { char: '士', color: BLACK_COLOR, fillColor: BLACK_FILL },
    [Piece.bB]: { char: '象', color: BLACK_COLOR, fillColor: BLACK_FILL },
    [Piece.bK]: { char: '馬', color: BLACK_COLOR, fillColor: BLACK_FILL },
    [Piece.bR]: { char: '車', color: BLACK_COLOR, fillColor: BLACK_FILL },
    [Piece.bC]: { char: '砲', color: BLACK_COLOR, fillColor: BLACK_FILL },
    [Piece.bP]: { char: '卒', color: BLACK_COLOR, fillColor: BLACK_FILL },
};
