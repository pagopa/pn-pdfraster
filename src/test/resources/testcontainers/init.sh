#!/bin/bash

# Aggiungi i parametri a AWS SSM
aws ssm put-parameter \
  --name "pn-PDFRaster" \
  --type "String" \
  --value '{"cropbox":"0,0,595,841","dpi":150,"margins":"0,0,595,841","mediaSize":"A4","transformationsList":"scale","maxFileSize":10000000,"convertToGrayscale":false}' \
  --endpoint-url http://localstack:4566

echo "Initialization terminated"