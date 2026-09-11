import React, { useState } from 'react';
import './HomeScreen.css';

interface Props {
    createRoom: () => void;
    joinRoom: (roomId: string) => void;
    connected: boolean;
    errorMsg: string | null;
}

export const HomeScreen: React.FC<Props> = ({ createRoom, joinRoom, connected, errorMsg }) => {
    const [roomId, setRoomId] = useState('');

    const handleJoin = (e: React.FormEvent) => {
        e.preventDefault();
        if (roomId.trim()) {
            joinRoom(roomId.trim().toUpperCase());
        }
    };

    return (
        <div className="home-container">
            <h1 className="title">Cờ Tướng Online</h1>
            
            <div className={`connection-status ${connected ? 'connected' : 'disconnected'}`}>
                {connected ? 'Đã kết nối với máy chủ' : 'Đang kết nối...'}
            </div>

            <div className="card">
                <div className="action-section">
                    <h2>Tạo Phòng Mới</h2>
                    <p>Khởi tạo một phòng mới và mời bạn bè cùng chơi.</p>
                    <button 
                        className="btn primary" 
                        onClick={createRoom} 
                        disabled={!connected}
                    >
                        Tạo Phòng
                    </button>
                </div>

                <div className="divider">
                    <span>HOẶC</span>
                </div>

                <div className="action-section">
                    <h2>Tham Gia Phòng</h2>
                    <p>Nhập mã phòng 6 ký tự từ bạn bè để tham gia.</p>
                    <form onSubmit={handleJoin} className="join-form">
                        <input 
                            type="text" 
                            placeholder="Nhập mã phòng..." 
                            value={roomId}
                            onChange={(e) => setRoomId(e.target.value)}
                            maxLength={6}
                            disabled={!connected}
                        />
                        <button 
                            type="submit" 
                            className="btn secondary" 
                            disabled={!connected || !roomId.trim()}
                        >
                            Tham Gia
                        </button>
                    </form>
                </div>

                {errorMsg && <div className="error-message">{errorMsg}</div>}
            </div>
        </div>
    );
};
