import { Outlet } from "react-router-dom";
import { useEffect } from "react";

import Header from "@/components/common/header/header";
import Footer from "@/components/common/footer/footer";
import MessageWindow from "@/components/common/message-window/message-window";
import { useAuthStore } from "@/store/authStore";
import { useTheme } from "@/hooks/use-theme";

export default function MainLayout() {
    const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
    const user = useAuthStore((s) => s.user);
    const loadCurrentUser = useAuthStore((s) => s.loadCurrentUser);

    useEffect(() => {
        if (isAuthenticated && !user) {
            loadCurrentUser();
        }
    }, [isAuthenticated, user, loadCurrentUser]);

    useTheme();

    return (
        <div className="app-layout">
            <Header />

            <MessageWindow />

            <Outlet />
            
            <Footer />
        </div>
    );
};