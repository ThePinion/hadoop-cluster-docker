#!/bin/bash

./build-image.sh
./start-container.sh
echo "start hadoop network..."
docker exec -it hadoop-master ./start-hadoop.sh
echo "start TFIDF..."
docker exec -it hadoop-master ./run-tfidf.sh $1 $2
