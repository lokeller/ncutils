#!/bin/bash

echo "Version?"
read ver

cp README.txt dist
cp -r src/examples dist
rm dist/README.TXT

zip -r ncutils-java-$ver.zip dist
