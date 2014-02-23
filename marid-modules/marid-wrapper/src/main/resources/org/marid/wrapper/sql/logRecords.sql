--- create log records table ---

CREATE TABLE logRecords (
    logger VARCHAR(1024) NOT NULL,
    level INTEGER NOT NULL,
    message LONGVARCHAR NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    parameters OBJECT ARRAY,
    thrown BIGINT REFERENCES throwns(id) ON DELETE SET NULL
);

--- create log records index ---

CREATE INDEX logRecordsIndexTimestampLevel ON logRecords (timestamp, level);