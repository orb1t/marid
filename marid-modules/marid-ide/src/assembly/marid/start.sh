#!/bin/sh

cd "$(dirname $0)"

java -jar lib/${project.build.finalName}.jar > "logs/ide.log" 2>&1 &