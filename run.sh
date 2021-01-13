#!/bin/bash

print_this_dir() {
  local real_path="$(readlink --canonicalize "$0")"
  (
    cd "$(dirname "$real_path")"
    pwd
  )
}

export APP_DIR="$(print_this_dir)"

java -jar \
  "${APP_DIR}/target/sample-a-0.0.1-SNAPSHOT-jar-with-dependencies.jar" \
  "$@"
