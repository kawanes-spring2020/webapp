#!/usr/bin/env bash
echo 'Starting Spring Boot'
sudo kill -9 $(sudo lsof -t -i:8080)
cd '/home/ubuntu/webapp'
sudo mvn clean package
cd '/home/ubuntu/webapp/target'
sudo mkdir assets
cd '/home/ubuntu/webapp/'
sudo mvn spring-boot:run
sudo kill -9 $(sudo lsof -t -i:8080)
cd '/home/ubuntu/webapp/target'
sudo java -jar CloudComputing-0.0.1-SNAPSHOT.jar

