#!/bin/bash

mkdir -p out

# compile
javac -d out -cp "lib/*" $(find src -name "*.java")

# package into a JAR
if [ $? -eq 0 ]; then
    mkdir -p out/lib
    cp -r lib/* out/
	
    # build JAR with UDPCast
    jar cfm ParallelGroupDownloader.jar MANIFEST.MF -C out .

    echo "JAR file 'ParallelGroupDownloader.jar' created successfully"
else
    echo "Compilation error"
fi
