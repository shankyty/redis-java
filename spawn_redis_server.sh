#!/bin/sh
set -e
tmpFile=$(mktemp -d)
javac -sourcepath src/main/java src/main/java/com/github/shankyty/redis/Main.java -d "$tmpFile"
jar cf java_redis.jar -C "$tmpFile"/ .
exec java -cp java_redis.jar com.github.shankyty.redis.Main "$@"
