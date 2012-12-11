#!/bin/bash

# first, install the Amazon AWS Elastic MapReduce CLI tool
# http://aws.amazon.com/developertools/2264

elastic-mapreduce --create --name "RF" \
  --jar s3n://temp.cascading.org/pattern/pattern.jar \
  --arg s3n://temp.cascading.org/pattern/sample.rf.xml \
  --arg s3n://temp.cascading.org/pattern/sample.tsv \
  --arg s3n://temp.cascading.org/pattern/out/classify \
  --arg s3n://temp.cascading.org/pattern/out/trap
