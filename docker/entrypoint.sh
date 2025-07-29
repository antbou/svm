#!/bin/bash
set -e

SPRING_PROFILE=${SPRING_PROFILES_ACTIVE:-local}

echo "Launching Spring Boot with profile: $SPRING_PROFILE"

if [ "$SPRING_PROFILE" = "local" ]; then
  exec mvn spring-boot:run \
    -Dspring-boot.run.profiles=$SPRING_PROFILE \
    -Dspring.devtools.restart.enabled=true \
    -Dspring.devtools.livereload.enabled=true
else
  exec mvn spring-boot:run \
    -Dspring-boot.run.profiles=$SPRING_PROFILE
fi
