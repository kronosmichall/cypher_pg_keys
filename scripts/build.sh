#!/usr/bin/bash
mvn clean package -DskipTests
sudo rm /var/lib/neo4j/plugins/neo4j-custom-procedure-1.0-SNAPSHOT.jar
sudo cp target/neo4j-custom-procedure-1.0-SNAPSHOT.jar /var/lib/neo4j/plugins/
sudo systemctl restart neo4j