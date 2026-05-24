import { useState } from "react";
import { useNavigate } from "react-router-dom";

import { sendRecoverPasswordMail } from "@/api/authApi";
import { useUiStore } from "@/store/uiStore";
import { ROUTES } from "@/router/routes";

export default function ForgotPasswordForm() {
	const [email, setEmail] = useState("");
	const [loading, setLoading] = useState(false);

	const navigate = useNavigate();
	const setMessage = useUiStore((s) => s.setMessage);

	const handleSubmit = async (e: React.FormEvent) => {
		e.preventDefault();

		if (loading) {
			return;
		}

		if (!email) {
			setMessage({
				message: "Пожалуйста, укажите e-mail.", 
				type: "error"
			});

			return;
		}

		try {
			setLoading(true);
			const resp = await sendRecoverPasswordMail({ email });
			setMessage({
				message: resp?.message || "Письмо отправлено.",
				type: "success"
			});
			navigate(ROUTES.LOGIN);
		} catch (err: any) {
			const msg = err?.response?.data?.message || "Не удалось отправить письмо.";
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
				<h2 className="authorization-form-title">Восстановление пароля</h2>
				<form onSubmit={handleSubmit}>
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
					<span className="authorization-form-text authorization-form-text--forgot-password">
						Введите ваш email, и мы отправим вам инструкции по восстановлению пароля.
					</span>
					<button type="submit" className="authorization-form-button" disabled={loading}>
						{loading ? "Загрузка..." : "Восстановить пароль"}
					</button>
					<a href="/login" className="form-link back-link">Вернуться назад</a>
				</form>
			</section>
		</main>
	);
};