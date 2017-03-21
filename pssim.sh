#!/usr/bin/env bash

JAVA_HOME=~/software/jdk1.7.0_79
MIN_HEAP=1g
MAX_HEAP=10g
GC_OPTS="+UseConcMarkSweepGC"
PSSIM_JAR_PATH=~/workspace/pssim/target/pssim-0.1-jar-with-dependencies.jar

if [ -z "$JAVA_HOME" ]; then
    JAVA_CMD="java"
else
    JAVA_CMD=$JAVA_HOME/bin/java
fi

$JAVA_CMD -Xms$MIN_HEAP -Xmx$MAX_HEAP -XX:$GC_OPTS -jar $PSSIM_JAR_PATH de.tum.msrg.pubsub.PSSim $@

echo finished $@
