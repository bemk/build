.PHONY: all clean distclean native bin/build configure test allno_config allyes_config random_config configure 
all: build.jar
	@java -jar build.jar --config new.config new.build -t random $(FLAGS)

clean: build.jar
	@java -jar build.jar -c --config new.config new.build -t random $(FLAGS)

configure: build.jar
	@java -jar build.jar --config new.config new.build --configure

allyes_config: build.jar
	@java -jar build.jar --config new.config new.build --configure --allyes-config

allno_config: build.jar
	@java -jar build.jar --config new.config new.build --configure --allno-config

random_config: build.jar
	@java -jar build.jar --config new.config new.build --configure --random-config


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
