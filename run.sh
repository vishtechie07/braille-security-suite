#!/bin/bash
echo "Building Braille Script Printing App..."
mvn clean compile
if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

echo "Build successful! Starting application..."
mvn javafx:run
