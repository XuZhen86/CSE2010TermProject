#!/bin/bash

for i in `seq 0 100000`; do
    printf "%d," $i >> Stats.csv
    java EvalBogglePlayer words.txt $i >> Stats.csv
done
