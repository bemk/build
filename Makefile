.PHONY: all
all:
	@java -jar build.jar --config new.config new.build $(FLAGS)

.PHONY: clean
clean:
	@java -jar build.jar -vc --config new.config new.build $(FLAGS)
