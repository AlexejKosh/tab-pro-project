import api from "./apiClient";

import type { Genre } from "@/types/genre";

export const getGenres =
    async (): Promise<Genre[]> => {
    const response =
        await api.get<Genre[]>("/genres");

    return response.data;
};

export const getGenreById =
    async (id: number): Promise<Genre> => {
    const response =
        await api.get<Genre>(
            `/genres/${id}`
        );
        
    return response.data;
};