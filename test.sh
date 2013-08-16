#/bin/sh

TEST_DIR="./test"
BUILD_DIR=$PWD
PROJECT=test

function clean()
{
	$BUILD_DIR/build -c --config $PROJECT.config $PROJECT.build
}

function test1()
{
	echo "Compiling test project with all flags NO"
	$BUILD_DIR/build --config $PROJECT.config $PROJECT.build --configure --allno-config
	$BUILD_DIR/build --config $PROJECT.config $PROJECT.build -t random
}

function test2()
{
	echo "Compiling test project with all flags YES"
	$BUILD_DIR/build --config $PROJECT.config $PROJECT.build --configure --allyes-config
	$BUILD_DIR/build --config $PROJECT.config $PROJECT.build -t random
}

function test3()
{
	echo "Compiling test project with user defined flags"
	$BUILD_DIR/build --config $PROJECT.config $PROJECT.build --configure
	$BUILD_DIR/build --config $PROJECT.config $PROJECT.build -t random
}

echo "Testing build..."

cd $TEST_DIR

echo " ** Test 1:"
test1
clean

echo " ** Test 2:"
test2
clean

echo " ** Test 3:"
test3
clean

cd $BUILD_DIR
