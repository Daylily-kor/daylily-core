# daylily-core

Spring boot server for Daylily project

## Prerequisites

```dotenv
DAYLILY_DB_USER=<username>
DAYLILY_DB_PASSWORD=<password>
```

On the project root, create a `.env` file with the above variables.

## Installation

```shell
$ docker compose -f docker-compose-prod.yaml up -d
```

Then run the command above to start Daylily. Docker compose will start Daylily Spring Boot server and PostgreSQL database.

## Development

> Make sure you have created `.env` file from **Prerequisties** section.

```shell
$ ./gradlew bootBuildImage --imageName=docker.io/<docker-username>/<image-name>
```

You can build the image with the command above. This will not push the image to Docker Hub by default. 
Use `--publishImage` option to push the image after image generation.

```shell
$ docker compose -f docker-compose-dev.yaml up -d
```

Then use Docker compose to start the server and PostgreSQL database in development mode.