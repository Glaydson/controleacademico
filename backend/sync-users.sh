#!/bin/bash

# Script to sync Keycloak users to backend database

echo "=== Getting admin token from Keycloak ==="

# Get admin token using client credentials (no username/password needed)
TOKEN_RESPONSE=$(curl -s -X POST "http://localhost:8080/realms/controle-academico/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=academico-backend" \
  -d "client_secret=Xk2NkMGx2huDBloo3wTW1DSWefPViYXW")

echo "Token response: $TOKEN_RESPONSE"

# Extract access token
ACCESS_TOKEN=$(echo $TOKEN_RESPONSE | jq -r '.access_token')

if [ "$ACCESS_TOKEN" == "null" ] || [ -z "$ACCESS_TOKEN" ]; then
    echo "Failed to get access token"
    echo "Response: $TOKEN_RESPONSE"
    exit 1
fi

echo "✅ Got access token: ${ACCESS_TOKEN:0:50}..."

echo "=== Calling sync endpoint WITHOUT authentication (public endpoint) ==="

# Call the sync endpoint without Authorization header since it's now public
SYNC_RESPONSE=$(curl -v -X POST "http://localhost:8081/users/sync-keycloak" \
  -H "Content-Type: application/json" \
  -w "HTTP_CODE: %{http_code}")

echo "✅ Sync completed"
echo "Full response: $SYNC_RESPONSE"
