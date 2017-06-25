---
-- #%L
-- marid-db
-- %%
-- Copyright (C) 2012 - 2017 MARID software development group
-- %%
-- This program is free software: you can redistribute it and/or modify
-- it under the terms of the GNU Affero General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
-- 
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU General Public License for more details.
-- 
-- You should have received a copy of the GNU Affero General Public License
-- along with this program.  If not, see <http://www.gnu.org/licenses/>.
-- #L%
---
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
