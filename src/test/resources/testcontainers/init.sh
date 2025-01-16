#!/bin/bash

# Configura la regione e l'endpoint AWS
AWS_REGION="eu-south-1"
LOCALSTACK_ENDPOINT="http://localhost:4566"

# Creazione del parametro SSM
aws ssm put-parameter \
  --region "$AWS_REGION" \
  --endpoint-url "$LOCALSTACK_ENDPOINT" \
  --name "pn-PDFRaster" \
  --type "String" \
  --value '{"cropbox":"0,0,595,841","dpi":160,"margins":"0,0,595,841","mediaSize":"A4","transformationsList":"scale","maxFileSize":10000000,"convertToGrayscale":false}' \

echo "Initialization terminated"