#!/usr/bin/env sh
export GDK_SCALE=2
export MAVEN_OPTS=-Xmx8g
cd ..
mvn exec:java -Dexec.mainClass="bo.tc.tcplanner.TCCore" -Djava.rmi.server.hostname=192.168.10.49
 
