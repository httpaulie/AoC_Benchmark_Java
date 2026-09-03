#!/bin/bash

# ==========================================
QTD_EXECUCOES=30       # Total de vezes que a JVM é reiniciada (forks)
WARMUPS=10             # Iterações de aquecimento
FILTRO_ATOMO=".*"      # Filtro de execução. Deixe ".*" para rodar todos
#FILTRO_ATOMO="conditionalOperator"
# ==========================================

echo "Starting $QTD_EXECUCOES runs for: $FILTRO_ATOMO"

for i in $(seq 1 $QTD_EXECUCOES); do
    echo "Run $i of $QTD_EXECUCOES..."
    
    taskset -c 0 java -jar target/benchmarks.jar "$FILTRO_ATOMO" \
        -f 1 -wi $WARMUPS -i 1 \
        -prof "perfnorm:events=instructions,cycles,branches,branch-misses,L1-dcache-loads,L1-dcache-stores,L1-dcache-load-misses,L1-dcache-store-misses" \
        -rf json -rff "run_${i}.json" > /dev/null
done

echo "Data Collection Finished!"