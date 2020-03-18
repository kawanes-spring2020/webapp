#!/usr/bin/env bash
echo 'Starting Spring Boot'
sudo kill -9 $(sudo lsof -t -i:8080)
cd '/home/ubuntu/webapp'
sudo rm -rf file_config.json
sudo cp -r file_config.json /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.d/
sudo systemctl restart amazon-cloudwatch-agent
sudo mvn clean package
cd '/home/ubuntu/webapp/target'
sudo mkdir assets
sudo java -jar CloudComputing-0.0.1-SNAPSHOT.jar  > /dev/null 2> /dev/null < /dev/null &

