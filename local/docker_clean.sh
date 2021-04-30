#! /usr/bin/env bash

echo "Stopping running instances..."
docker stop $(docker ps -q)

echo "Removing containers..."
docker rm $(docker ps -a -q)

echo "Removing all images..."
docker rmi $(docker images -a -q) -f

echo "Done."
