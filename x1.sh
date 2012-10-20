#!/bin/bash

CLASSPATH=.:./lib/janino.jar:./lib/commons-compiler.jar:./build

javac -d build src/java/XPathReader.java src/java/XPathExample.java

java XPathReader data/sample.xml
