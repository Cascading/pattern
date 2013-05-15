Pattern Examples
================

To build and run:

    gradle clean jar
    rm -rf out
    hadoop jar build/libs/pattern-examples-1.0.0-wip-dev.jar data/sample.tsv out/classify --pmml data/sample.rf.xml 
