export function extractErrorMessage(
    error: any,
    defaultMessage: string = "Неизвестная ошибка",
): string {

    const responseData = error?.response?.data;

    if (responseData?.detail) {
        return String(responseData.detail);
    }

    if (responseData?.message) {
        try {
            const parsed = JSON.parse(responseData.message);

            if (parsed?.detail) {
                return String(parsed.detail);
            }
        } catch {
            // message не является JSON
        }

        return String(responseData.message);
    }

    if (error?.message) {
        return String(error.message);
    }

    return defaultMessage;
}