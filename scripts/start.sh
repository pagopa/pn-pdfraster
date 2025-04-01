#!/bin/bash

curr_dir=$(pwd)
echo "### Starting pn-pdfraster ###"
cd ..

if ! ./mvnw -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=local -Daws.accessKeyId=TEST -Daws.secretAccessKey=TEST -Daws.region=eu-south-1" spring-boot:run; then
  echo "### Initialization failed ###"
  exit 1
fi

# Return to the original directory
cd "$curr_dir"
