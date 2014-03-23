#!/bin/sh

set -e

if [ -f `dirname $0`/.demo.env ]; then
	echo "!!! ERROR: `dirname $0`/.demo.env exists, which means you already created AWS resources"
	echo "    Aborting"
	exit 1
fi

if [ -z "$MY_DATOMIC_USERNAME" -o -z "$MY_DATOMIC_PASSWORD" ]; then
	echo "!!! ERROR: MY_DATOMIC_USERNAME and/or MY_DATOMIC_PASSWORD not found in environment"
	echo "    Please set them in your shell and export them."
	exit 1
fi

if [ -z "$1" -o -z "$2" ]; then
	echo "Usage: `basename $0` <path/to/jdk-7u51-linux-x64.rpm> <path/to/datomic-license-key>" >&2
	exit 1
fi

echo "!!! WARNING: THIS WILL CREATE DynamoDB TABLES WITH PROVISIONED BANDWIDTH !!!"
echo "    This will cost you money"
echo

ts=`date +%s`
SRCBUCKET=vriv-demo-src-$ts
LOGBUCKET=vriv-demo-log-$ts
SSHKEY=none

echo -n "Enter name of a S3 bucket for your war files [$SRCBUCKET]: "
read src_input
echo -n "Enter name of a S3 bucket for transactor logs [$LOGBUCKET]: "
read log_input

aws ec2 describe-key-pairs --output table
echo -n "Enter the name of ssh-key you wish to use to access EC2 Instances [$SSHKEY]: "
read key_input

[ -z "$src_input" ] || SRCBUCKET=$src_input
[ -z "$log_input" ] || LOGBUCKET=$log_input
[ -z "$key_input" ] || SSHKEY=$key_input

cat > `dirname $0`/.demo.env << _END_
SRCBUCKET=$SRCBUCKET
LOGBUCKET=$LOGBUCKET
MY_DATOMIC_USERNAME=$MY_DATOMIC_USERNAME
MY_DATOMIC_PASSWORD=$MY_DATOMIC_PASSWORD
SSHKEY=$SSHKEY
_END_

read_prov=50
write_prov=10

set -x
aws s3 mb s3://$SRCBUCKET
aws s3 mb s3://$LOGBUCKET

#for env in staging loadtest qa production; do
for env in staging production; do
	aws ec2 create-security-group --group-name "datomic-demo-peer-$env" --description "Datomic Demo - peer $env" || true
	aws dynamodb create-table --table-name datomic_demo_$env \
		--attribute-definitions "AttributeName=id,AttributeType=S" \
		--key-schema "AttributeName=id,KeyType=HASH" \
		--provisioned-throughput "ReadCapacityUnits=$read_prov,WriteCapacityUnits=$write_prov"
done

aws s3 cp "$1" s3://$SRCBUCKET/
aws s3 cp "$2" s3://$SRCBUCKET/datomic-license-key
