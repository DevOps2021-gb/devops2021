#!/bin/bash
sudo chmod +x ../setup_elk.sh
source ../setup_elk.sh

docker-compose -f docker-compose-local.yml up --build
