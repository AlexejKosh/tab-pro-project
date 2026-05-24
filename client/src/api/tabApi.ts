import api from "./apiClient";

import type {
    GenerateTabRequest,
    GenerateResponse,
    SaveTabRequest,
    TabResponse,
    TabSummaryResponse
} from "@/types/tab";

export const getTabs =
    async (): Promise<TabSummaryResponse[]> => {
    const response =
        await api.get<TabSummaryResponse[]>("/tabs");

    return response.data;
};

export const getTabById =
    async (id: number): Promise<TabResponse> => {
    const response =
        await api.get<TabResponse>(`/tabs/${id}`);

    return response.data;
};

export const generateTab =
    async (data: GenerateTabRequest): Promise<GenerateResponse> => {
    const response =
        await api.post<GenerateResponse>("/tabs/generate", data);
        
    return response.data;
};

export const saveTab =
    async (data: SaveTabRequest): Promise<void> => {
    await api.post("/tabs", data);
};

export const deleteTab =
    async (id: number): Promise<void> => {
    await api.delete(`/tabs/${id}`);
};