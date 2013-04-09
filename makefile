.PHONY: all
all:
	@java -jar build.jar $(FLAGS)

.PHONY: clean
clean:
	@java -jar build.jar -c $(FLAGS)
