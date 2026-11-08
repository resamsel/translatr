#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE USER translatr WITH PASSWORD 'translatr';
    GRANT ALL PRIVILEGES ON DATABASE translatr TO translatr;
    CREATE DATABASE "translatr-test";
    GRANT ALL PRIVILEGES ON DATABASE "translatr-test" TO translatr;
    CREATE DATABASE "translatr-load-test";
    GRANT ALL PRIVILEGES ON DATABASE "translatr-load-test" TO translatr;
    CREATE USER keycloak WITH PASSWORD 'keycloak';
    CREATE DATABASE "keycloak";
    GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak;
EOSQL

# Postgres 15+ no longer grants CREATE on the "public" schema to PUBLIC by
# default, so the DB owner (superuser $POSTGRES_USER) must hand it to the
# app users explicitly or Flyway/Keycloak's schema migrations fail with
# "permission denied for schema public".
for i in translatr translatr-test translatr-load-test; do
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" -d "$i" <<-EOSQL
    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
    GRANT ALL ON SCHEMA public TO translatr;
EOSQL
done

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" -d keycloak <<-EOSQL
    GRANT ALL ON SCHEMA public TO keycloak;
EOSQL