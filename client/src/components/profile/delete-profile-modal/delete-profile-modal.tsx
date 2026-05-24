import React, { useCallback, useEffect } from "react";
import { useState } from "react";
import { useNavigate } from "react-router-dom";

import { deleteCurrentUser } from "@/api/userApi";
import { useAuthStore } from "@/store/authStore";
import { useUiStore } from "@/store/uiStore";
import { ROUTES } from "@/router/routes";

interface Props {
	isOpen: boolean;
	onClose: () => void;
}

export default function DeleteProfileModal({ isOpen, onClose }: Props) {
	const [loading, setLoading] = useState(false);
	const [error, setError] = useState<string | null>(null);

	const navigate = useNavigate();
	const logout = useAuthStore((s) => s.logout);
	const setMessage = useUiStore((s) => s.setMessage);

	useEffect(() => {
		if (isOpen) {
			setError(null);
		}
	}, [isOpen]);

	const handleOverlayClick = useCallback(
		(e: React.MouseEvent) => {
			if (e.target === e.currentTarget) {
				onClose();
			}
		},
		[onClose]
	);

	const handleDelete = async () => {
		setError(null);
		try {
			setLoading(true);
			await deleteCurrentUser();
			logout();
			navigate(ROUTES.HOME);
		} catch (err: any) {
			const msg = err?.response?.data?.message || "Не удалось удалить профиль.";
			setMessage({
				message: msg,
				type: "error"
			});
		} finally {
			setLoading(false);
		}
	};

	const overlayClass = isOpen ? "modal-overlay active" : "modal-overlay";

	return (
		<div className={overlayClass} onClick={handleOverlayClick}>
			<div className="modal-window">
				<button className="modal-close-button" onClick={onClose}>
					&times;
				</button>
				<h2 className="modal-title">Удаление профиля</h2>
				<p className="modal-text">
					Вы действительно хотите удалить профиль?
					<br />
					Это действие нельзя отменить.
				</p>
				<button
					className="profile-delete-button"
					onClick={handleDelete}
					disabled={loading}
				>
					{loading ? "Удаляю..." : "Удалить профиль"}
				</button>
				{error && <div className="form-error">{error}</div>}
			</div>
		</div>
	);
};