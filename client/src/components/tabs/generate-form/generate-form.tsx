import type { Genre } from "@/types/genre";

interface Props {
    genres: Genre[];
    loading: boolean;

    genreId: number | null;
    signature: string;
    tonality: number;
    bpm: number;
    chordProgression: string;

    onGenreChange: (genreId: number) => void;
    onSignatureChange: (signature: string) => void;
    onTonalityChange: (tonality: number) => void;
    onBpmChange: (bpm: number) => void;
    onChordProgressionChange: (value: string) => void;
    onSubmit: (e: React.FormEvent<HTMLFormElement>) => void;
}

const GENRE_LABELS: Record<string, string> = {
	Rock: "Рок",
	Metal: "Метал",
	Blues: "Блюз",
};

const KEYS = [
	"C/Am",
	"Db/Bbm",
	"D/Bm",
	"Eb/Cm",
	"E/C#m",
	"F/Dm",
	"F#/Ebm",
	"G/Em",
	"Ab/Fm",
	"A/F#m",
	"Bb/Gm",
	"B/G#m",
];

export default function GenerateForm({
	genres,
	loading,
	genreId,
	signature,
	tonality,
	bpm,
	chordProgression,
	onGenreChange,
	onSignatureChange,
	onTonalityChange,
	onBpmChange,
	onChordProgressionChange,
	onSubmit,
}:Props) {

  	return (
		<form className="generate-form" onSubmit={onSubmit}>
			<div className="generate-form-top">
				<div className="form-group">
					<h2 className="form-title">Жанр</h2>
						<div className="genre-buttons">
							{genres.map((genre) => {
							const inputId = `genre-${genre.id}`;
							return (
								<div key={genre.id}>
								<input
									type="radio"
									id={inputId}
									name="genre"
									value={genre.id}
									checked={genreId === genre.id}
									onChange={() => onGenreChange(genre.id)}
									hidden
									disabled={loading}
								/>
								<label htmlFor={inputId} className="genre-button">
									{GENRE_LABELS[genre.name] ?? genre.name}
								</label>
								</div>
							);
							})}
						</div>
					</div>
				<div className="form-group">
					<h2 className="form-title">Размер</h2>
					<div className="signature-buttons">
						<div>
							<input
								type="radio"
								id="three-four"
								name="signature"
								value="3/4"
								checked={signature === "3/4"}
								onChange={() => onSignatureChange("3/4")}
								hidden
								disabled={loading}
							/>
							<label htmlFor="three-four" className="signature-button">
								3/4
							</label>
						</div>
						<div>
							<input
								type="radio"
								id="four-four"
								name="signature"
								value="4/4"
								checked={signature === "4/4"}
								onChange={() => onSignatureChange("4/4")}
								hidden
								disabled={loading}
							/>
							<label htmlFor="four-four" className="signature-button">
								4/4
							</label>
						</div>
					</div>
				</div>
			</div>
			<div className="form-group">
				<h2 className="form-title">Тональность</h2>
				<select
				name="tonality"
				id="tonality"
				className="tonality-input"
				value={tonality}
				onChange={(e) => onTonalityChange(Number(e.target.value))}
				disabled={loading}
				>
					{KEYS.map((item, index) => (
						<option key={item} value={index}>
							{item}
						</option>
					))}
				</select>
			</div>
			<div className="form-group">
				<h2 className="form-title">
					Темп (BPM): <span id="bpm-current"><b>{bpm}</b></span>
				</h2>
				<div className="bpm-container">
					<span className="bpm-value">50</span>
					<input
						type="range"
						id="bpm-slider"
						className="bpm-slider"
						min="50"
						max="200"
						value={bpm}
						onChange={(e) => onBpmChange(Number(e.target.value))}
						disabled={loading}
					/>
					<span className="bpm-value">200</span>
				</div>
			</div>
			<div className="form-group">
				<h2 className="form-title">Последовательность аккордов</h2>
				<textarea
					className="generate-textarea"
					placeholder="Введите ритм-партию..."
					value={chordProgression}
					onChange={(e) => onChordProgressionChange(e.target.value)}
					disabled={loading}
				/>
			</div>
			<button className="generate-button" type="submit" disabled={loading || genres.length === 0}>
				{loading ? "Генерация..." : "Сгенерировать"}
			</button>
		</form>
  	);
};