[![Java CI with Maven](https://github.com/DevOps2021-gb/devops2021/actions/workflows/maven.yml/badge.svg)](https://github.com/DevOps2021-gb/devops2021/actions/workflows/maven.yml)
[![SonarCloud](https://github.com/DevOps2021-gb/devops2021/actions/workflows/sonarcloud.yml/badge.svg)](https://github.com/DevOps2021-gb/devops2021/actions/workflows/sonarcloud.yml)
# devops2021  
Group B  
# Running locally  
### Running only website  
Simply run java project like any other project.  
### Running website   
Go to ./local folder
Either run "./run_local.sh"  
or the commands:  
sudo chmod +x setup_elk_local.sh  
source setup_elk_local.sh  
docker-compose -f docker-compose-local.yml up --build  
### Closing docker containers
Run the commands:  
docker-compose down -v --rmi 'all' --remove-orphans  
docker_clean.sh  
### Running simulator
Either run "run_sim.sh"  
or "python3 minitwit_simulator.py http://localhost:4567"  
