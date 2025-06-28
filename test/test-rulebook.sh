#!/bin/bash

echo "Testing RuleBook Engine with sample entities..."

# Test data for entities
curl -X POST http://localhost:8081/api/merge/process \
  -H "Content-Type: application/json" \
  -d '{
    "entities": [
      {
        "id": "1",
        "type": "Organization",
        "name": "Acme Corporation",
        "address": "123 Main St, New York, NY",
        "email": "contact@acme.com",
        "phone": "555-123-4567",
        "sourceSystem": "CRM"
      },
      {
        "id": "2",
        "type": "Organization",
        "name": "ACME CORPORATION",
        "address": "456 Oak Ave, Los Angeles, CA",
        "email": "info@acme.com",
        "phone": "555-123-4567",
        "sourceSystem": "ERP"
      },
      {
        "id": "3",
        "type": "Organization",
        "name": "Different Company",
        "address": "123 Main St, New York, NY",
        "email": "contact@different.com",
        "phone": "555-987-6543",
        "sourceSystem": "CRM"
      }
    ],
    "companyId": "COMPANY_A",
    "domain": "lifescience"
  }'

echo ""
echo "Test completed. Check the logs for merge suggestions." 