#!/usr/bin/env sh
export GDK_SCALE=2
export MAVEN_OPTS=-Xmx8g
cd ..
mvn exec:java -Dexec.mainClass="bo.tc.tcplanner.TCRemote" -Dexec.args="--address=192.168.10.49 --port=1091"
 
