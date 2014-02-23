--- create upload store ---

CREATE TABLE uploadStore (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version VARCHAR(128) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    status VARCHAR(64) NOT NULL,
    data BLOB(128M) NOT NULL
);