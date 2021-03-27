#!/bin/sh
sudo chmod +x setup_elk.sh
source setup_elk.sh

printf "nime:$(openssl passwd -crypt 123)\n" > .htpasswd

docker-compose -f docker-compose-local.yml up --build
