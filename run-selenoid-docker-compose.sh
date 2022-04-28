#!/usr/bin/env bash

command -v docker >/dev/null 2>&1 || { echo >&2 "I require docker but it's not installed.  Aborting."; exit 1; }
command -v docker-compose >/dev/null 2>&1 || { echo >&2 "I require docker-compose but it's not installed.  Aborting."; exit 1; }
RUN_DIR=$(dirname $(readlink -f "$0"))
docker pull selenoid/chrome:latest
docker pull selenoid/firefox:latest
docker pull selenoid/video-recorder:7.1
docker-compose -f "$RUN_DIR/selenoid-docker-compose.yml" up -d
docker ps -n2

# TO STOP
# docker-compose -f "$CURRENT_DIR/selenoid-docker-compose.yml" down