import { Navigate } from "react-router-dom";

import { useAuthStore } from "@/store/authStore";
import { ROUTES } from "./routes";

type Props = {
    children: React.ReactNode;
};

export const PrivateRoute = ({ children }: Props) => {

    const isAuthenticated =
        useAuthStore(
            state => state.isAuthenticated
        );

    if (!isAuthenticated) {
        return (
            <Navigate to={ROUTES.LOGIN} replace />
        );
    }

    return <>{children}</>;
};