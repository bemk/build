.PHONY: all clean distclean native bin/build configure test
all: build.jar
	@java -jar build.jar --config new.config new.build -t random $(FLAGS)

clean: build.jar
	@java -jar build.jar -c --config new.config new.build -t random $(FLAGS)

configure: build.jar
	@java -jar build.jar --config new.config new.build --configure

distclean: clean
	mvn clean
	rm -f build.jar

build.jar:
	mvn clean install
	cp target/build.jar ./

native: bin/build

bin/build:
	./check_bin.sh
	gcj --main=eu.orionos.build.Build -o bin/build `find -name *.java`

test: all
	./bin/main.bin
