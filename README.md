# s3testj
Java tools to measure S3 latency, transfer rate, throughput, etc.

## Installation
A shaded jar containing the tools can be simply built from source with maven and Java 8.  (They probably
will build on Java 7, but the POM specifies JDK 1.8.)
```
git clone https://github.com/talkingscott/s3testj.git
mvn package
```

## Configuration
These tools use the AWS Java SDK, so credentials are configured as usual, e.g. IAM on an EC2 instance,
~/.aws/credentials on a laptop.  The tools are hard-coded to use the us-east-1 region.  Edit the source
to change that.

## Running
I run from the uber-jar.
```
java -cp target/s3testj-0.0.1-SNAPSHOT.jar com.scottnichol.s3testj.GetMany your-bucket-name
java -cp target/s3testj-0.0.1-SNAPSHOT.jar com.scottnichol.s3testj.GetManyMT thread-count your-bucket-name
java -cp target/s3testj-0.0.1-SNAPSHOT.jar com.scottnichol.s3testj.PeriodicGet your-bucket-name
```

## Gotchas
The big one right now is that these assume objects with a specific set of keys already exist in your
bucket.  They do exist if you have run the node.js tools at http://github.com/talkingscott/s3test.
Hopefully, I will add a PutMany and/or PutManyMT tool to this repo soon.
