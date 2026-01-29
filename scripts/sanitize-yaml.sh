#!/bin/bash

# sanitize-yaml.sh

echo "Starting YAML sanitization..."

if [ $# -ne 1 ]; then
    echo "invalid number of arguments."
    echo "Uso: bash $0 <input_file>"
    exit 1
fi

INPUT_FILE="$1"

sed -i '
    s|&|\&amp;|     # 1. & → &amp;
    s|<|\&lt;|      # 2. < → &lt;
    s|>|\&gt;|      # 3. > → &gt;
    ' "$INPUT_FILE"
unix2dos "$INPUT_FILE"

echo "File sanitized: $INPUT_FILE"
