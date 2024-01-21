#!/bin/bash

./build-image.sh
./start-container.sh
echo "start hadoop network..."
sudo docker exec -it hadoop-master ./start-hadoop.sh
echo "start TFIDF..."
sudo docker exec -it hadoop-master ./run-tfidf.sh
