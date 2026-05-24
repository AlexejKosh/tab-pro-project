import { useEffect } from "react";

import { useUiStore } from "@/store/uiStore";

export default function MessageWindow() {
    const currentMessage = useUiStore((s) => s.currentMessage);
    const clearMessage = useUiStore((s) => s.clearMessage);

    useEffect(() => {
        if (!currentMessage) { 
            return;
        }

        const timer = window.setTimeout(() => {
            clearMessage();
        }, 4000);
        return () => {
            clearTimeout(timer);
        };
    }, [currentMessage, clearMessage]);

    if (!currentMessage) {
        return null;
    }

    return (
        <div className="message-window" role="status" aria-live="polite">
            <div className={`message message--${currentMessage.type}`}>
                {currentMessage.message}
            </div>
        </div>
    );
};