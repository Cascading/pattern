#!/bin/bash

CLASSPATH=.:./lib/janino.jar:./lib/commons-compiler.jar:./lib/jgrapht-jdk1.6.jar:./build

javac -d build src/java/XPathReader.java
javac -d build src/java/Main.java

java Main data/sample.xml

