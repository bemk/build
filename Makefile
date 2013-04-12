.PHONY: all
all:
	@java -jar build.jar --config new.config new.build $(FLAGS)

.PHONY: clean
clean:
	@java -jar build.jar -c --config new.config new.build $(FLAGS)
