#!/bin/sh

# Execute the following commands in given order to run 2 containers:
# 1 container containing Sprinboot app
# 1 container containing PostgreSQL with one volume attached to it for persistence

export MY_DB_URL="postgres";
export MY_DB_PASSWORD="root";

mvn clean;
mvn package;

# creates a volume for the database
echo "Create volume used in Postgresql";
docker create -v /var/lib/postgresql/data --name PostgresData alpine;

# creates an image from the Dockerfile
echo "Create Sprinboot image";
docker build ./ -t springbootapp;

# shows docker info
echo "Images:" && docker image ls && echo "\nContainers:" && docker ps -a && echo "\nVolumes:" && docker volume ls;

# start and link containers
echo "Doing docker-compose up";
docker-compose --file docker-compose-dev.yml up;

# to inspect postgres contanier
# docker exec -it postgres bash
# psql -U postgres

# SELECT * FROM pg_catalog.pg_tables WHERE schemaname != 'pg_catalog' AND schemaname != 'information_schema';
