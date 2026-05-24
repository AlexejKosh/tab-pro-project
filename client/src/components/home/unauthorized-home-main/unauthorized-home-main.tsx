import { useRef } from "react";
import { Link } from "react-router-dom";

import { ROUTES } from "@/router/routes";

import { useBannerInteractions } from "@/hooks/use-banner-interactions";
import { useTheme } from "@/hooks/use-theme";

export default function UnauthorizedHomeMain() {
    const rootRef = useRef<HTMLDivElement | null>(null);
    const { switchTheme } = useTheme();

    useBannerInteractions(rootRef);

    return (
        <main className="main" ref={rootRef}>
            <button
                type="button"
                className="change-theme-link"
                onClick={switchTheme}
            >
                Сменить тему
            </button>
            <section className="banner banner--hero">
                <div className="banner-particles" />
                <div className="banner-content">
                    <h2 className="banner-title">
                        Генерация соло
                    </h2>
                    <p className="banner-description">
                        Создавайте гитарные соло на основе аккордов: всего лишь
                        выберите стиль, а платформа с помощью искусственного
                        интеллекта сгенерирует табулатуру.
                    </p>
                </div>
            </section>
            <section className="banners">
                <Link
                    to={ROUTES.GENERATE}
                    className="banner banner--start"
                >
                    <div className="banner-particles" />
                    <div className="banner-content">
                        <h2 className="banner-title">
                            Быстрый старт
                        </h2>
                        <p className="banner-description">
                            Генерация табулатур без регистрации –
                            оцените возможности платформы за пару кликов
                        </p>
                    </div>
                </Link>
                <Link
                    to={ROUTES.LOGIN}
                    className="banner banner--account"
                >
                    <div className="banner-particles" />
                    <div className="banner-content">
                        <h2 className="banner-title">
                            Личный кабинет
                        </h2>
                        <p className="banner-description">
                            Зарегистрируйтесь, чтобы сохранять табулатуры
                            и возвращаться к ним в любое время
                        </p>
                    </div>
                </Link>
            </section>
        </main>
    );
};