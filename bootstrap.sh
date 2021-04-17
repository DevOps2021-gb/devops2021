#!/bin/sh

docker-compose down
docker rmi mertz/minitwit:latest
rm docker-compose.yml
rm prometheus.yml
rm filebeat.yml
rm nginx.conf
rm .htpasswd
rm setup_elk.sh

if [ -e heartbeat.sh]; then
	sh heartbeat.sh &
fi

wget https://raw.githubusercontent.com/DevOps2021-gb/devops2021/main/docker-compose.yml
wget https://raw.githubusercontent.com/DevOps2021-gb/devops2021/main/prometheus.yml
wget https://raw.githubusercontent.com/DevOps2021-gb/devops2021/main/filebeat.yml
wget https://raw.githubusercontent.com/DevOps2021-gb/devops2021/main/nginx.conf
wget https://raw.githubusercontent.com/DevOps2021-gb/devops2021/main/.htpasswd
wget https://raw.githubusercontent.com/DevOps2021-gb/devops2021/main/setup_elk.sh
sudo chmod +x setup_elk.sh
source setup_elk.sh
docker-compose up -d --build
