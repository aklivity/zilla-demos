#!/bin/bash

echo -e "Running test with 1 sec intervals \n"
for i in {1..5}
do
    sleep 1
    timestamp=$(date +%s)
    curl --location 'http://localhost:8080/taxiroute.TaxiRoute/CreateTaxi' \
    --header 'Content-Type: application/json' \
    --header 'Accept: application/json' \
    --header 'Idempotency-Key: '"$timestamp"'' \
    --data '{"key":"'"$timestamp"'","bbox":[-121.888074,37.328473,27.5,-121.879609,37.333059,29.65],"distance":0.927,"duration":179.3,"coordinates":[[-121.888074,37.328473,29],[-121.886911,37.329319,29],[-121.886852,37.329337,29],[-121.886696,37.329454,29],[-121.886581,37.329545,29],[-121.886246,37.329703,29],[-121.88636,37.32985,29],[-121.886261,37.329896,29],[-121.886005,37.330029,29],[-121.885725,37.330174,29],[-121.885224,37.330411,28.7],[-121.884953,37.330542,28.5],[-121.883928,37.331029,27.5],[-121.883117,37.331408,27.7],[-121.882838,37.331528,28],[-121.88212,37.331865,29],[-121.882067,37.331889,29.2],[-121.881741,37.332029,29.6],[-121.881154,37.332321,29.7],[-121.880664,37.332554,29.2],[-121.880145,37.332806,28.7],[-121.879967,37.332891,28.6],[-121.879631,37.333049,28.3],[-121.879609,37.333059,28.2]]}'
    echo -e ""
done

echo -e "Finished, waiting... \n"
sleep 5

echo -e "Running test with 10 fast calls \n"
for i in {1..10}
do
    timestamp=$(date +%s)
    curl --location 'http://localhost:8080/taxiroute.TaxiRoute/CreateTaxi' \
    --header 'Content-Type: application/json' \
    --header 'Accept: application/json' \
    --header 'Idempotency-Key: '"$timestamp"'' \
    --data '{"key":"'"$timestamp"'","bbox":[-121.888074,37.328473,27.5,-121.879609,37.333059,29.65],"distance":0.927,"duration":179.3,"coordinates":[[-121.888074,37.328473,29],[-121.886911,37.329319,29],[-121.886852,37.329337,29],[-121.886696,37.329454,29],[-121.886581,37.329545,29],[-121.886246,37.329703,29],[-121.88636,37.32985,29],[-121.886261,37.329896,29],[-121.886005,37.330029,29],[-121.885725,37.330174,29],[-121.885224,37.330411,28.7],[-121.884953,37.330542,28.5],[-121.883928,37.331029,27.5],[-121.883117,37.331408,27.7],[-121.882838,37.331528,28],[-121.88212,37.331865,29],[-121.882067,37.331889,29.2],[-121.881741,37.332029,29.6],[-121.881154,37.332321,29.7],[-121.880664,37.332554,29.2],[-121.880145,37.332806,28.7],[-121.879967,37.332891,28.6],[-121.879631,37.333049,28.3],[-121.879609,37.333059,28.2]]}'
    echo -e ""
done

echo -e "Finished, waiting... \n"
sleep 5

echo -e "Running test with 500 fast calls \n"
timestamp=$(date +%s)
for i in {1..500}
do
    sleep 0.01
    ((timestamp++))
    curl --location 'http://localhost:8080/taxiroute.TaxiRoute/CreateTaxi' \
    --header 'Content-Type: application/json' \
    --header 'Accept: application/json' \
    --header 'Idempotency-Key: '"$timestamp"'' \
    --data '{"key":"'"$timestamp"'","bbox":[-121.888074,37.328473,27.5,-121.879609,37.333059,29.65],"distance":0.927,"duration":179.3,"coordinates":[[-121.888074,37.328473,29],[-121.886911,37.329319,29],[-121.886852,37.329337,29],[-121.886696,37.329454,29],[-121.886581,37.329545,29],[-121.886246,37.329703,29],[-121.88636,37.32985,29],[-121.886261,37.329896,29],[-121.886005,37.330029,29],[-121.885725,37.330174,29],[-121.885224,37.330411,28.7],[-121.884953,37.330542,28.5],[-121.883928,37.331029,27.5],[-121.883117,37.331408,27.7],[-121.882838,37.331528,28],[-121.88212,37.331865,29],[-121.882067,37.331889,29.2],[-121.881741,37.332029,29.6],[-121.881154,37.332321,29.7],[-121.880664,37.332554,29.2],[-121.880145,37.332806,28.7],[-121.879967,37.332891,28.6],[-121.879631,37.333049,28.3],[-121.879609,37.333059,28.2]]}'
    echo -e ""
done
