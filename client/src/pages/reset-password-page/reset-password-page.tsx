import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import ResetPasswordForm from "@/components/auth/reset-password-form/reset-password-form";
import { checkRecoverPasswordToken } from "@/api/authApi";
import { useUiStore } from "@/store/uiStore";
import { ROUTES } from "@/router/routes";

import { useDocumentTitle } from "@/hooks/use-document-title";

export default function ResetPasswordPage() {
    useDocumentTitle("Сброс пароля");

    const { token } = useParams<{ token?: string }>();
    const [checking, setChecking] = useState(true);

    const navigate = useNavigate();
    const setMessage = useUiStore((s) => s.setMessage);

    useEffect(() => {
        const run = async () => {
            if (!token) {
                setMessage({
                    message: "Неверная ссылка восстановления.",
                    type: "error"
                });
                navigate(ROUTES.LOGIN);

                return;
            }

            try {
                setChecking(true);
                const resp = await checkRecoverPasswordToken(token);

                if (!resp?.valid) {
                    setMessage({
                        message: "Ссылка восстановления пароля недействительна.",
                        type: "error"
                    });
                    navigate(ROUTES.LOGIN);

                    return;
                }
            } catch (err: any) {
                setMessage({
                    message: "Ссылка восстановления пароля недействительна.",
                    type: "error"
                });
                navigate(ROUTES.LOGIN);

                return;
            } finally {
                setChecking(false);
            }
        };
        run();
    }, [token, navigate, setMessage]);

    if (checking) {
        return null
    };

    return <ResetPasswordForm token={token as string} />;
};