import ProjectInfo from "@/components/about/project-info/project-info";

import { useDocumentTitle } from "@/hooks/use-document-title";

export default function AboutPage() {
    useDocumentTitle("О проекте");

    return <ProjectInfo />
};