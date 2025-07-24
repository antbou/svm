#!/bin/bash
set -e

# Launch Spring Boot app in development mode
exec mvn spring-boot:run \
  -Dspring.devtools.restart.enabled=true \
  -Dspring.devtools.livereload.enabled=true