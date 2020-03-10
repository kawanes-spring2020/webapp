#!/usr/bin/env bash
echo 'Starting Spring Boot'
cd '/home/ubuntu/webapp'
sudo mvn package
cd '/home/ubuntu/webapp/target'
sudo mkdir assets
cd '/home/ubuntu/webapp'
sudo kill -9 $(sudo lsof -t -i:8080)
sudo mvn spring-boot:run
