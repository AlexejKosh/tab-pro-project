import { Link } from "react-router-dom";

import { useAuthStore } from "@/store/authStore";
import { ROUTES } from "@/router/routes";

import logo from "@/assets/logo/logo.png";

export default function Header() {
    const isAuthenticated =
        useAuthStore(
            state => state.isAuthenticated
        );

    const user =
        useAuthStore(
            state => state.user
        );

    return (
        <header className="header">
            <Link to={ROUTES.HOME} className="logo">
                <img src={logo} alt="Logo" />
            </Link>

            {!isAuthenticated ? (
                <Link
                    to={ROUTES.LOGIN}
                    className="header-profile-text"
                >
                    Войти
                </Link>
            ) : (
                <Link
                    to={ROUTES.PROFILE}
                    className="header-profile-text"
                >
                    {user?.username}
                </Link>
            )}
            
        </header>
    );
};