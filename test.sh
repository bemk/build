#/bin/bash

TEST_DIR="./test"
BUILD_DIR=$PWD
PROJECT=test

function exit_error()
{
	error=$?
	if [ $error -ne 0 ] ; then
		echo Exit error $error
		exit $error 
	fi
}

function clean()
{
	$BUILD_DIR/build -c --config $PROJECT.config $PROJECT.build $FLAGS
	exit_error
}

function test_no()
{
	echo "Compiling test project with all flags NO"
	$BUILD_DIR/build --config $PROJECT.config $PROJECT.build --configure --allno-config $FLAGS
	exit_error
	$BUILD_DIR/build --config $PROJECT.config $PROJECT.build -t random $FLAGS
	exit_error
}

function test_yes()
{
	echo "Compiling test project with all flags YES"
	$BUILD_DIR/build --config $PROJECT.config $PROJECT.build --configure --allyes-config $FLAGS
	exit_error
	$BUILD_DIR/build --config $PROJECT.config $PROJECT.build -t random $FLAGS
	exit_error
}

function test_random()
{
	echo "Compiling test project with random flags"
	$BUILD_DIR/build --config $PROJECT.config $PROJECT.build --random-config $FLAGS
	exit_error
	$BUILD_DIR/build --config $PROJECT.config $PROJECT.build $FLAGS
	exit_error
	
}

function test_user()
{
	echo "Compiling test project with user defined flags"
	$BUILD_DIR/build --config $PROJECT.config $PROJECT.build --configure $FLAGS
	exit_error
	$BUILD_DIR/build --config $PROJECT.config $PROJECT.build -t random $FLAGS
	exit_error
}

echo "Testing build..."

cd $TEST_DIR

echo " ** Test 1:"
test_no
clean

echo " ** Test 2:"
test_yes
clean

echo " ** Test: 3"
test_random
clean

if [ "$1" != "auto" ]; then
echo " ** Test 4:"
test_user
clean
fi

cd $BUILD_DIR
