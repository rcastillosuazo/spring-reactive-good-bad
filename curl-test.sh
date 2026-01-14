#!/bin/bash

BASE_URL="http://localhost:8080"

echo "======================================"
echo "▶️ Reactivo CORRECTO (no bloqueante)"
echo "======================================"
curl -v "$BASE_URL/correct?v=test"
echo -e "\n"

#echo "======================================"
#echo "⛔ Reactivo INCORRECTO (bloqueante)"
#echo "======================================"
#curl -v "$BASE_URL/incorrect?v=test"
#echo -e "\n"

echo "======================================"
echo "🔗 Flujo complejo ENCADENADO (A -> B)"
echo "======================================"
curl -v "$BASE_URL/complex/chained?v=test"
echo -e "\n"

echo "======================================"
echo "⚡ Flujo complejo PARALELO + JOIN"
echo "======================================"
curl -v "$BASE_URL/complex/parallel?v=test"
echo -e "\n"

echo "======================================"
echo "🔥 Concurrencia REAL (endpoint correcto)"
echo "======================================"
for i in {1..10}; do
  curl -s "$BASE_URL/correct?v=req-$i" &
done
wait
echo -e "\n"

#echo "======================================"
#echo "🔥 Concurrencia REAL (endpoint incorrecto)"
#echo "======================================"
#for i in {1..10}; do
#  curl -s "$BASE_URL/incorrect?v=req-$i" &
#done
#wait
#echo -e "\n"
#
#echo "======================================"
#echo "✅ FIN"
#echo "======================================"
