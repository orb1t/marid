-- NUMERICS

SET DATABASE DEFAULT TABLE TYPE CACHED;
SET DATABASE TRANSACTION CONTROL MVLOCKS;
SET DATABASE DEFAULT RESULT MEMORY ROWS 200000;
SET FILES CACHE ROWS 100000;
SET FILES SCALE 128;
SET FILES DEFRAG 30;
SET FILES WRITE DELAY 1;
SET FILES NIO FALSE;

CREATE TABLE NUMERICS(TAG BIGINT, TS TIMESTAMP, VAL DOUBLE, CONSTRAINT PK_NUMERICS PRIMARY KEY(TAG, TS));
CREATE INDEX IX_NUMERICS_TS ON NUMERICS(TS);