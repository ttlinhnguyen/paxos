CLASSPATH := .:./target/classes:./target/test-classes

TEST := src/test/java/*.java

CLASSES := members/*.java \
	messages/*.java \
	*.java

CLASSES := $(foreach c, $(CLASSES), src/main/java/$(c))

all: compile tests

tests:
	java -cp $(CLASSPATH) Test

controller:
	java -cp $(CLASSPATH) Controller

compile: make_dir compile_class compile_test

make_dir:
	mkdir -p target
	mkdir -p target/classes
	mkdir -p target/test-classes

compile_class:
	javac -d ./target/classes/ -cp $(CLASSPATH) $(CLASSES)

compile_test:
	javac -d ./target/test-classes/ -cp $(CLASSPATH) $(TEST)


clean:
	rm *.class