#!/bin/bash

ls *.jar > jars.txt
ls *.aar > aars.txt
for AAR in `ls *.aar`; do
    unzip $AAR classes.jar
    BASE="${AAR%.*}"
    if [ -f classes.jar ]; then
        echo "Copying classes.jar"
        mv classes.jar "$BASE.jar"
    fi
    zip -d "$AAR" classes.jar
done
