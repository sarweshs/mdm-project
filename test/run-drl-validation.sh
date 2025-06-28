#!/bin/bash

# Script to compile and run DRL validation test
echo "=== DRL Validation Test Runner ==="

# Set up classpath with Drools dependencies
DROOLS_VERSION="8.44.0.Final"
CLASSPATH=""

# Add Drools dependencies (you may need to adjust paths based on your Maven setup)
# For now, we'll use the bot-core module's dependencies
cd mdm-bot-core

# Compile the test
echo "Compiling DRL validation test..."
javac -cp "$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q)" \
      -d target/test-classes \
      ../test/SimpleDrlValidationTest.java

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    
    # Run the test
    echo "Running DRL validation test..."
    java -cp "$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q):target/test-classes" \
         SimpleDrlValidationTest
else
    echo "Compilation failed!"
    exit 1
fi

cd .. 