import { useAuthStore } from "@/store/authStore";
import AuthorizedHomeMain from "@/components/home/authorized-home-main/authorized-home-main";
import UnauthorizedHomeMain from "@/components/home/unauthorized-home-main/unauthorized-home-main";

import { useDocumentTitle } from "@/hooks/use-document-title";

export default function HomePage() {
    useDocumentTitle("Главная страница");

    const isAuthenticated = useAuthStore(
        state => state.isAuthenticated
    );

    return isAuthenticated
        ? <AuthorizedHomeMain />
        : <UnauthorizedHomeMain />;
};