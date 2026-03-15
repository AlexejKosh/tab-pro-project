-- 1. Пользователи
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(25) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Жанры
CREATE TABLE IF NOT EXISTS genres (
    id SERIAL PRIMARY KEY,
    name VARCHAR(25) UNIQUE NOT NULL
);

-- 3. Табулатуры
CREATE TABLE IF NOT EXISTS tabs (
    id SERIAL PRIMARY KEY,

    user_id INTEGER NOT NULL
        REFERENCES users(id)
        ON DELETE CASCADE,

    genre_id INTEGER NOT NULL
        REFERENCES genres(id)
        ON DELETE RESTRICT,

    title VARCHAR(50) NOT NULL,
    signature VARCHAR(5) NOT NULL,

    chord_progression JSONB NOT NULL,
    tab_data JSONB NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Предзаполнение жанров
MERGE INTO genres (name) KEY(name) VALUES
('Rock'),
('Blues'),
('Funk');