#!/usr/bin/env bash
echo 'Starting Spring Boot'
sudo kill -9 $(sudo lsof -t -i:8080)
cd '/home/ubuntu/webapp'
sudo cp -r file_config.json /opt/aws/amazon-cloudwatch-agent/etc/
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
    -a fetch-config \
    -m ec2 \
    -c file:/opt/aws/amazon-cloudwatch-agent/etc/file_config.json \
    -s
sudo systemctl restart amazon-cloudwatch-agent
sudo mvn clean package
cd '/home/ubuntu/webapp/target'
sudo mkdir assets
sudo java -jar CloudComputing-0.0.1-SNAPSHOT.jar  > /dev/null 2> /dev/null < /dev/null &

