# TFIDF using Hadoop MapReduce

Algorithm description and some implementation intuition is located in the ```Description.pdf``` file (in polish).
The implementation is located in the ```config/TFIDF.java``` file.

## Running the benchmarks
To run the benchmarks:
```bash
python3 bench_runner.py
```
The results of the benchmarsk will be generated in the ```benchs``` directory. 
The logs of the subsequent benchmark runs will be dumped to the ```logs``` directory.
The filenames are of the form:
```{cluster_size}_{data_size_mb}_{mapps}_{reduces}_log.txt```
If the ```mapps``` or ```reduces``` value equals 0, the hadoop framework will choose the appropriate number automatically.
You can also run the benchmark by manually populating the ```books_temp``` directory and running ```./bench.sh m r```. Where ```m``` and ```r``` are mapps and reduces values respectively. 

## Dataset
The dataset is located in the ```book``` directory. There are almost 100MB of books. Each time a benchmark is run, a portion of these books is copied to the ```books_temp``` directory. Which is then put inside the docker container.

## Results
These results were obained on a 16GB DDR4RAM machine running Fedora Linux 39 on Inteli3 9100F processor.
The contents of the ```/etc/docker/daemon.json``` were as follows:
```json
{
   "storage-driver": "devicemapper"
}

```
You can view the coresponding ```log``` files in the ```logs``` directory.
Precise execution times were as follows:
s
