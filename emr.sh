#!/bin/bash -ex

# create your Amazon AWS account and be sure to sign up for: EMR, S3, SimpleDB
# have your credentials set up locally, such as at ~./aws_cred

# install the Amazon AWS Elastic MapReduce CLI tool:
# http://aws.amazon.com/developertools/2264

# install the `s3cmd` tool for S3:
# http://s3tools.org/s3cmd

# then edit the `BUCKET` variable to use one of your S3 buckets:
BUCKET=temp.cascading.org/pattern
SINK=out
PMML=sample.rf.xml
DATA=sample.tsv

# clear previous output (required by Apache Hadoop)
s3cmd del -r s3://$BUCKET/$SINK

# load built JAR + input data
s3cmd put build/libs/pattern.jar s3://$BUCKET/
s3cmd put data/$PMML s3://$BUCKET/
s3cmd put data/$DATA s3://$BUCKET/

# launch cluster and run
elastic-mapreduce --create --name "RF" \
  --debug --enable-debugging \
  --log-uri s3n://$BUCKET/logs \
  --jar s3n://$BUCKET/pattern.jar \
  --arg s3n://$BUCKET/$DATA \
  --arg s3n://$BUCKET/$SINK/classify \
  --arg s3n://$BUCKET/$SINK/trap \
  --arg "--pmml" \
  --arg s3n://$BUCKET/$PMML
