clojure-west-2014-demo
======================

Simple Web Service that saves messages to Datomic.

_Note: the web app is currently a work-in-progress._ Right now, it runs locally
but fails to initialize under Tomcat when deployed via a war file. Hope to have
it fixed up in a few days.

## Overview

This is a demo web application that saves data sent to the /write endpoint to
Datomic, and reads from Datomic for queries to the /read endpoint.

```
$ curl 'http://localhost:3001/write?id=1&msg=HelloWorld'
1006
$ curl 'http://localhost:3001/read?id=1'
HelloWorld
```

This project provides a reference implementation of deploying Datomic in AWS as
a follow-up to my presentation at Clojure/West 2014:
[DevOps Done Right: Room Key's Datomic Deployment in AWS](https://www.youtube.com/watch?v=vFX6T5oQC7Y)

### CloudFormation Template

The provided CloudFormation template (`config/cfn-template.json`) builds a stack,
peers and transactors, without using the tools included in the Datomic
distribution. The web application included is very basic, but the template
includes examples of many advanced CloudFormation features, including
Conditions, Auto Scaling Update Policy, and CloudFormation MetaData. 

The template should provide a refernce for those who may want to run a custom
Datomic deployment in Amazon Web Services.

### Prerequisites

* [AWS Account](http://aws.amazon.com) and Access Keys
* [Datomic Pro Starter](http://www.datomic.com/get-datomic.html) license and credentials
* [leiningen](https://github.com/technomancy/leiningen)
* [AWS CLI tools](http://aws.amazon.com/cli/)
* Java7 JDK Linux RPM downloaded from Oracle ([jdk-7u51-linux-x64.rpm](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html))

## Getting Started

### Setup Environment

First setup environment variables in your shell profile. Tools in the scripts
directory will look for MY_DATOMIC_USERNAME and MY_DATOMIC_USERNAME, as will
leiningen when it tries to download the Datomic peer library from Cognitect's
maven repo (see project.clj). These are the credentials assigned to your
Datomic account after registering for a license key.

AWS_ACCESS_KEY_ID and AWS_SECRET_KEY aren't explicitly used by this project,
but other AWS tools and libraries look for them, so they're useful to have set.

For example, add the following to your ~/.bashrc

```sh
export MY_DATOMIC_USERNAME=username@demo.domain
export MY_DATOMIC_PASSWORD=XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX

export AWS_ACCESS_KEY_ID=AKI...
export AWS_SECRET_KEY=<SECRET>
```

### Configure AWS CLI Tools

The tools in the scripts directory rely on the [AWS CLI tools](http://aws.amazon.com/cli/)
being installed and configured.

Installation requires Python, and can typically be done via pip:

```
pip install awscli
```

Configuration is just as simple. For example:

```
$ aws configure
AWS Access Key ID [None]: AKI...
AWS Secret Access Key [None]: ...
Default region name [None]: us-east-1
Default output format [None]: table
```

This tells the tools to use us-east-1 by default, and the preferred output type
is table format.

You can use the describe-regions command to verify it has been successfully
configured:

```
$ aws ec2 describe-regions
----------------------------------------------------------
|                     DescribeRegions                    |
+--------------------------------------------------------+
||                        Regions                       ||
|+-----------------------------------+------------------+|
||             Endpoint              |   RegionName     ||
|+-----------------------------------+------------------+|
||  ec2.eu-west-1.amazonaws.com      |  eu-west-1       ||
||  ec2.sa-east-1.amazonaws.com      |  sa-east-1       ||
||  ec2.us-east-1.amazonaws.com      |  us-east-1       ||
||  ec2.ap-northeast-1.amazonaws.com |  ap-northeast-1  ||
||  ec2.us-west-2.amazonaws.com      |  us-west-2       ||
||  ec2.us-west-1.amazonaws.com      |  us-west-1       ||
||  ec2.ap-southeast-1.amazonaws.com |  ap-southeast-1  ||
||  ec2.ap-southeast-2.amazonaws.com |  ap-southeast-2  ||
|+-----------------------------------+------------------+|
```

### Download Java7 JDK from Oracle

Cognitect recommends that production Datomic transactors run using the JRE from
Oracle. In fact, the official transactor AMI they build includes the Oracle JDK.

Go to [Oracle.com](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)
and download the Linux 64-bit Java7 JDK RPM (jdk-7u51-linux-x64.rpm). 

Keep this handy, the script that creates resources in AWS will upload it to S3.

### Save your Datomic License Key

After your register for Datomic Pro Starter, save the license key they send you
to a file. Don't include _license-key=_ when you save it - just save the key.

## Running Locally

Before you can run the service locally, you need to download Datomic and start
up a transactor.

### Download Datomic and Run Transactor

Assuming you setup your environment correctly, you should be able to run
scripts/download-datomic-pro.sh.

For example:

```sh
mkdir ~/download
cd ~/download
../clojure-west-2014-demo/scripts/download-datomic-pro.sh
```

Unzip the downloaded Datomic Pro zip file in the filesystem location of your
choice.

Included is a helper script that will populate a properties file for a dev
transactor and start it up.

It takes the path to the Datomic Pro distribution, as well as the path to your
license key file, as arguments.

For example:

```sh
./scripts/transactor.sh ~/datomic-pro-0.9.4609 ~/download/datomic-license-key
```

The output should look something like this:

```sh
Creating dev-transactor.propertie ...
Executing: /home/var/datomic-pro-0.9.4609/bin/transactor dev-transactor.properties
Launching with Java options -server -Xms1g -Xmx1g -XX:NewRatio=4 -XX:SurvivorRatio=8 -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:+CMSParallelRemarkEnabled -XX:CMSInitiatingOccupancyFraction=60 -XX:+UseCMSInitiatingOccupancyOnly -XX:+CMSScavengeBeforeRemark
Starting datomic:dev://localhost:4334/<DB-NAME>, storing data in: data ...
System started datomic:dev://localhost:4334/<DB-NAME>, storing data in: data
```

### Start a local instance of the app

Once the transactor is online, you can run lein ring server

```sh
lein ring server-headless
```

For write & query examples, see [Overview](#overview).


## Deploying to AWS

Deploying to Amazon requires you to create some AWS resources - DynamoDB tables,
S3 buckets, and an EC2 Security Group. This is handled by scripts/mk-aws-resources.sh.

**NOTE: Creating DynamoDB tables _cost you money_.** If you don't delete the tables
created by scripts/mk-aws-resources.sh, they will cost you roughly $15/mo.

The mk-aws-resources.sh saves an environment file in the scripts directory named
.demo.env. The other scripts read in details about your environment from it.

To remove the resources mk-aws-resources.sh created, make sure there are no
CloudFormation stacks running, then run `scripts/rm-aws-resources.sh`.

### Creating Prerequisite AWS Resources

Run scripts/mk-aws-resources.sh, supplying it with the path to the JDK RPM, as
well as the path to the license key file you created.

```sh
./scripts/mk-aws-resources.sh ~/download/jdk-7u51-linux-x64.rpm ~/download/datomic-license-key
```

It will prompt for S3 bucket names (it generates defaults), as well as an EC2
key-pair to associate with instances launched in the CloudFormation stack.
(You may choose not to have a SSH key, but you will not be able to access
those instances.)

If successful, your AWS account will now have two DynamoDB tables for this
app: one for 'staging' and one for 'production'. You will also have two EC2
Security Groups for yet-to-be-created Datomic peers, and two S3 buckets.

mk-aws-resources.sh uploads your license-key and JDK RPM to the first S3 bucket,
and this S3 bucket will be used for war file uploads.

The second S3 bucket is where transactors will save their logs.

Again, all of these resources can be deleted via scripts/rm-aws-resources.sh.

### Deploying a Staging Stack

After the required AWS resources are created, deploying a CloudFormation stack
that runs this demo is as simple as building a war file with lein and running
scripts/create-stack.sh:

```sh
lein ring uberwar
```

The output of lein ring uberwar will look something like

```sh
$ lein ring uberwar
Created /home/var/clojure-west-2014-demo/target/clj-west-0.0.1-SNAPSHOT-standalone.war
```

Pass in the path to the war file the lein ring uberwar generated, as well as a
stack name:

```sh
./scripts/create-stack.sh \
    /home/var/clojure-west-2014-demo/target/clj-west-0.0.1-SNAPSHOT-standalone.war \
    datomic-demo-staging
```

**NOTE: The EC2 instance-types defined in the CloudFormation template are
_not_ in the free tier, so creating this stack _will cost you money_.**

The war file you specified will be uploaded to S3 by create-stack.sh, and then
passed in as a parameter to the stack it creates.

Watch your stack come online by logging into the AWS Console and selecting
CloudFormation under Services. Once the stack's status is CREATE_COMPLETE,
lookup the TransLaunchGroup output value in the Outputs tab.

Then scale-up the transactor Auto Scaling Group to one:

```sh
aws autoscaling set-desired-capacity --desired-capacity 1 \
    --no-honor-cooldown \
    --auto-scaling-group-name datomic-demo-staging-TransactorLaunchGroup-XXXXXXXXXXXXX
```

If you want high-availability, just specify `--desired-capacity 2`.

Also listed in the stack outputs is the hostname of the peer Elastic Load
Balancer.

We can use this hostname to write data to Datomic, and to query it:

TODO: Examples with curl hitting /query & /read against ELBDNS

If you'd like to log in to the instances, look up their hostname in the EC2
console.

### Deploying an Update

If we make an update to the source code or we make update to the CloudFormation
template, and we need to deploy a new version, this can be accomplished two
ways.

After we build a new war file, we can either run scripts/update-stack.sh,
or we can create a new stack using create-stack.sh as described above.

If you choose to update the existing stack, update-stack.sh takes the same
arguments as create-stack.sh.

For example:

```sh
./scripts/update-stack.sh \
    /home/var/clojure-west-2014-demo/target/clj-west-0.0.2-SNAPSHOT-standalone.war \
    datomic-demo-staging
```

The CloudFormation template has Update Policies defined for the transactor and
peer Auto Scaling groups, so CloudFormation will take care of cycling instances.
You can watch the actions CloudFormation is taking by viewing the stack's
Events tab in the CloudFormation console.

If you choose to create a new stack, you'll need to scale-up the transactor
auto scaling group in the new stack while scaling down the transactor group in
the old stack.

Both methods were described in my presentation, and I leave it as an exercise to
the user to work through them.

## Cleaning up

As mentioned above, creating DynamoDB tables using mk-aws-resources.sh will
incur charges on your AWS account, as will running a CloudFormation stack using
the included template.

To delete resources from your AWS account, first delete any CloudFormation
stack you created:

```sh
./scripts/delete-stack.sh datomic-demo-staging
```

After the stack is completely deleted, run rm-aws-resources.sh:

```sh
./scripts/rm-aws-resources.sh
```

These tools _should_ remove AWS resources they created. To be safe, you should
poke around your AWS console to make sure.
