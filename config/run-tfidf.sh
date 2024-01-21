#!/bin/bash

# test the hadoop cluster by running TFIDF

# create input directory on HDFS
hadoop fs -mkdir -p input

# put input files to HDFS
hdfs dfs -put ./input/* input

# compile TFIDF.java and create a jar
javac -cp $(hadoop classpath) TFIDF.java
jar cf tfidf.jar TFIDF*.class

# run TFIDF
hadoop jar tfidf.jar TFIDF input termf documentf output $1 $2

# print the output of TFIDF
# echo -e "\ntfidf output:"
# hdfs dfs -cat output/part-r-00000