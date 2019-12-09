#!/bin/sh

export MY_DB_URL="localhost";
export MY_DB_PASSWORD="root";

mvn clean spring-boot:run;
