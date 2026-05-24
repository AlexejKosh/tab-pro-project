
import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";

import TabInfo from "@/components/tabs/tab-info/tab-info";
import { getTabById, deleteTab } from "@/api/tabApi";
import { useUiStore } from "@/store/uiStore";

import { useDocumentTitle } from "@/hooks/use-document-title";

import type { TabResponse } from "@/types/tab";

export default function TabPage() {
	useDocumentTitle("Моя табулатура");

	const { id } = useParams();
	const navigate = useNavigate();

	const [tab, setTab] = useState<TabResponse | null>(null);
	const [loading, setLoading] = useState<boolean>(true);

	useEffect(() => {
		if (!id) {
			return;
		}

		const load = async () => {
			try {
				const data = await getTabById(Number(id));
				setTab(data);
			} catch (err) {
				navigate("/tabs");
			} finally {
				setLoading(false);
			}
		};
		load();
	}, [id, navigate]);

	const setMessage = useUiStore((s) => s.setMessage);
	const handleDelete = async () => {
		if (!tab) {
			return;
		}

		try {
			await deleteTab(tab.id);
			setMessage({
				message: "Табулатура удалена.",
				type: "success"
			});
			navigate("/tabs");
		} catch (err: any) {
			const msg = err?.response?.data?.message || "Не удалось удалить табулатуру.";
			setMessage({
                message: msg,
                type: "error"
            });
		}
	};

	if (loading) {
		return (
			<main className="main main--usual-page">
				<p className="main-text">Загрузка...</p>
			</main>
		);
	}

	if (!tab) {
		return (
			<main className="main main--usual-page">
				<p className="main-text">Табулатура не найдена.</p>
			</main>
		);
	}

	return (
		<main className="main main--usual-page">
			<a
                href="/tabs"
                className="form-link"
            >
                Назад
            </a>
			<TabInfo
				title={tab.title}
				genreId={tab.genreId}
				tempo={tab.bpm}
				signature={tab.signature}
				keyIndex={tab.musicKey}
				chords={tab.chordProgression}
				tabData={tab.tabData}
				audioData={tab.audioData}
			/>
				<button
					className="generate-button generate-button--delete"
					onClick={handleDelete}
				>
					Удалить
				</button>
		</main>
	);
}