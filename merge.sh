#!/bin/bash
hadoop fs -getmerge gs://dataproc-staging-us-west1-388522857539-gkh9vufp/output ./output.txt
hadoop fs -copyFromLocal -f ./output.txt /user/csquarehd/
hadoop fs -cp -f /user/csquarehd/output.txt gs://dataproc-staging-us-west1-388522857539-gkh9vufp/output.txt