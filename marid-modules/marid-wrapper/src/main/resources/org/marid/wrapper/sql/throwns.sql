--- create throwns table ---

CREATE TABLE throwns (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    type VARCHAR(1024) NOT NULL,
    message LONGVARCHAR,
    cause BIGINT REFERENCES throwns(id) ON DELETE SET NULL,
    fileName VARCHAR(1024) ARRAY,
    className VARCHAR(1024) ARRAY,
    methodName VARCHAR(1024) ARRAY,
    lineNumber INTEGER ARRAY,
    supressed BIGINT ARRAY
);
