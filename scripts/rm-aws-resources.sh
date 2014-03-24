#!/bin/sh

. `dirname $0`/.demo.env || exit $?

echo "!!! WARNING: This will destroy the data in the buckets you specify, as well as DynamoDB tables !!!"
echo
echo -n "Press <ENTER> to Continue ..."
read junk

if [ -n "$SRCBUCKET" ] && aws s3 ls --output text | grep -q "\<$SRCBUCKET$" ; then
	set -x
	aws s3 rm --recursive s3://$SRCBUCKET/
	aws s3 rb s3://$SRCBUCKET
	set +x
fi
if [ -n "$LOGBUCKET" ] && aws s3 ls --output text | grep -q "\<$LOGBUCKET$" ; then
	set -x
	aws s3 rm --recursive s3://$LOGBUCKET/
	aws s3 rb s3://$LOGBUCKET
	set -x
fi

#for env in staging loadtest qa production; do
for env in staging production; do
	set -x
	aws dynamodb delete-table --table-name datomic_demo_$env
	aws ec2 delete-security-group --group-name "datomic-demo-peer-$env"
	set +x
done

rm -f `dirname $0`/.demo.env
