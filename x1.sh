#!/bin/bash

CLASSPATH=.:./lib/janino.jar:./lib/commons-compiler.jar:./lib/jgrapht-jdk1.6.jar:./build
SRC=src/main/java/pattern/rf

javac -d build $SRC/XPathReader.java
javac -d build $SRC/Vertex.java
javac -d build $SRC/Edge.java
javac -d build $SRC/Tree.java
javac -d build $SRC/RandomForest.java

java pattern.rf.RandomForest data/sample.xml data/sample.tsv
