#!/bin/bash

function error_exit()
{
	error=$?
	if [ $error -ne 0 ]; then
		popd
		echo Exit error $error
		echo Failure after $i itterations
		exit $error
	fi
}

function build()
{
	mvn clean
	rm -f build
	mvn install
}

function test()
{
	./test.sh auto 
	error_exit
}

pushd ../
build
for i in $(seq 1 1000); do
	test
	sleep_time=$(($RANDOM % 5))
	echo sleeping: $sleep_time
	sleep $sleep_time
done
popd

