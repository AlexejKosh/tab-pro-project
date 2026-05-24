export interface ApiErrorResponse {
    message: string;
}

export interface UiMessage {
    message: string;
    type:
        | "error"
        | "success";
}