import { useState, useEffect, useCallback, useRef, useMemo } from 'react';
import { wsService } from '../services/websocket';
import { MessageType, Side, Piece } from '../types/protocol';
import type { ServerMessage, RoomPayload, GamePayload, PlayerRole } from '../types/protocol';

export type ScreenType = 'HOME' | 'LOBBY' | 'GAME';

export function useWebSocket() {
    const [connected, setConnected] = useState(false);
    const [currentScreen, setCurrentScreen] = useState<ScreenType>('HOME');
    
    // Room State
    const [myRole, setMyRole] = useState<PlayerRole | null>(null);
    const [roomState, setRoomState] = useState<RoomPayload | null>(null);
    const [errorMsg, setErrorMsg] = useState<string | null>(null);
    
    // Game State
    const [gameState, setGameState] = useState<GamePayload | null>(null);
    const [gameResultModal, setGameResultModal] = useState<GamePayload | null>(null);
    const [lastMove, setLastMove] = useState<{fromX: number, fromY: number, toX: number, toY: number} | null>(null);

    const myRoleRef = useRef(myRole);
    
    const mySessionId = useMemo(() => {
        if (!roomState?.players || !myRole) return null;
        const me = roomState.players.find(p => p.role === myRole);
        return me ? me.sessionId : null;
    }, [roomState, myRole]);

    const mySide = useMemo(() => {
        if (!gameState || !mySessionId) return null;
        if (gameState.redPlayerId === mySessionId) return Side.RED;
        if (gameState.blackPlayerId === mySessionId) return Side.BLACK;
        return null;
    }, [gameState, mySessionId]);
    
    useEffect(() => {
        myRoleRef.current = myRole;
    }, [myRole]);

    useEffect(() => {
        const unsubscribeConn = wsService.onConnectionChange(setConnected);
        
        const unsubscribeMsg = wsService.onMessage((msg: ServerMessage) => {
            const role = myRoleRef.current;
            
            switch (msg.type) {
                case MessageType.ROOM_CREATED:
                case MessageType.JOINED_ROOM: {
                    const newRole = msg.roomPayload?.role || null;
                    setMyRole(newRole);
                    myRoleRef.current = newRole; // Cập nhật ngay lập tức
                    setCurrentScreen('LOBBY');
                    setRoomState(msg.roomPayload || null);
                    setErrorMsg(null);
                    break;
                }
                case MessageType.ROOM_JOIN_FAILED:
                case MessageType.ERROR: {
                    setErrorMsg(msg.message || 'An error occurred');
                    break;
                }
                case MessageType.LOBBY_STATE: {
                    setRoomState(msg.roomPayload || null);
                    break;
                }
                case MessageType.PLAYER_JOINED:
                case MessageType.PLAYER_LEFT:
                case MessageType.READY_STATE_CHANGED: {
                    if (msg.roomPayload) {
                        setRoomState(prev => {
                            if (!prev) return msg.roomPayload!;
                            return { ...prev, ...msg.roomPayload };
                        });
                    }
                    break;
                }
                case MessageType.START_GAME: {
                    if (msg.gamePayload) {
                        setGameState(msg.gamePayload);
                        setCurrentScreen('GAME');
                        setGameResultModal(null);
                        setLastMove(null);
                    }
                    break;
                }
                case MessageType.MOVE_APPLIED: {
                    if (msg.movePayload) {
                        const p = msg.movePayload!;
                        if (p.fromX !== undefined && p.fromY !== undefined && p.toX !== undefined && p.toY !== undefined) {
                            setLastMove({
                                fromX: p.fromX,
                                fromY: p.fromY,
                                toX: p.toX,
                                toY: p.toY
                            });
                        }

                        setGameState(prev => {
                            if (!prev) return prev;
                            const newBoard = prev.board ? [...prev.board.map(row => [...row])] : [];
                            
                            if (newBoard.length > 0 && p.fromX !== undefined && p.fromY !== undefined && p.toX !== undefined && p.toY !== undefined) {
                                const piece = newBoard[p.fromX][p.fromY];
                                newBoard[p.fromX][p.fromY] = Piece.EMPTY;
                                newBoard[p.toX][p.toY] = piece;
                            }
                            
                            return {
                                ...prev,
                                board: newBoard,
                                redTurn: p.redTurn,
                                redTime: p.redTime,
                                blackTime: p.blackTime,
                                checkSide: p.checkSide
                            };
                        });
                    }
                    break;
                }
                case MessageType.MOVE_REJECTED: {
                    alert('Lỗi: ' + (msg.movePayload?.reason || msg.message));
                    break;
                }
                case MessageType.GAME_OVER: {
                    if (msg.gamePayload) {
                        setGameResultModal(msg.gamePayload);
                    }
                    break;
                }
                case MessageType.ROOM_CLOSED: {
                    alert(msg.message || 'Phòng đã đóng');
                    resetToHome();
                    break;
                }
            }
        });

        wsService.connect();

        return () => {
            unsubscribeConn();
            unsubscribeMsg();
        };
    }, []);

    const resetToHome = useCallback(() => {
        setCurrentScreen('HOME');
        setMyRole(null);
        setRoomState(null);
        setGameState(null);
        setGameResultModal(null);
        setLastMove(null);
        setErrorMsg(null);
    }, []);

    const createRoom = () => wsService.send({ type: MessageType.CREATE_ROOM });
    
    const joinRoom = (roomId: string) => wsService.send({ 
        type: MessageType.JOIN_ROOM, 
        roomPayload: { roomId } 
    });
    
    const ready = () => wsService.send({ type: MessageType.READY });
    
    const unready = () => wsService.send({ type: MessageType.UNREADY });
    
    const leave = () => {
        wsService.send({ type: MessageType.LEAVE });
        resetToHome();
    };
    
    const sendMove = (fromX: number, fromY: number, toX: number, toY: number) => {
        wsService.send({
            type: MessageType.MOVE,
            movePayload: { fromX, fromY, toX, toY }
        });
    };

    return {
        connected,
        currentScreen,
        myRole,
        mySide,
        mySessionId,
        roomState,
        gameState,
        gameResultModal,
        lastMove,
        errorMsg,
        createRoom,
        joinRoom,
        ready,
        unready,
        leave,
        sendMove,
        resetToHome,
        setErrorMsg
    };
}
