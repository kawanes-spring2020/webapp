#!/usr/bin/env bash
sudo kill -9 $(sudo lsof -t -i:8080)
cd '/home/ubuntu/'
sudo rm -rf webapp


