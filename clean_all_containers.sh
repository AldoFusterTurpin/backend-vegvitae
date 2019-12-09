#!/bin/sh

# delete all containers
docker rm $(docker ps -a -q) -f;

# delete all images
docker rmi -f $(docker images -a -q);

# delete all volumes
docker volume prune;