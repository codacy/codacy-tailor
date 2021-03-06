#!/usr/bin/env bash

set -e

VERSION="1.0.0-$(git symbolic-ref --short HEAD)-SNAPSHOT"

if [ -n "$1" ]; then
  VERSION="$1"
fi

echo "Building docker with version ${VERSION}"
sbt 'set version in Docker := "'"${VERSION}"'"' docker:publishLocal
