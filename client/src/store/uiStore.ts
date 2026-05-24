import { create } from "zustand";

import type { UiMessage } from "@/types/api";

interface UiStore {
    currentMessage: UiMessage | null;
    setMessage: (message: UiMessage) => void;
    clearMessage: () => void;
}

export const useUiStore =
    create<UiStore>((set) => ({
    currentMessage: null,
    setMessage: (message) =>
        set({ currentMessage: message }),
    clearMessage: () =>
        set({ currentMessage: null })
}));