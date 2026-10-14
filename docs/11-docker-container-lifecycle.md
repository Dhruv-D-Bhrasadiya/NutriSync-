# Task 11 — Docker Image and Container Lifecycle

## Container design
Use a multi-stage Docker build: build with Maven/JDK, copy the runnable JAR into a compatible JRE runtime image. Pin a supported base image, run as non-root, configure through environment variables and do not bake credentials into the image. Spring Boot must bind to 0.0.0.0 inside the container.

Keep PostgreSQL data in a named volume or managed database; deleting the app container must not delete the only DB copy. Docker Compose is useful for a local app/database stack.

## Build and inspect
After Dockerfile and Maven artifact exist:
~~~bash
docker build -t nutrisync:0.1.0 .
docker image ls nutrisync
docker image inspect nutrisync:0.1.0
docker history nutrisync:0.1.0
~~~

Prefer release or commit-SHA tags over relying on mutable latest.

## Run with config
Example only, after a DB service/network and ignored local environment file exist:
~~~bash
docker network create nutrisync-net
docker run -d --name nutrisync-app --network nutrisync-net \
  -p 127.0.0.1:8080:8080 \
  --env-file .env.local nutrisync:0.1.0
~~~

The command is not standalone until datasource settings and DB service are configured. Keep .env.local out of Git. Avoid exposing PostgreSQL broadly to the host network.

## Lifecycle commands
~~~bash
docker ps
docker ps -a
docker logs --tail 100 nutrisync-app
docker inspect nutrisync-app
docker port nutrisync-app
docker restart nutrisync-app
docker stop nutrisync-app
docker start nutrisync-app
docker rm -f nutrisync-app
docker image ls nutrisync
~~~

Only remove lab containers; never remove production containers/volumes in the exercise.

## Validation
- Container starts and health endpoint responds.
- Logs contain no credentials.
- Port binding is intentional; use loopback for local demo.
- PostgreSQL records survive app-container restart.
- Runtime user is non-root.
- Image tag/ID and command outputs are captured.

## Troubleshooting
- Container exits: inspect logs, datasource and startup settings.
- DB unreachable: use service DNS, not localhost from inside app container.
- Port occupied: choose another host port and update Nginx/base URL.
- Permissions: check runtime user and mount ownership rather than running everything as root.
- Missing JAR: inspect Maven output, build context and .dockerignore.
