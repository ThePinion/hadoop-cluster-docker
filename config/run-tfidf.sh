#!/bin/bash

# test the hadoop cluster by running TFIDF

# create input files 
mkdir input
echo "Hello Docker Hello" >input/file2.txt
echo "Hello Hadoop" >input/file1.txt

# create input directory on HDFS
hadoop fs -mkdir -p input

# put input files to HDFS
hdfs dfs -put ./input/* input

# compile TFIDF.java and create a jar
javac -cp $(hadoop classpath) TFIDF.java
jar cf tfidf.jar TFIDF*.class

# run TFIDF
hadoop jar tfidf.jar TFIDF input termf output 

# print the input files
echo -e "\ninput file1.txt:"
hdfs dfs -cat input/file1.txt

echo -e "\ninput file2.txt:"
hdfs dfs -cat input/file2.txt

# print the output of TFIDF
echo -e "\ntfidf output:"
hdfs dfs -cat output/part-r-00000