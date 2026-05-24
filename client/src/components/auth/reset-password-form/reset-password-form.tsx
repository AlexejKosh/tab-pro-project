import { useState } from "react";
import { useNavigate } from "react-router-dom";

import { recoverPassword } from "@/api/authApi";
import { useUiStore } from "@/store/uiStore";
import { ROUTES } from "@/router/routes";

interface Props {
    token: string;
}

export default function ResetPasswordForm({ token }: Props) {
    const [password1, setPassword1] = useState("");
    const [password2, setPassword2] = useState("");
    const [loading, setLoading] = useState(false);

    const setMessage = useUiStore((s) => s.setMessage);
    const navigate = useNavigate();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (loading){
            return;
        }

        if (!password1 || !password2) {
            setMessage({
                message: "Пожалуйста, заполните все поля.",
                type: "error"
            });
            
            return;
        }

        if (password1 !== password2) {
            setMessage({
                message: "Пароли не совпадают.",
                type: "error"
            });

            return;
        }

        try {
            setLoading(true);
            await recoverPassword(token, { password1, password2 });
            setMessage({
                message: "Пароль успешно сброшен.",
                type: "success"
            });
            navigate(ROUTES.LOGIN);
        } catch (err: any) {
            const msg = err?.response?.data?.message || "Не удалось сбросить пароль.";
            setMessage({
                message: msg,
                type: "error"
            });
        } finally {
            setLoading(false);
        }
    };

    return (
        <main className="main">
        <section className="authorization-form">
            <h2 className="authorization-form-title">Сброс пароля</h2>
            <form onSubmit={handleSubmit}>
            <div className="input-wrapper">
                <input
                type="password"
                className="form-input"
                placeholder="Пароль"
                value={password1}
                onChange={(e) => setPassword1(e.target.value)}
                required
                />
            </div>
            <div className="input-wrapper">
                <input
                type="password"
                className="form-input"
                placeholder="Повторите пароль"
                value={password2}
                onChange={(e) => setPassword2(e.target.value)}
                required
                />
            </div>
            <button type="submit" className="authorization-form-button" disabled={loading}>
                {loading ? "Загрузка..." : "Сбросить пароль"}
            </button>
            <a href="/" className="form-link back-link">На главную страницу</a>
            </form>
        </section>
        </main>
    );
};