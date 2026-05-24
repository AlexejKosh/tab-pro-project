import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import { ROUTES } from "@/router/routes";
import { login as loginApi } from "@/api/authApi";
import { useAuthStore } from "@/store/authStore";
import { useUiStore } from "@/store/uiStore";

import lock from "@/assets/icons/lock.png";
import email from "@/assets/icons/email.png";

export default function LoginForm() {
    const [usernameOrEmail, setUsernameOrEmail] = useState("");
    const [password, setPassword] = useState("");
    const [loading, setLoading] = useState(false);

    const navigate = useNavigate();

    const authLogin = useAuthStore((s) => s.login);
    const loadCurrentUser = useAuthStore((s) => s.loadCurrentUser);
    const setMessage = useUiStore((s) => s.setMessage);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (loading) {
            return;
        }

        setLoading(true);

        try {
            const resp = await loginApi({ usernameOrEmail, password });
            authLogin(resp.token);
            await loadCurrentUser();
            setMessage({
                message: "Успешный вход",
                type: "success"
            });
            navigate(ROUTES.HOME);
        } catch (err: any) {
            const msg = err?.response?.data?.message || "Не удалось выполнить вход.";
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
                <h2 className="authorization-form-title">
                    Вход в аккаунт
                </h2>
                <form onSubmit={handleSubmit}>
                    <div className="input-wrapper">
                        <img
                            src={email}
                            className="input-icon"
                            alt="email"
                        />
                        <input
                            type="text"
                            className="form-input login-form--username"
                            placeholder="Имя пользователя/e-mail"
                            value={usernameOrEmail}
                            onChange={(e) =>
                                setUsernameOrEmail(e.target.value)
                            }
                            required
                        />
                    </div>
                    <div className="input-wrapper">
                        <img
                            src={lock}
                            className="input-icon password"
                            alt="password"
                        />
                        <input
                            type="password"
                            className="form-input login-form--password"
                            placeholder="Пароль"
                            value={password}
                            onChange={(e) =>
                                setPassword(e.target.value)
                            }
                            required
                        />
                    </div>
                    <Link
                        to={ROUTES.FORGOT_PASSWORD}
                        className="form-link forgot-password-link"
                    >
                        Забыли пароль?
                    </Link>
                    <button
                        type="submit"
                        className="authorization-form-button"
                    >
                        Войти
                    </button>
                    <span className="authorization-form-text">
                        Нет аккаунта?{" "}
                        <Link
                            to={ROUTES.REGISTER}
                            className="form-link"
                        >
                            Создать его
                        </Link>
                    </span>
                </form>
            </section>
        </main>
    );
};