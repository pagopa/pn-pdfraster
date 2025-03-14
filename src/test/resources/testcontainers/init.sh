#!/bin/bash

## CONFIGURATION ##
AWS_REGION="eu-south-1"
LOCALSTACK_ENDPOINT="http://localhost:4566"

## LOGGING FUNCTIONS ##
log() { echo "[pn-ss-init][$(date +'%Y-%m-%d %H:%M:%S')] $*"; }

silent() {
  if [ "$VERBOSE" = false ]; then
    "$@" > /dev/null 2>&1
  else
    "$@"
  fi
}

## FUNCTIONS ##
create_ssm_parameter() {
  local parameter_name=$1
  local parameter_value=$2
  echo "Creating parameter: $parameter_name"
  echo "Parameter value: $parameter_value"

  silent aws ssm get-parameter \
    --region "$AWS_REGION" \
    --endpoint-url "$LOCALSTACK_ENDPOINT" \
    --name "$parameter_name" && \
    log "Parameter already exists: $parameter_name" && \
    return 0

  aws ssm put-parameter \
    --region "$AWS_REGION" \
    --endpoint-url "$LOCALSTACK_ENDPOINT" \
    --name "$parameter_name" \
    --type String \
    --value "$parameter_value" && \
    log "Parameter created: $parameter_name" || \
  { log "Failed to create parameter: $parameter_name"; return 1; }
}

create_queue(){
  local queue=$1
  log "Creating queue: $queue"

  if silent aws sqs get-queue-url --queue-name "$queue" \
                                  --region "$AWS_REGION"  \
                                  --endpoint-url "$LOCALSTACK_ENDPOINT"; then
    log "Queue already exists: $queue"
    return 0
  fi

  silent aws sqs create-queue --queue-name "$queue" \
                       --region "$AWS_REGION"  \
                       --endpoint-url "$LOCALSTACK_ENDPOINT" && \
  log "Created queue: $queue" || \
  { log "Failed to create queue: $queue"; return 1; }
}

create_ssm_parameter "pn-PDFRaster" '{"cropbox": "0,0,595,841","dpi": 150,"mediaSize": "A4","margins": "0,0,595,841","transformationsList": "portrait;scale","maxFileSize": 100000000,"convertToGrayscale": true}' && \
create_queue "pn-ss-transformation-raster-queue" && \
echo "Initialization terminated"