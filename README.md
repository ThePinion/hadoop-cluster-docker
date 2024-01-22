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

## Benchmark results
These results were obained on a 20GB DDR4 RAM machine running Fedora Linux 39 on AMD Ryzen 4300U quad-core processor.
The contents of the ```/etc/docker/daemon.json``` were as follows:
```json
{
   "storage-driver": "devicemapper"
}

```
You can view the coresponding ```log``` files in the ```logs``` directory.
Precise execution times were as follows:

### Variable cluster size and data size
The number of mapps and reduces was set automatically by hadoop.
| slaves | mb | mstf   | msdf  | mstfidf |
|--------|----|--------|-------|---------|
| 5      | 1  | 25211  | 17544 | 16575   |
| 5      | 10 | 90866  | 18591 | 17526   |
| 5      | 45 | 230741 | 18601 | 20571   |
| 5      | 90 | 428981 | 21762 | 22543   |
| 3      | 1  | 25167  | 20600 | 21672   |
| 3      | 10 | 74791  | 22651 | 21556   |
| 3      | 45 | 217220 | 23563 | 25618   |
| 3      | 90 | 417399 | 25551 | 28560   |
| 1      | 1  | 25118  | 21589 | 20602   |
| 1      | 10 | 68861  | 23078 | 22007   |
| 1      | 45 | 193878 | 23585 | 25120   |
| 1      | 90 | 374437 | 25638 | 27577   |

### Variable data size 
The number of slaves was set to 5 and the number of mapps and reduces was set automatically by hadoop.
| mb | mstf   | msdf  | mstfidf |
|----|--------|-------|---------|
| 10 | 80955  | 18577 | 18598   |
| 20 | 137004 | 17630 | 18596   |
| 30 | 160536 | 18665 | 18566   |
| 40 | 212504 | 18555 | 20561   |
| 50 | 257359 | 20534 | 20550   |
| 60 | 314987 | 19648 | 21544   |
| 70 | 343254 | 19601 | 21557   |
| 80 | 379747 | 21676 | 22546   |
| 90 | 431258 | 21625 | 23591   |

### Variable mapps and reduces
The number of slaves was set to 5 and the data size was 10MB.
| mapps | reduces | mstf   | msdf  | mstfidf |
|-------|---------|--------|-------|---------|
| 5     | 1       | 81245  | 17525 | 17614   |
| 5     | 2       | 84036  | 18529 | 19568   |
| 5     | 8       | 91629  | 31808 | 33988   |
| 5     | 16      | 103759 | 57800 | 62350   |
| 25    | 1       | 80133  | 17648 | 17650   |
| 25    | 2       | 82374  | 18548 | 19588   |
| 25    | 8       | 92456  | 32770 | 36746   |
| 25    | 16      | 103158 | 56907 | 62368   |
| 250   | 1       | 78541  | 18558 | 18537   |
| 250   | 2       | 83190  | 18654 | 18576   |
| 250   | 8       | 92831  | 31798 | 34782   |
| 250   | 16      | 104270 | 55965 | 65161   |

### Legend
- **slaves** - number of slaves in the cluster
- **mb** - data size in MB (one book is around 250KB)
- **mstf** - Term Frequency stage running time in ms.
- **msdf** - Document Frequency stage runnin time in ms.
- **mstfidf** - TFIDF (final) stage running time in ms.
- **mapps** - the number of mapps set in config.
- **reduces** - the number of reduces set in config.