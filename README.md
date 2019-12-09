# Backend VegVitae

This is the repository for the backend for PES application VegVitae. It implements a REST API using Java, Spring Boot and JavaPersistence/Hibernate.

NOTE:
The Springboot API depends on $MY_DB_URL and $MY_DB_PASSWORD environment variables to run properly, the below scripts already set them correctly this is done
to avoid hardcoding credentials (for the deployment of the app)

NOTE: execute
```
$ chmod +x [script_file.sh]
``` 
to gain execution permission on the script

EXECUTE THE APP (method 1 using Docker), recommended.
You need Docker(https://www.docker.com), Maven(https://maven.apache.org) and Java(https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
Note: if you are on Windows use Git Bash (https://gitforwindows.org)

1) Execute the following script to launch the application (it uses Docker and Docker-Compose):
```
   $ source run_with_docker.sh
``` 
   It will run 2 containers:
   1 container of Sprinboot app
   1 container of PostgreSQL with one volume attached to it for persistence
   To test the app you can access localhost:8080 from Postman

2) Once you have finished testing the app, you can clean your environment executing the following script and press 'y' and enter when a question is prompted.
IMPORTANT: this will erase all your containers and volumes
```
   $ source clean_all_containers.sh
```

_------------------------------------------------------------------------------------------------

EXECUTE THE APP (method 2)
Execute the following command to launch the application natively without Docker, you will need to set Postgres DB Manually.
1) run the script set_env_dev_without_docker.sh, it will set some environment variables:
```
    $ source run_without_docker.sh
```

Backend is made by:
  * Aldo
  * Aaron
  * Marc
  * Ruben

PES FIB 2019-2020