.PHONY: all
all:
	@java -jar build.jar --config new.config new.build -t cores $(FLAGS)

.PHONY: clean
clean:
	@java -jar build.jar -c --config new.config new.build -t cores $(FLAGS)
