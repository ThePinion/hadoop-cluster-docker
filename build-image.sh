#!/bin/bash

echo ""

echo -e "\nbuild docker hadoop image\n"
docker build --ulimit nofile=122880:122880 --memory --memory-swap -t kiwenlau/hadoop:1.0 .

echo ""