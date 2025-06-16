#!/bin/sh
docker exec -it postgres_db psql -d dondb -U admin -f /docker-entrypoint-initdb.d/mock_data.sql
