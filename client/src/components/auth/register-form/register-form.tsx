import { useState } from "react";
import { useNavigate } from "react-router-dom";

import { ROUTES } from "@/router/routes";
import { register as registerApi } from "@/api/authApi";
import { useAuthStore } from "@/store/authStore";
import { useUiStore } from "@/store/uiStore";

import type { RegisterRequest } from "@/types/auth";
import { extractErrorMessage } from "@/utils/error";

export default function RegisterForm() {
	const [username, setUsername] = useState("");
	const [email, setEmail] = useState("");
	const [password, setPassword] = useState("");
	const [confirmPassword, setConfirmPassword] = useState("");
	const [loading, setLoading] = useState(false);

	const navigate = useNavigate();

	const authLogin = useAuthStore((s) => s.login);
	const loadCurrentUser = useAuthStore((s) => s.loadCurrentUser);
	const setMessage = useUiStore((s) => s.setMessage);

	const handleSubmit = async (e: React.FormEvent) => {
		e.preventDefault();
		if (loading) return;

		if (!username || !email || !password || !confirmPassword) {
			setMessage({
				message: "Пожалуйста, заполните все поля.",
				type: "error"
			});

			return;
		}

		if (password !== confirmPassword) {
			setMessage({
				message: "Пароли не совпадают.",
				type: "error"
			});

			return;
		}

		const payload: RegisterRequest = {
			username,
			email,
			password,
		};

		try {
			setLoading(true);
			const resp = await registerApi(payload);
			authLogin(resp.token);
			await loadCurrentUser();
			setMessage({
				message: "Регистрация прошла успешно.",
				type: "success"
			});
			navigate(ROUTES.HOME);
		} catch (err: any) {
			setMessage({
				message: extractErrorMessage(
					err,
					"Не удалось выполнить регистрацию.",
				),
				type: "error",
			});
		} finally {
			setLoading(false);
		}
	};

	return (
		<main className="main">
			<section className="authorization-form">
				<h2 className="authorization-form-title">Регистрация</h2>
				<form onSubmit={handleSubmit}>
					<div className="input-wrapper">
						<input
							type="text"
							className="form-input"
							placeholder="Имя пользователя"
							value={username}
							onChange={(e) => setUsername(e.target.value)}
							required
						/>
					</div>
					<div className="input-wrapper">
						<input
							type="email"
							className="form-input"
							placeholder="E-mail"
							value={email}
							onChange={(e) => setEmail(e.target.value)}
							required
						/>
					</div>
					<div className="input-wrapper">
						<input
							type="password"
							className="form-input"
							placeholder="Пароль"
							value={password}
							onChange={(e) => setPassword(e.target.value)}
							required
						/>
					</div>
					<div className="input-wrapper">
						<input
							type="password"
							className="form-input"
							placeholder="Повторите пароль"
							value={confirmPassword}
							onChange={(e) => setConfirmPassword(e.target.value)}
							required
						/>
					</div>
					<button type="submit" className="authorization-form-button" disabled={loading}>
						{loading ? "Загрузка..." : "Зарегистрироваться"}
					</button>
					<a href="/login" className="form-link back-link">Вернуться назад</a>
				</form>
			</section>
		</main>
	);
};