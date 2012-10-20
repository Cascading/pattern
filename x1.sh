#!/bin/bash

CLASSPATH=.:./lib/janino.jar:./lib/commons-compiler.jar:./lib/jgrapht-jdk1.6.jar:./build

javac -d build src/java/XPathReader.java

java XPathReader data/sample.xml
