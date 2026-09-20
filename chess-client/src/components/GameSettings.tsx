import React, { useState, useEffect, useRef } from 'react';
import './GameSettings.css';
import { soundPlayer } from '../utils/soundPlayer';

interface Props {
    onSurrender: () => void;
    isGameOver?: boolean;
}

export const GameSettings: React.FC<Props> = ({ onSurrender, isGameOver }) => {
    const [isOpen, setIsOpen] = useState(false);
    const [isMuted, setIsMuted] = useState(soundPlayer.getMuted());
    const menuRef = useRef<HTMLDivElement>(null);

    // Toggle menu
    const toggleMenu = () => setIsOpen(!isOpen);

    // Ẩn menu khi click ra ngoài
    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
                setIsOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const handleToggleSound = () => {
        const newMuted = !isMuted;
        soundPlayer.setMuted(newMuted);
        setIsMuted(newMuted);
        setIsOpen(false);
    };

    const handleSurrender = () => {
        if (isGameOver) {
            setIsOpen(false);
            onSurrender(); // Đã hết game thì thoát thẳng, onSurrender lúc này đang gọi hàm leave
        } else {
            if (window.confirm("Bạn có chắc chắn muốn đầu hàng không?")) {
                setIsOpen(false);
                onSurrender();
            }
        }
    };

    return (
        <div className="game-settings-container" ref={menuRef}>
            <button className="settings-btn" onClick={toggleMenu} title="Cài đặt">
                ⚙️
            </button>

            {isOpen && (
                <div className="settings-popover">
                    <button className="menu-item" onClick={handleToggleSound}>
                        {isMuted ? '🔇 Bật Âm Thanh' : '🔊 Tắt Âm Thanh'}
                    </button>
                    <button className="menu-item surrender-btn" onClick={handleSurrender}>
                        {isGameOver ? '🚪 Thoát Phòng' : '🏳️ Đầu Hàng'}
                    </button>
                </div>
            )}
        </div>
    );
};
