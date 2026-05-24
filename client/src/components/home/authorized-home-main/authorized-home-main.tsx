import { useRef } from "react";
import { Link } from "react-router-dom";

import { ROUTES } from "@/router/routes";

import { useBannerInteractions } from "@/hooks/use-banner-interactions";
import { useTheme } from "@/hooks/use-theme";

export default function AuthorizedHomeMain() {
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
            <section className="banners">
                <Link
                    to={ROUTES.GENERATE}
                    className="banner banner--generate"
                >
                    <div className="banner-particles" />
                    <div className="banner-content">
                        <p className="banner-description">
                            <b>
                                Генерация<br />
                                табулатуры
                            </b>
                        </p>
                    </div>
                </Link>
                <Link
                    to={ROUTES.TABS}
                    className="banner banner--saved"
                >
                    <div className="banner-particles" />
                    <div className="banner-content">
                        <p className="banner-description">
                            <b>
                                Сохранённые<br />
                                табулатуры
                            </b>
                        </p>
                    </div>
                </Link>
            </section>
        </main>
    );
};