import ForgotPasswordForm from "@/components/auth/forgot-password-form/forgot-password-form";

import { useDocumentTitle } from "@/hooks/use-document-title";

export default function ForgotPasswordPage() {
    useDocumentTitle("Восстановление пароля");

    return <ForgotPasswordForm />
};