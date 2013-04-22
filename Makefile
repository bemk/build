.PHONY: all clean distclean native bin/build
all: build.jar
	@java -jar build.jar --config new.config new.build -t random $(FLAGS)

clean:
	@java -jar build.jar -c --config new.config new.build -t random $(FLAGS)

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
