import React, { useState } from "react";

import { changePassword } from "@/api/userApi";
import { useUiStore } from "@/store/uiStore";

import type { ChangePasswordRequest } from "@/types/user";

export default function ChangePasswordForm() {
    const [oldPassword, setOldPassword] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [loading, setLoading] = useState(false);

    const setMessage = useUiStore((s) => s.setMessage);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!oldPassword || !newPassword || !confirmPassword) {
            setMessage({
                message: "Пожалуйста, заполните все поля.",
                type: "error"
            });
            
            return;
        }

        if (newPassword !== confirmPassword) {
            setMessage({
                message: "Пароли не совпадают.",
                type: "error"
            });

            return;
        }

        const payload: ChangePasswordRequest = {
            oldPassword,
            newPassword,
        };

        try {
            setLoading(true);
            await changePassword(payload);
            setOldPassword("");
            setNewPassword("");
            setConfirmPassword("");
            setMessage({
                message: "Пароль успешно изменён.",
                type: "success"
            });
        } catch (err: any) {
            const msg = err?.response?.data?.message || "Не удалось изменить пароль.";
            setMessage({
                message: msg,
                type: "error"
            });
        } finally {
            setLoading(false);
        }
    };

    return (
        <form className="profile-change-password-form" onSubmit={handleSubmit}>
            <p className="main-text"><b>Изменить пароль</b></p>
            <div className="input-wrapper">
                <input
                    type="password"
                    className="form-input"
                    placeholder="Текущий пароль"
                    required
                    value={oldPassword}
                    onChange={(e) => setOldPassword(e.target.value)}
                />
            </div>
            <div className="input-wrapper">
                <input
                    type="password"
                    className="form-input"
                    placeholder="Новый пароль"
                    required
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                />
            </div>
            <div className="input-wrapper">
                <input
                    type="password"
                    className="form-input"
                    placeholder="Повторите пароль"
                    required
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                />
            </div>
            <button type="submit" className="authorization-form-button" disabled={loading}>
                {loading ? "Сохраняю..." : "Изменить пароль"}
            </button>
        </form>
    );
};