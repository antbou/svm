DOCKER_DIR=docker
SERVICE_NAME=spring-playwright-app

.PHONY: build up down logs clean shell

build:
	docker compose -f $(DOCKER_DIR)/docker-compose.yml build

up:
	docker compose -f $(DOCKER_DIR)/docker-compose.yml up

down:
	docker compose -f $(DOCKER_DIR)/docker-compose.yml down

logs:
	docker compose -f $(DOCKER_DIR)/docker-compose.yml logs -f

clean:
	docker compose -f $(DOCKER_DIR)/docker-compose.yml down -v --remove-orphans
	docker system prune -f

shell:
	docker exec -it $(SERVICE_NAME) bash
