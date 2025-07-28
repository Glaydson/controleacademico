#!/bin/bash

echo "Waiting for Keycloak to be ready..."

# Função para verificar se o Keycloak está pronto
wait_for_keycloak() {
  until curl -f -s http://keycloak:8080/realms/controle-academico > /dev/null 2>&1; do
    echo "Keycloak is not ready yet, waiting 5 seconds..."
    sleep 5
  done
  echo "Keycloak is ready!"
}

# Aguarda o Keycloak estar pronto
wait_for_keycloak

# Inicia a aplicação Quarkus usando o script run-java.sh
echo "Starting backend application..."
exec /opt/jboss/container/java/run/run-java.sh
