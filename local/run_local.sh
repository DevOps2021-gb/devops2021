#!/bin/bash
sudo chmod +x setup_elk_local.sh
source setup_elk_local.sh

docker-compose -f docker-compose-local.yml up --build
