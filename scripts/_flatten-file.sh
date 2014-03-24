#!/bin/sh

# Helper script to flatten license file into one line.
while read line ; do
	echo -n $line
done < "$1"
echo
