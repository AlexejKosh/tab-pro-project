import RegisterForm from "@/components/auth/register-form/register-form";

import { useDocumentTitle } from "@/hooks/use-document-title";

export default function RegisterPage() {
    useDocumentTitle("Регистрация");
    
    return <RegisterForm />
};