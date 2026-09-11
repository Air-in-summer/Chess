class SoundPlayer {
    private ctx: AudioContext | null = null;
    private buffers: Record<string, AudioBuffer> = {};
    private isMuted: boolean = false;

    constructor() {
        if (typeof window !== 'undefined') {
            try {
                const AudioContextClass = window.AudioContext || (window as any).webkitAudioContext;
                if (AudioContextClass) {
                    this.ctx = new AudioContextClass();
                    
                    // Pre-fetch và decode audio vào RAM
                    this.loadSound('move', '/sounds/move.mp3');
                    this.loadSound('check', '/sounds/check.mp3');
                    this.loadSound('capture', '/sounds/move.mp3'); // Dùng chung move.mp3 cho capture tạm thời
                }
            } catch (e) {
                console.error("AudioContext không được hỗ trợ", e);
            }
        }
    }

    private async loadSound(name: string, url: string) {
        try {
            const response = await fetch(url);
            const arrayBuffer = await response.arrayBuffer();
            if (this.ctx) {
                const audioBuffer = await this.ctx.decodeAudioData(arrayBuffer);
                this.buffers[name] = audioBuffer;
            }
        } catch (e) {
            console.warn(`Lỗi khi tải âm thanh [${name}] từ ${url}`);
        }
    }

    // Mở khoá AudioContext sau tương tác đầu tiên của người dùng
    unlock() {
        if (this.ctx && this.ctx.state === 'suspended') {
            this.ctx.resume().catch(() => {});
        }
    }

    setMuted(muted: boolean) {
        this.isMuted = muted;
    }

    getMuted(): boolean {
        return this.isMuted;
    }

    private playSound(name: string) {
        if (this.isMuted || !this.ctx || !this.buffers[name]) return;
        
        // Trình duyệt có thể suspend audio context nếu không có user gesture
        if (this.ctx.state === 'suspended') {
            this.ctx.resume().catch(() => {});
        }

        const source = this.ctx.createBufferSource();
        source.buffer = this.buffers[name];
        source.connect(this.ctx.destination);
        source.start(0);
    }

    playMove() {
        this.playSound('move');
    }

    playCapture() {
        this.playSound('capture');
    }

    playCheck() {
        this.playSound('check');
    }
}

export const soundPlayer = new SoundPlayer();
