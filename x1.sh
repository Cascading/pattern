#!/bin/bash

CLASSPATH=.:./lib/janino.jar:./lib/commons-compiler.jar:./lib/jgrapht-jdk1.6.jar:./build

javac -d build src/java/XPathReader.java
javac -d build src/java/Vertex.java
javac -d build src/java/Edge.java
javac -d build src/java/Tree.java
javac -d build src/java/RandomForest.java

java RandomForest data/sample.xml
