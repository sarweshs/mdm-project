#!/bin/bash

# Load Test Data Script for MDM Project
# This script loads global rules, company rules, and company overrides

echo "Loading test data for MDM Project..."

# Base URL
BASE_URL="http://localhost:8080"

# Function to load global rules
load_global_rules() {
    echo "Loading global rules..."
    
    # Load individual global rules
    for file in test/global-rules/*.json; do
        if [ -f "$file" ]; then
            echo "Loading $(basename "$file")..."
            curl -X POST "$BASE_URL/api/global-rules" \
                 -H "Content-Type: application/json" \
                 -d @"$file"
            echo -e "\n"
        fi
    done
    
    echo "--- Global rules loaded ---\n"
}

# Function to load company rules
load_company_rules() {
    echo "Loading company rules..."
    
    # Load individual company rules
    for file in test/company-rules/*.json; do
        if [ -f "$file" ]; then
            echo "Loading $(basename "$file")..."
            curl -X POST "$BASE_URL/api/company-rules" \
                 -H "Content-Type: application/json" \
                 -d @"$file"
            echo -e "\n"
        fi
    done
    
    echo "--- Company rules loaded ---\n"
}

# Function to load company overrides
load_company_overrides() {
    echo "Loading company overrides..."
    
    # Load individual company overrides
    for file in test/company-overrides/*.json; do
        if [ -f "$file" ]; then
            echo "Loading $(basename "$file")..."
            curl -X POST "$BASE_URL/api/company-rules" \
                 -H "Content-Type: application/json" \
                 -d @"$file"
            echo -e "\n"
        fi
    done
    
    echo "--- Company overrides loaded ---\n"
}

# Function to test effective rules
test_effective_rules() {
    echo "Testing effective rules for different companies..."
    
    echo "Company A (lifescience):"
    curl -s "$BASE_URL/api/company-rules/effective/COMPANY_A/lifescience" | jq .
    
    echo -e "\nCompany B (lifescience):"
    curl -s "$BASE_URL/api/company-rules/effective/COMPANY_B/lifescience" | jq .
    
    echo -e "\nCompany C (lifescience):"
    curl -s "$BASE_URL/api/company-rules/effective/COMPANY_C/lifescience" | jq .
    
    echo -e "\nCompany D (lifescience):"
    curl -s "$BASE_URL/api/company-rules/effective/COMPANY_D/lifescience" | jq .
    
    echo -e "\nTechnology domain (no company):"
    curl -s "$BASE_URL/api/global-rules/domain/technology" | jq .
}

# Main execution
echo "Starting test data load..."

# Load all data
load_global_rules
load_company_rules
load_company_overrides

# Test the endpoints
test_effective_rules

echo "Test data loading completed!" 