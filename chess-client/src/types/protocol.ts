/**
 * Định nghĩa giao thức giao tiếp (Communication Protocol) giữa Client và Server qua WebSocket.
 * Chứa các Enum (MessageType, RoomStatus...) và Interface (Payloads) để đảm bảo đồng bộ kiểu dữ liệu hai chiều.
 */
export enum MessageType {
    // Client to Server
    CREATE_ROOM = 'CREATE_ROOM',
    JOIN_ROOM = 'JOIN_ROOM',
    READY = 'READY',
    UNREADY = 'UNREADY',
    MOVE = 'MOVE',
    SURRENDER = 'SURRENDER',
    LEAVE = 'LEAVE',
    
    // Server to Client
    ROOM_CREATED = 'ROOM_CREATED',
    JOINED_ROOM = 'JOINED_ROOM',
    ROOM_JOIN_FAILED = 'ROOM_JOIN_FAILED',
    LOBBY_STATE = 'LOBBY_STATE',
    PLAYER_JOINED = 'PLAYER_JOINED',
    PLAYER_LEFT = 'PLAYER_LEFT',
    READY_STATE_CHANGED = 'READY_STATE_CHANGED',
    START_GAME = 'START_GAME',
    BOARD_STATE = 'BOARD_STATE',
    POSSIBLE_MOVES = 'POSSIBLE_MOVES',
    MOVE_APPLIED = 'MOVE_APPLIED',
    MOVE_REJECTED = 'MOVE_REJECTED',
    CHECK = 'CHECK',
    TIMER_UPDATED = 'TIMER_UPDATED',
    GAME_OVER = 'GAME_OVER',
    RETURN_HOME = 'RETURN_HOME',
    ROOM_CLOSED = 'ROOM_CLOSED',
    ERROR = 'ERROR'
}

export enum ErrorCode {
    ROOM_NOT_FOUND = 'ROOM_NOT_FOUND',
    ROOM_FULL = 'ROOM_FULL',
    ROOM_ALREADY_PLAYING = 'ROOM_ALREADY_PLAYING',
    ROOM_FINISHED = 'ROOM_FINISHED',
    NOT_IN_ROOM = 'NOT_IN_ROOM',
    NOT_ROOM_PLAYER = 'NOT_ROOM_PLAYER',
    ROOM_NOT_IN_LOBBY = 'ROOM_NOT_IN_LOBBY',
    GAME_NOT_STARTED = 'GAME_NOT_STARTED',
    GAME_ALREADY_OVER = 'GAME_ALREADY_OVER',
    NOT_YOUR_TURN = 'NOT_YOUR_TURN',
    INVALID_POSITION = 'INVALID_POSITION',
    EMPTY_FROM_POSITION = 'EMPTY_FROM_POSITION',
    NOT_YOUR_PIECE = 'NOT_YOUR_PIECE',
    INVALID_MOVE = 'INVALID_MOVE',
    INVALID_MESSAGE = 'INVALID_MESSAGE'
}

export enum PlayerRole {
    OWNER = 'OWNER',
    GUEST = 'GUEST'
}

export enum RoomStatus {
    WAITING = 'WAITING',
    LOBBY = 'LOBBY',
    PLAYING = 'PLAYING',
    FINISHED = 'FINISHED'
}

export enum GameResult {
    ONGOING = 'ONGOING',
    RED_WIN = 'RED_WIN',
    BLACK_WIN = 'BLACK_WIN',
    DRAW = 'DRAW'
}

export enum Side {
    BLACK = 'BLACK',
    RED = 'RED'
}

export enum Piece {
    EMPTY = 'EMPTY',
    rP = 'rP', rR = 'rR', rK = 'rK', rB = 'rB', rA = 'rA', rG = 'rG', rC = 'rC',
    bP = 'bP', bR = 'bR', bK = 'bK', bB = 'bB', bA = 'bA', bG = 'bG', bC = 'bC'
}

export interface Position {
    x: number;
    y: number;
}

export interface PlayerPayload {
    sessionId: string;
    role: PlayerRole;
    ready: boolean;
    connected: boolean;
}

export interface RoomPayload {
    roomId?: string;
    role?: PlayerRole;
    roomStatus?: RoomStatus;
    players?: PlayerPayload[];
    player?: PlayerPayload;
    leftPlayerId?: string;
    playerId?: string;
    ready?: boolean;
    message?: string;
}

export interface GamePayload {
    roomId?: string;
    redPlayerId?: string;
    blackPlayerId?: string;
    board?: Piece[][];
    redTurn?: boolean;
    redTime?: number;
    blackTime?: number;
    checkSide?: string;
    winner?: string;
    loser?: string;
    result?: GameResult;
    reason?: string;
}

export interface MovePayload {
    fromX?: number;
    fromY?: number;
    toX?: number;
    toY?: number;
    piece?: Piece;
    capturedPiece?: Piece;
    moves?: Position[];
    redTurn?: boolean;
    checkSide?: string;
    redTime?: number;
    blackTime?: number;
    reason?: string;
    message?: string;
}

export interface ClientMessage {
    type: MessageType;
    roomPayload?: RoomPayload;
    movePayload?: MovePayload;
}

export interface ServerMessage {
    type: MessageType;
    roomPayload?: RoomPayload;
    movePayload?: MovePayload;
    gamePayload?: GamePayload;
    errorCode?: ErrorCode;
    message?: string;
}
