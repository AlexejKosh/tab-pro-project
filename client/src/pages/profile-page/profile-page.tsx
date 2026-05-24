import ProfileInfo from "@/components/profile/profile-info/profile-info";

import { useDocumentTitle } from "@/hooks/use-document-title";

export default function ProfilePage() {
    useDocumentTitle("Профиль");

    return <ProfileInfo />
};