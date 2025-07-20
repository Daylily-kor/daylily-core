# daylily-core

Spring boot server for Daylily project

## Installation

```shell
$ docker compose -f docker-compose-prod.yaml up -d
```

Docker compose will start Daylily Spring Boot server and PostgreSQL database.

## Development

```shell
$ ./gradlew bootBuildImage --imageName=docker.io/<docker-username>/<image-name>
```

You can build the image with the command above. This will not push the image to Docker Hub by default. 
Use `--publishImage` option to push the image after image generation.

```shell
$ docker compose -f docker-compose-dev.yaml up -d
```

Then use Docker compose to start the server and PostgreSQL database in development mode.