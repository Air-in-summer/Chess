import type { ClientMessage, ServerMessage } from '../types/protocol';

type MessageHandler = (message: ServerMessage) => void;
type ConnectionHandler = (connected: boolean) => void;

/**
 * Lớp dịch vụ (Service) quản lý kết nối WebSocket tĩnh (Singleton) ở phía Client.
 * Mục đích chung:
 * - Đảm bảo duy trì một kết nối duy nhất (mạch máu) đến Server trong toàn bộ vòng đời ứng dụng.
 * - Tự động kết nối lại (Auto-reconnect) khi bị rớt mạng.
 * - Cung cấp cơ chế đăng ký (Subscribe) và hủy đăng ký (Unsubscribe) theo mô hình Pub/Sub,
 *   giúp các Component hoặc Hook có thể lắng nghe tin nhắn mà không bị phụ thuộc cứng.
 */
class WebSocketService {
    // Đối tượng WebSocket gốc của trình duyệt
    private ws: WebSocket | null = null;
    
    // Tập hợp các hàm callback (listeners) đăng ký chờ xử lý tin nhắn từ Server
    private messageHandlers: Set<MessageHandler> = new Set();
    
    // Tập hợp các hàm callback đăng ký chờ sự kiện thay đổi trạng thái kết nối mạng (online/offline)
    private connectionHandlers: Set<ConnectionHandler> = new Set();
    
    // Bộ đếm thời gian (timer) quản lý việc thử kết nối lại
    private reconnectTimer: number | null = null;
    
    // Cờ đánh dấu nguyên nhân đóng kết nối: Chủ động ngắt (true) hay do rớt mạng (false).
    // Nếu rớt mạng ngoài ý muốn -> Kích hoạt cơ chế tự kết nối lại.
    private isIntentionallyClosed: boolean = false;

    /**
     * Khởi tạo kết nối tới Server. Sẽ bỏ qua nếu đang kết nối hoặc đã kết nối thành công.
     * Thiết lập các event listeners cơ bản của WebSocket: onopen, onmessage, onclose, onerror.
     * @param url Địa chỉ WebSocket Server (mặc định lấy từ cấu hình máy chủ)
     */
    public connect(url: string = 'ws://localhost:8080/ws/chess') {
        if (this.ws && (this.ws.readyState === WebSocket.OPEN || this.ws.readyState === WebSocket.CONNECTING)) {
            return;
        }

        this.isIntentionallyClosed = false;
        this.ws = new WebSocket(url);

        this.ws.onopen = () => {
            console.log('[WebSocket] Connected');
            this.notifyConnectionChange(true);
            if (this.reconnectTimer) {
                clearTimeout(this.reconnectTimer);
                this.reconnectTimer = null;
            }
        };

        this.ws.onmessage = (event) => {
            try {
                const message: ServerMessage = JSON.parse(event.data);
                this.messageHandlers.forEach(handler => handler(message));
            } catch (err) {
                console.error('[WebSocket] Failed to parse message:', err);
            }
        };

        this.ws.onclose = () => {
            console.log('[WebSocket] Disconnected');
            this.notifyConnectionChange(false);
            this.ws = null;
            
            if (!this.isIntentionallyClosed) {
                this.scheduleReconnect(url);
            }
        };

        this.ws.onerror = (error) => {
            console.error('[WebSocket] Error:', error);
        };
    }

    /**
     * Tự động lên lịch kết nối lại sau mỗi 3 giây nếu kết nối bị đứt ngoài ý muốn (do mất mạng, rớt server).
     */
    private scheduleReconnect(url: string) {
        if (!this.reconnectTimer) {
            this.reconnectTimer = window.setTimeout(() => {
                this.reconnectTimer = null;
                console.log('[WebSocket] Attempting to reconnect...');
                this.connect(url);
            }, 3000);
        }
    }

    /**
     * Chủ động ngắt kết nối WebSocket (thường dùng khi người dùng chủ động thoát hoàn toàn hoặc unmount).
     * Bật cờ isIntentionallyClosed để ngăn chặn việc tự động kết nối lại (reconnect).
     */
    public disconnect() {
        this.isIntentionallyClosed = true;
        if (this.ws) {
            this.ws.close();
            this.ws = null;
        }
        if (this.reconnectTimer) {
            clearTimeout(this.reconnectTimer);
            this.reconnectTimer = null;
        }
    }

    /**
     * Gửi tin nhắn lên Server. Tự động chuyển đổi object ClientMessage thành chuỗi JSON trước khi truyền.
     * Báo lỗi nếu WebSocket chưa được mở.
     */
    public send(message: ClientMessage) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify(message));
        } else {
            console.error('[WebSocket] Cannot send message, not connected.');
        }
    }

    /**
     * Cho phép các Component (hoặc Custom Hook) đăng ký hàm lắng nghe tin nhắn đến từ Server.
     * Sử dụng mô hình Pub/Sub. Trả về một hàm dọn dẹp (cleanup function) để Component hủy đăng ký khi bị unmount.
     * @param handler Hàm xử lý tin nhắn
     */
    public onMessage(handler: MessageHandler) {
        this.messageHandlers.add(handler);
        return () => this.messageHandlers.delete(handler);
    }

    /**
     * Đăng ký hàm lắng nghe sự thay đổi trạng thái kết nối mạng (có mạng / mất mạng).
     * Sẽ gọi ngay lập tức 1 lần để báo trạng thái hiện tại cho Component biết khi vừa đăng ký.
     */
    public onConnectionChange(handler: ConnectionHandler) {
        this.connectionHandlers.add(handler);
        // Call immediately with current status
        handler(this.ws !== null && this.ws.readyState === WebSocket.OPEN);
        return () => this.connectionHandlers.delete(handler);
    }

    /**
     * Nội bộ: Phát thông báo cho tất cả các listeners đang theo dõi trạng thái kết nối.
     */
    private notifyConnectionChange(connected: boolean) {
        this.connectionHandlers.forEach(handler => handler(connected));
    }
}

export const wsService = new WebSocketService();
