#!/bin/bash

print_this_dir() {
  local real_path="$(readlink --canonicalize "$0")"
  (
    cd "$(dirname "$real_path")"
    pwd
  )
}

setup(){
  echo "Install Maven Wrapper"
  mvn -N io.takari:maven:0.7.7:wrapper -Dmaven=3.6.3
}

package(){
  (
    cd ${APP_DIR}
    # $MVN_CMD clean
    $MVN_CMD package -Dmaven.test.skip=true
    local st=$?
    if [ "$st" -ne 0 ]; then
      return $st
    fi

    echo "----"
    ls -l target/*.jar
    echo "----"
    ls -lh target/*.jar
  )
}

_test(){
  $MVN_CMD test
}

APP_DIR="$(print_this_dir)"
MVN_CMD=${APP_DIR}/mvnw

cmd="$1"; shift

case $cmd in
  setup) #task
    setup
    ;;
  run) #task
    run "$@"
    ;;
  package) #task
    package
    ;;
  test) #task
    _test
    ;;
  tasks) #task
    grep '#task' $0 | grep -v "grep '#task'"
    ;;
  *)
    echo "invalid command"
    ;;
esac
