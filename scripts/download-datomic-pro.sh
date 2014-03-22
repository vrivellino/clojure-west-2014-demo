#!/bin/sh

# Download Datomic-Pro locally, if you need it
VERSION=0.9.4609
wget --http-user=$MY_DATOMIC_USERNAME \
     --http-password=$MY_DATOMIC_PASSWORD \
	 https://my.datomic.com/repo/com/datomic/datomic-pro/$VERSION/datomic-pro-$VERSION.zip
