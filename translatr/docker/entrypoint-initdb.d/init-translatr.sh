#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE USER translatr;
    CREATE DATABASE translatr;
    GRANT ALL PRIVILEGES ON DATABASE translatr TO translatr;
EOSQL
