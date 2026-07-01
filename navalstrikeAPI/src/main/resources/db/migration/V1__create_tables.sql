
-- Tabela de usuários
CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       name VARCHAR(255) NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL
);

-- Tokens revogados
CREATE TABLE revoked_tokens (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                token_id VARCHAR(255) NOT NULL UNIQUE,
                                expires_at TIMESTAMP NOT NULL
);

-- Tabuleiro
CREATE TABLE board (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid()
);

-- Navios
CREATE TABLE ships (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       board_id UUID NOT NULL REFERENCES board(id),
                       ship_type VARCHAR(50) NOT NULL
);

-- Coordenadas dos navios
CREATE TABLE ship_coordinates (
                                  ship_id UUID NOT NULL REFERENCES ships(id),
                                  x INT NOT NULL,
                                  y INT NOT NULL
);

-- Hits nos navios
CREATE TABLE ship_hits (
                           ship_id UUID NOT NULL REFERENCES ships(id),
                           x INT NOT NULL,
                           y INT NOT NULL
);

-- Partida
CREATE TABLE matches (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       status VARCHAR(50) NOT NULL,
                       board_player_1_id UUID REFERENCES board(id),
                       board_player_2_id UUID REFERENCES board(id),
                       player_1 UUID REFERENCES users(id),
                       player_2 UUID REFERENCES users(id)
);