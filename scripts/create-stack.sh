#!/bin/sh

if [ -z "$1" -o -z "$2" ]; then
	echo "Usage: `basename $0` <path/to/uber.war> <stack-name>" >&2
	exit 1
fi

set -e
. `dirname $0`/.demo.env

warfile=uberwars/`basename $1`
aws s3 cp "$1" "s3://$SRCBUCKET/$warfile"

parameters="--parameters ParameterKey=SrcBucket,ParameterValue=$SRCBUCKET"
parameters="$parameters ParameterKey=WarFile,ParameterValue=$warfile"
parameters="$parameters ParameterKey=TransactorLogBucket,ParameterValue=$LOGBUCKET"
parameters="$parameters ParameterKey=SshKey,ParameterValue=$SSHKEY"
parameters="$parameters ParameterKey=MyDatomicUserName,ParameterValue=$MY_DATOMIC_USERNAME"
parameters="$parameters ParameterKey=MyDatomicPassword,ParameterValue=$MY_DATOMIC_PASSWORD"

set -x
aws cloudformation create-stack \
	--stack-name "$2" \
	--template-body file://`dirname $0`/../config/cfn-template.json \
	--capabilities CAPABILITY_IAM \
	$parameters
