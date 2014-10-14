#!/bin/sh

if [ -z "$1" -o -z "$2" ]; then
	echo "Usage: `basename $0` <path/to/uber.war> <stack-name>" >&2
	exit 1
fi

. `dirname $0`/.demo.env

# BSD stat has other flags
stat="stat -c %Y"
stat -c %Y $file > /dev/null 2>&1
if [ $? -ne 0 ]; then
        stat="stat -f %m"
fi

set -e

warfile=uberwars/`basename $1 .war`-$($stat $1).war
# Uploaded war file will have modifcation ts embedded in its name
aws s3 cp "$1" "s3://$SRCBUCKET/$warfile"

parameters="--parameters ParameterKey=SrcBucket,ParameterValue=$SRCBUCKET"
parameters="$parameters ParameterKey=WarFile,ParameterValue=$warfile"
parameters="$parameters ParameterKey=TransactorLogBucket,ParameterValue=$LOGBUCKET"
parameters="$parameters ParameterKey=SshKey,ParameterValue=$SSHKEY"
parameters="$parameters ParameterKey=MyDatomicUserName,ParameterValue=$MY_DATOMIC_USERNAME"
parameters="$parameters ParameterKey=MyDatomicPassword,ParameterValue=$MY_DATOMIC_PASSWORD"

set -x
aws cloudformation update-stack \
	--stack-name "$2" \
	--template-body file://`dirname $0`/../config/cfn-template.json \
	--capabilities CAPABILITY_IAM \
	$parameters
