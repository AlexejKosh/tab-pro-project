import { Link, useLocation } from "react-router-dom";

import { useAuthStore } from "@/store/authStore";
import { ROUTES } from "@/router/routes";

type FooterLink = {
    to: string;
    label: string;
};

export default function Footer() {
    const { pathname } = useLocation();

    const isAuthenticated = useAuthStore(
        state => state.isAuthenticated
    );

    const getLinks = (): FooterLink[] => {
        if (pathname === ROUTES.HOME) {
            return isAuthenticated
                ? [
                    {
                        to: ROUTES.PROFILE,
                        label: "Профиль"
                    },
                    {
                        to: ROUTES.GENERATE,
                        label: "Генерация табулатуры"
                    },
                    {
                        to: ROUTES.ABOUT,
                        label: "О проекте"
                    }
                ]
                : [
                    {
                        to: ROUTES.REGISTER,
                        label: "Регистрация"
                    },
                    {
                        to: ROUTES.GENERATE,
                        label: "Генерация табулатуры"
                    },
                    {
                        to: ROUTES.ABOUT,
                        label: "О проекте"
                    }
                ];
        }

        if (
            pathname === ROUTES.ABOUT ||
            pathname === ROUTES.GENERATE
        ) {
            return isAuthenticated
                ? [
                    {
                        to: ROUTES.HOME,
                        label: "Главная страница"
                    },
                    {
                        to: ROUTES.PROFILE,
                        label: "Профиль"
                    },
                    {
                        to: ROUTES.ABOUT,
                        label: "О проекте"
                    }
                ]
                : [
                    {
                        to: ROUTES.HOME,
                        label: "Главная страница"
                    },
                    {
                        to: ROUTES.REGISTER,
                        label: "Регистрация"
                    },
                    {
                        to: ROUTES.GENERATE,
                        label: "Генерация табулатуры"
                    }
                ];
        }

        if (pathname.startsWith("/tabs")) {
            return [
                {
                    to: ROUTES.HOME,
                    label: "Главная страница"
                },
                {
                    to: ROUTES.PROFILE,
                    label: "Профиль"
                },
                {
                    to: ROUTES.ABOUT,
                    label: "О проекте"
                }
            ];
        }

        return [
            {
                to: ROUTES.HOME,
                label: "Главная страница"
            },
            {
                to: ROUTES.GENERATE,
                label: "Генерация табулатуры"
            },
            {
                to: ROUTES.ABOUT,
                label: "О проекте"
            }
        ];
    };

    const links = getLinks();

    return (
        <footer className="footer">
            <div className="footer-links">
                {links.map((link) => (
                    <Link key={link.to} to={link.to}>
                        {link.label}
                    </Link>
                ))}
            </div>
            <span className="footer-copy">
                Кошелев Алексей, гр. 4321-22, 2026 г.
            </span>
        </footer>
    );
};