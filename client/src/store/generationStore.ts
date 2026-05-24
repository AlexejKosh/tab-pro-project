import { create } from "zustand";
import { createJSONStorage, persist } from "zustand/middleware";

import { generateTab } from "@/api/tabApi";

import type { GenerateResponse, GenerateTabRequest } from "@/types/tab";

interface GenerationStore {
    lastRequest: GenerateTabRequest | null;
    result: GenerateResponse | null;
    isGenerating: boolean;
    startGeneration: (
        request: GenerateTabRequest
    ) => Promise<GenerateResponse>;
    setResult: (result: GenerateResponse | null) => void;
    reset: () => void;
}

export const useGenerationStore = create<GenerationStore>()(
    persist(
        (set) => ({
            lastRequest: null,
            result: null,
            isGenerating: false,
            startGeneration: async (request) => {
                set({
                    lastRequest: request,
                    result: null,
                    isGenerating: true,
                });

                try {
                    const response = await generateTab(request);
                    set({
                        result: response,
                        isGenerating: false,
                    });

                    return response;
                } catch (error) {
                    set({ isGenerating: false });
                    throw error;
                }
            },
            setResult: (result) => {
                set({ result });
            },
            reset: () => {
                set({
                    lastRequest: null,
                    result: null,
                    isGenerating: false,
                });
            }
        }),
        {
            name: "generation-store",
            storage: createJSONStorage(() => localStorage),
            partialize: (state) => ({
                lastRequest: state.lastRequest,
                result: state.result,
            })
        }
    )
);