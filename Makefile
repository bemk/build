.PHONY: all clean distclean native bin/build configure test allno_config allyes_config random_config configure preconfig
all: build
	@./build --config new.config new.build -t random $(FLAGS)

clean: build
	@rm -rf ./src/main/java/META-INF ./src/main/java/org
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
	@unzip -o -qq ~/.m2/repository/org/json/json/20090211/json-20090211.jar -d ./src/main/java
	@rm -rf ./src/main/java/META-INF
	./check_bin.sh
	gcj --main=eu.orionos.build.Build -o bin/build `find -name *.java`
	@rm -rf ./src/main/java/org

test: all
	./bin/main.bin
