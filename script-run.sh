#!/usr/bin/env bash

COMMAND=${1}

VERSION="truefilm:0.1"
ACTIVEDIR=`pwd`
FILES=(metadata.csv.gz wiki.xml.gz)
LINK=(https://www.dropbox.com/s/xkcgowtzd0eo9mj/metadata.csv.gz https://www.dropbox.com/s/0rsg6kag7u3oypx/wiki.xml.gz)
downloadFile(){
  for i in "${!FILES[@]}"; do
      if [[ -e "./input/${FILES[i]}" ]]; then
        echo "${FILES[i]} already exist"
      else
        echo "Downloading: ${FILES[i]}"
        wget -P ./input ${LINK[i]}
      fi
  done
}
checkImageExist(){
  echo ${VERSION}
  if [[ "$(docker images -q ${VERSION} 2> /dev/null)" == "" ]]; then
  echo "Building image"
  docker build . -t ${VERSION}
fi
}

start_postgres() {
  echo "Starting postgres"
  docker-compose up -d
  echo -e "Postgres start, visualize data on http://localhost:5050"
}

stop_postgres() {
  echo "Stopping Postgres"
  docker-compose down
  echo -e "OK"
  echo "Removing Postgres volume"
  docker volume rm truefilm_postgres
  echo -e "OK"
}

check_file_exist() {

  for input_file in "${FILES[@]}"; do
      if [[ -e "./input/${input_file}" ]]; then
        echo "Checking input file: ${input_file}... OK"
      else
        echo "Checking input file: ${input_file}... File does not exist. Exiting"
        exit 1
      fi
  done
}

start_process_with_docker() {
  check_file_exist
  checkImageExist
  docker run --network="host" -v ${ACTIVEDIR}/input:/app/input ${VERSION} sbt run
}

unit_test_with_docker() {
  checkImageExist
  docker run ${VERSION} sbt clean test
}

start_process() {
  sbt clean run
}

unit_test() {
  sbt clean test
}

case "${COMMAND}" in
startdb)
  start_postgres
  ;;
stopdb)
  stop_postgres
  ;;
download_files)
  downloadFile
  ;;
process)
  start_process
  ;;
test)
  unit_test
  ;;
process_with_docker)
  start_process_with_docker
  ;;
test_with_docker)
  unit_test_with_docker
  ;;
*)
  echo "Command not found: You can use (startdb,stopdb,download_files,process,test,process_with_docker,test_with_docker)"
  exit 1
  ;;
esac