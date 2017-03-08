#!/bin/bash
if [[ -z "${DOCKER_HOST_IP-}" ]]; then
  docker_host_ip=$(docker run --rm --net host alpine ip address show eth0 | awk '$1=="inet" {print $2}' | cut -f1 -d'/')
  # Work around Docker for Mac 1.12.0-rc2-beta16 (build: 9493)
  if [[ $docker_host_ip = '192.168.65.2' ]]; then
    docker_host_ip=$(/sbin/ifconfig | grep -v '127.0.0.1' | awk '$1=="inet" {print $2}' | cut -f1 -d'/' | head -n 1)
  fi
  export DOCKER_HOST_IP=$docker_host_ip
fi

docker docker-compose -f docker/docker-compose-local.yml up -d && \
  docker-compose -f docker/docker-compose-local.yml scale kafka=3
