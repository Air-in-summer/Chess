import React from 'react';
import { useWebSocket } from './hooks/useWebSocket';
import { HomeScreen } from './screens/HomeScreen';
import { LobbyScreen } from './screens/LobbyScreen';
import { GameScreen } from './screens/GameScreen';
import './App.css';

function App() {
    const { 
        connected, 
        currentScreen, 
        errorMsg, 
        createRoom, 
        joinRoom,
        roomState,
        gameState,
        gameResultModal,
        lastMove,
        moveHistory,
        mySessionId,
        mySide,
        ready,
        unready,
        leave,
        sendMove
    } = useWebSocket();

    // Mở khóa âm thanh ở lần click đầu tiên của user (tránh trình duyệt block)
    React.useEffect(() => {
        const handleFirstClick = () => {
            import('./utils/soundPlayer').then(({ soundPlayer }) => {
                soundPlayer.unlock();
            });
            window.removeEventListener('click', handleFirstClick);
        };
        window.addEventListener('click', handleFirstClick);
        return () => window.removeEventListener('click', handleFirstClick);
    }, []);

    return (
        <div className="app-container">
            {currentScreen === 'HOME' && (
                <HomeScreen 
                    createRoom={createRoom} 
                    joinRoom={joinRoom} 
                    connected={connected} 
                    errorMsg={errorMsg} 
                />
            )}
            
            {currentScreen === 'LOBBY' && (
                <LobbyScreen 
                    roomState={roomState}
                    mySessionId={mySessionId}
                    ready={ready}
                    unready={unready}
                    leave={leave}
                />
            )}
            
            {currentScreen === 'GAME' && (
                <GameScreen 
                    gameState={gameState}
                    mySide={mySide}
                    mySessionId={mySessionId}
                    gameResultModal={gameResultModal}
                    lastMove={lastMove}
                    moveHistory={moveHistory}
                    onMove={sendMove}
                    leave={leave}
                />
            )}
        </div>
    );
}

export default App;
