import LoginForm from "@/components/auth/login-form/login-form";

import { useDocumentTitle } from "@/hooks/use-document-title";

export default function LoginPage() {
    useDocumentTitle("Вход");

    return <LoginForm />
};