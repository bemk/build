.PHONY: all clean distclean native bin/build configure test allno_config allyes_config random_config configure preconfig
all: build
	@./build --config new.config new.build -t random $(FLAGS)

clean: build
	@./build -c --config new.config new.build -t random $(FLAGS)

configure: build
	@./build --config new.config new.build --configure

allyes_config: build
	@./build --config new.config new.build --configure --allyes-config

allno_config: build
	@./build --config new.config new.build --configure --allno-config

random_config: build
	@./build --config new.config new.build --configure --random-config

preconfig: build
	@./build --config new.config new.build --update-depfile


distclean: clean
	mvn clean
	rm -f build

build: build.jar
	cat stub.sh ./target/build.jar > build && chmod +x build

build.jar:
	mvn clean install

native: bin/build

bin/build:
	./check_bin.sh
	gcj --main=eu.orionos.build.Build -o bin/build `find -name *.java`

test: all
	./bin/main.bin
