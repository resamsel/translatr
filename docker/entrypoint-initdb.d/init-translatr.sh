#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE USER translatr;
    CREATE DATABASE translatr;
    GRANT ALL PRIVILEGES ON DATABASE translatr TO translatr;
    CREATE DATABASE "translatr-test";
    GRANT ALL PRIVILEGES ON DATABASE "translatr-test" TO translatr;
    CREATE DATABASE "translatr-load-test";
    GRANT ALL PRIVILEGES ON DATABASE "translatr-load-test" TO translatr;
EOSQL
