export const ROUTES = {
    HOME: "/",
    LOGIN: "/login",
    REGISTER: "/register",

    FORGOT_PASSWORD: "/forgot-password",
    RESET_PASSWORD: "/reset-password/:token",

    GENERATE: "/generate",

    TABS: "/tabs",
    TAB: "/tabs/:id",

    PROFILE: "/profile",

    ABOUT: "/about",

    NOT_FOUND: "*"
} as const;