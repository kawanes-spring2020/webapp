#!/usr/bin/env bash
echo 'Starting Spring Boot'
cd '/home/ubuntu/webapp'
sudo mvn clean package
cd '/home/ubuntu/webapp/target'
sudo mkdir assets
cd '/home/ubuntu/webapp/'
sudo kill -9 $(sudo lsof -t -i:8080)
sudo nohup mvn spring-boot:run

