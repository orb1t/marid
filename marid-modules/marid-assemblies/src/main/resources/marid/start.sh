#!/bin/sh

cd "$(dirname $0)"

JAVA_CMD="$JAVA_HOME/bin/java"

$JAVA_CMD -jar ${artifactId}.${project.packaging} $@ Init > "logs/${project.artifactId}.log" 2>&1 &