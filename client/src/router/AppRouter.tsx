import {
    BrowserRouter,
    Routes,
    Route
} from "react-router-dom";

import { ROUTES } from "./routes";

import { PrivateRoute } from "./PrivateRoute";
import { PublicRoute } from "./PublicRoute";

import HomePage from "@/pages/home-page/home-page";
import LoginPage from "@/pages/login-page/login-page";
import RegisterPage from "@/pages/register-page/register-page";
import ForgotPasswordPage from "@/pages/forgot-password-page/forgot-password-page";
import ResetPasswordPage from "@/pages/reset-password-page/reset-password-page";
import GeneratePage from "@/pages/generate-page/generate-page";
import SavedTabsPage from "@/pages/saved-tabs-page/saved-tabs-page";
import TabPage from "@/pages/tab-page/tab-page";
import ProfilePage from "@/pages/profile-page/profile-page";
import AboutPage from "@/pages/about-page/about-page";
import NotFoundPage from "@/pages/not-found-page/not-found-page";

import MainLayout from "@/components/layout/MainLayout";

export const AppRouter = () => {
    return (
        <BrowserRouter>
            <Routes>
                <Route element={<MainLayout />}>

                    {/* public */}
                    <Route
                        path={ROUTES.HOME}
                        element={<HomePage />}
                    />

                    <Route
                        path={ROUTES.LOGIN}
                        element={
                            <PublicRoute>
                                <LoginPage />
                            </PublicRoute>
                        }
                    />

                    <Route
                        path={ROUTES.REGISTER}
                        element={
                            <PublicRoute>
                                <RegisterPage />
                            </PublicRoute>
                        }
                    />

                    <Route
                        path={ROUTES.FORGOT_PASSWORD}
                        element={
                            <PublicRoute>
                                <ForgotPasswordPage />
                            </PublicRoute>
                        }
                    />

                    <Route
                        path="/reset-password"
                        element={
                            <PublicRoute>
                                <ResetPasswordPage />
                            </PublicRoute>
                        }
                    />

                    <Route
                        path={ROUTES.RESET_PASSWORD}
                        element={
                            <PublicRoute>
                                <ResetPasswordPage />
                            </PublicRoute>
                        }
                    />

                    <Route
                        path={ROUTES.ABOUT}
                        element={<AboutPage />}
                    />

                    <Route
                        path={ROUTES.GENERATE}
                        element={<GeneratePage />}
                    />

                    {/* private */}

                    <Route
                        path={ROUTES.TABS}
                        element={
                            <PrivateRoute>
                                <SavedTabsPage />
                            </PrivateRoute>
                        }
                    />
                    <Route
                        path={ROUTES.TAB}
                        element={
                            <PrivateRoute>
                                <TabPage />
                            </PrivateRoute>
                        }
                    />

                    <Route
                        path={ROUTES.PROFILE}
                        element={
                            <PrivateRoute>
                                <ProfilePage />
                            </PrivateRoute>
                        }
                    />

                    {/* fallback */}
                    <Route
                        path={ROUTES.NOT_FOUND}
                        element={<NotFoundPage />}
                    />
                </Route>
            </Routes>
        </BrowserRouter>
    );
};