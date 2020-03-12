#!/usr/bin/env bash
echo 'Starting Spring Boot'
cd '/home/ubuntu/webapp'
sudo mvn clean package
cd '/home/ubuntu/webapp/target'
sudo mkdir assets
cd '/home/ubuntu/webapp/'
sudo nohup mvn spring-boot:run  > /dev/null 2> /dev/null < /dev/null &

