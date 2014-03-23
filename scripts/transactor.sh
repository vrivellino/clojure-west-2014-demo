#!/bin/sh

if [ -z "$1" -o -z "$2" ]; then
	echo "Usage: `basename $0` path/to/datomic-pro path/to/datomic-license-key" 2>&1
	exit 1
fi

if [ ! -f "$2" ]; then
	echo "$2 is not a file" >&2
	exit 1
fi

set -e

scriptdir=`dirname $0`

[ -f "$1/dev-transactor.properties" ] || \
	echo "Creating dev-transactor.propertie ..."
	sed "s,^license-key=,license-key=`$scriptdir/_flatten-file.sh $2`," "$1/config/samples/dev-transactor-template.properties" > "$1/dev-transactor.properties"

cd "$1"
echo "Executing: $1/bin/transactor dev-transactor.properties"
./bin/transactor dev-transactor.properties
