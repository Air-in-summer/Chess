import React from 'react';
import { PlayerRole } from '../types/protocol';
import type { RoomPayload } from '../types/protocol';
import './LobbyScreen.css';

interface Props {
    roomState: RoomPayload | null;
    mySessionId: string | null;
    ready: () => void;
    unready: () => void;
    leave: () => void;
}

export const LobbyScreen: React.FC<Props> = ({ roomState, mySessionId, ready, unready, leave }) => {
    if (!roomState) return <div>Đang tải...</div>;

    const players = roomState.players || [];
    let me = players.find(p => p.sessionId === mySessionId);
    let opponent = players.find(p => p.sessionId !== mySessionId);

    // Fallback for OWNER when creating room before LOBBY_STATE sends players
    if (!me && roomState.role === PlayerRole.OWNER) {
        me = { sessionId: '', role: PlayerRole.OWNER, ready: false, connected: true };
    }

    const isReady = me?.ready || false;

    return (
        <div className="lobby-container">
            <div className="lobby-card">
                <div className="lobby-header">
                    <h2>Phòng Chờ</h2>
                    <div className="room-code-box">
                        <span className="label">MÃ PHÒNG</span>
                        <span className="code">{roomState.roomId}</span>
                    </div>
                </div>

                <div className="players-list">
                    <div className={`player-slot ${me ? 'filled' : 'empty'}`}>
                        <div className="player-info">
                            <div className="avatar me">Tôi</div>
                            <div className="details">
                                <span className="role">{me?.role === PlayerRole.OWNER ? 'Chủ phòng' : 'Khách'}</span>
                            </div>
                        </div>
                        <div className="status">
                            {me?.ready ? <span className="badge ready">Sẵn sàng</span> : <span className="badge waiting">Đang chờ</span>}
                        </div>
                    </div>

                    <div className="vs-divider">VS</div>

                    <div className={`player-slot ${opponent ? 'filled' : 'empty'}`}>
                        {opponent ? (
                            <>
                                <div className="player-info">
                                    <div className="avatar opponent">Đ</div>
                                    <div className="details">
                                        <span className="role">{opponent.role === PlayerRole.OWNER ? 'Chủ phòng' : 'Khách'}</span>
                                    </div>
                                </div>
                                <div className="status">
                                    {opponent.ready ? <span className="badge ready">Sẵn sàng</span> : <span className="badge waiting">Đang chờ</span>}
                                </div>
                            </>
                        ) : (
                            <div className="waiting-opponent">Đang chờ đối thủ tham gia...</div>
                        )}
                    </div>
                </div>

                <div className="action-buttons">
                    <button className="btn leave-btn" onClick={leave}>
                        Thoát Phòng
                    </button>
                    <button 
                        className={`btn ${isReady ? 'unready-btn' : 'ready-btn'}`} 
                        onClick={isReady ? unready : ready}
                        disabled={!opponent}
                    >
                        {isReady ? 'Hủy Sẵn Sàng' : 'Sẵn Sàng'}
                    </button>
                </div>
            </div>
        </div>
    );
};
