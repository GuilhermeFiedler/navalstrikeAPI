CREATE TABLE board_misses (
                              board_id UUID NOT NULL REFERENCES board(id),
                              x INT NOT NULL,
                              y INT NOT NULL
);