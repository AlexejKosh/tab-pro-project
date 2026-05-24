import { Link } from "react-router-dom";

import { useDocumentTitle } from "@/hooks/use-document-title";

export default function NotFoundPage() {
    useDocumentTitle("Страница не найдена");

    return (
        <main className="main main--info">
            <h1 className="main-title-error">Ошибка 404</h1>
            <p className="main-text">
                Данная страница не найдена.
            </p>
            <Link
                to="/"
                className="authorization-form-button login-button"
            >
                На главную
            </Link>
        </main>
    );
};