#!/usr/bin/env sh
cd ..
mvn exec:java -Dexec.mainClass="bo.tc.tcplanner.TCCore" -Djava.rmi.server.hostname=192.168.10.49
 
