#!/bin/sh

cd "$(dirname $0)"

JAVA_CMD="$java"

$JAVA_CMD -jar lib/${project.build.finalName}.jar > "logs/${project.artifactId}.log" 2>&1 &