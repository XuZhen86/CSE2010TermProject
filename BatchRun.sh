#!/bin/bash

# Usage: ./BatchRun.sh startSeed endSeed
# Example: ./BatchRun.sh 1 10

sleep 10
for i in `seq $1 $2`; do
    printf "%d," $i >> Stats.csv
    java -Xmx2G -Xms128M BatchEvalBogglePlayer words.txt $i >> Stats.csv
done
