-- 1. Пользователи
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(25) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE
        DEFAULT CURRENT_TIMESTAMP
);

-- 2. Токены восстановления пароля
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL
        REFERENCES users(id)
        ON DELETE CASCADE,
    token VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- 3. Жанры
CREATE TABLE IF NOT EXISTS genres (
    id SERIAL PRIMARY KEY,
    name VARCHAR(25) UNIQUE NOT NULL
);

-- 4. Табулатуры
CREATE TABLE IF NOT EXISTS tabs (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL
        REFERENCES users(id)
        ON DELETE CASCADE,
    genre_id INTEGER NOT NULL
        REFERENCES genres(id)
        ON DELETE RESTRICT,
    title VARCHAR(50) NOT NULL,
    chord_progression TEXT NOT NULL,
    signature VARCHAR(5) NOT NULL,
    music_key INTEGER NOT NULL
        CHECK (music_key BETWEEN 0 AND 11),
    bpm INTEGER NOT NULL
        CHECK (bpm BETWEEN 50 AND 200),
    tab_data TEXT NOT NULL,
    audio_url TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE
        DEFAULT CURRENT_TIMESTAMP
);

-- Предзаполнение жанров
INSERT INTO genres (name) VALUES
('Blues'),
('Metal'),
('Rock');