import { useEffect, useState } from "react";

type Theme = "light" | "dark";

export const useTheme = () => {
    const [theme, setTheme] = useState<Theme>(() => {
        return (
            (localStorage.getItem("theme") as Theme)
            || "light"
        );
    });

    useEffect(() => {
        const themeLink =
            document.getElementById(
                "theme-style"
            ) as HTMLLinkElement;

        if (!themeLink) {
            return;
        }

        themeLink.href =
            theme === "light"
                ? "/src/styles/global-light-theme.css"
                : "/src/styles/global-dark-theme.css";
        localStorage.setItem("theme", theme);
    }, [theme]);

    const switchTheme = () => {
        setTheme((prev) =>
            prev === "light"
                ? "dark"
                : "light"
        );
    };

    return { theme, switchTheme };
};