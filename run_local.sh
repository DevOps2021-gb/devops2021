#!/bin/sh
sudo chmod +x setup_elk.sh
source setup_elk.sh

printf "USERNAME:$(openssl passwd -crypt PASSWORD)\n" > .htpasswd

docker-compose -f docker-compose-local.yml up --build
