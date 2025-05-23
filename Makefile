# Compiler
JAVAC = javac

# Directories
SRC_DIR = backEnd/src
BIN_DIR = backEnd/bin
ARGS=""

# Source files
SOURCES = $(shell find $(SRC_DIR) -name "*.java")

# Main class with package name
MAIN = backEnd/src/Simulation

# Compile all .java files to .class files in the bin directory
all: $(SOURCES)
	@mkdir -p $(BIN_DIR)
	$(JAVAC) -d $(BIN_DIR) $(SOURCES)

# Run the main class
run: all
	java -cp $(BIN_DIR) $(MAIN) $(ARGS)


# Clean compiled classes
clean:
	rm -rf $(BIN_DIR)

# Phony targets
.PHONY: all run clean
