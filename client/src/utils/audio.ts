export const base64ToAudioUrl = (base64: string): string => {
    const cleaned = base64.replace(/\s+/g, "");

    if (/^data:audio\/\w+;base64,/.test(cleaned)) {
        return cleaned;
    }

    return `data:audio/mpeg;base64,${cleaned}`;
};