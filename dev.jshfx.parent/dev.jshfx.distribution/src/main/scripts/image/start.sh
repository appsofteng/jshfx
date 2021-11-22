#!/bin/sh

cd `dirname $0`
exec runtime/bin/java @options > /dev/null 2>&1
