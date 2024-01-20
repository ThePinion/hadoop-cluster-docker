#!/bin/bash

echo ""

echo -e "\nbuild docker hadoop image\n"
docker build --memory --memory-swap -t kiwenlau/hadoop:1.0 .

echo ""