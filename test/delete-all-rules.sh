#!/bin/bash

# Script to delete all global and company rules from the MDM API
BASE_URL="http://localhost:8080"
MAX_ID=30

echo "Deleting all global rules..."
for id in $(seq 1 $MAX_ID); do
  curl -s -o /dev/null -w "Global rule $id: %{http_code}\n" -X DELETE "$BASE_URL/api/global-rules/$id"
done

echo "Deleting all company rules..."
for id in $(seq 1 $MAX_ID); do
  curl -s -o /dev/null -w "Company rule $id: %{http_code}\n" -X DELETE "$BASE_URL/api/company-rules/$id"
done

echo "Done." 