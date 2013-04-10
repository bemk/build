.PHONY: all
all:
	@java -jar build.jar -v $(FLAGS)

.PHONY: clean
clean:
	@java -jar build.jar -vc $(FLAGS)
