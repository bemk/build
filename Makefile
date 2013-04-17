.PHONY: all clean distclean
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
