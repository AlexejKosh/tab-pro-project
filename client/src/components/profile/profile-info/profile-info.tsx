
import { useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import ChangePasswordForm from "@/components/profile/change-password-form/change-password-form";
import DeleteProfileModal from "@/components/profile/delete-profile-modal/delete-profile-modal";
import { useAuthStore } from "@/store/authStore";
import { useUiStore } from "@/store/uiStore";
import { ROUTES } from "@/router/routes";

export default function ProfileInfo() {
	const user = useAuthStore((s) => s.user);
	const logout = useAuthStore((s) => s.logout);
	const navigate = useNavigate();

	const [isDeleteOpen, setIsDeleteOpen] = useState(false);

	const createdAt = user?.createdAt ? new Date(user.createdAt) : null;

	const createdInfo = useMemo(() => {
		if (!createdAt) {
			return null;
		}

		const now = new Date();
		const diffMs = now.getTime() - createdAt.getTime();
		const days = Math.floor(diffMs / (1000 * 60 * 60 * 24));
		const dateStr = createdAt.toLocaleDateString();

		return { dateStr, days };
	}, [createdAt]);

	const setMessage = useUiStore((s) => s.setMessage);

	const handleLogout = () => {
		logout();
		setMessage({
			message: "Вы вышли из профиля.",
			type: "success"
		});
		navigate(ROUTES.HOME);
	};

	return (
		<main className="main main--usual-page">
			<Link to={ROUTES.HOME} className="form-link">На главную</Link>
			<h1 className="main-title main-title--usual-page">Профиль</h1>
			<h2 className="profile-username">{user?.username ?? "—"}</h2>
			<p className="main-text"><b>Email:</b> {user?.email ?? "—"}</p>
			<p className="main-text"><b>Имя пользователя:</b> {user?.username ?? "—"}</p>
			{createdInfo && (
				<p className="main-text">
					<b>Дата создания профиля:</b> {createdInfo.dateStr} <i>(уже {createdInfo.days} дней вместе с нами)</i>
				</p>
			)}
			<ChangePasswordForm />
			<button className="authorization-form-button logout-button" onClick={handleLogout}>
				Выйти из профиля
			</button>
			<button className="profile-delete-button" onClick={() => setIsDeleteOpen(true)}>
				Удалить профиль
			</button>
			<DeleteProfileModal isOpen={isDeleteOpen} onClose={() => setIsDeleteOpen(false)} />
		</main>
	);
};