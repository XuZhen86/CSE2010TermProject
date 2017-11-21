#!/bin/bash

# Usage: ./BatchRun.sh startSeed endSeek
# Example: ./BatchRun.sh 1 10

sleep 5
for i in `seq $1 $2`; do
    printf "%d," $i >> Stats.csv
    java -Xmx2G BatchEvalBogglePlayer words.txt $i >> Stats.csv
done
