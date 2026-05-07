#!/bin/bash

SOURCE=build/components-ios/PlayerApp.xcarchive/Products/Applications/PlayerApp.app
mkdir -p build/components-ios/Root/Payload
ditto -V "$SOURCE" build/components-ios/Root/Payload/PlayerApp.app
ditto -V build/components-ios/PlayerApp.xcarchive/SwiftSupport build/components-ios/Root/SwiftSupport
mkdir -p build/components-ios/thinned-out/Payload/PlayerApp.app
cd build/components-ios/Root
FILES=`find Payload/PlayerApp.app -perm 0755 -type f`
for FILE in ${FILES}; do
    echo $FILE
    ls -al $FILE
    mkdir -p $PWD/../thinned-out/`dirname $FILE`
    ARCHS=`lipo -archs $FILE`
    if [[ "${FILE##*.}" == "dylib" ]]; then
        echo "bitcode_strip $FILE -r -keep_cs -o $FILE"
        /Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/bin/bitcode_strip $FILE -r -keep_cs -o $FILE
    elif [[ "$ARCHS" != "arm64" ]]; then
        echo "Thinning $FILE"
        lipo -thin arm64 $FILE -output $PWD/../thinned-out/$FILE
        /Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/bin/bitcode_strip -r -o $FILE $PWD/../thinned-out/$FILE
        strip -ST $FILE
    fi
    lipo "$FILE" -verify_arch arm64e
    if [[ $? -eq 0 ]]; then
        lipo "$FILE" -remove arm64e -output "$FILE"
    fi
    ls -al $FILE
done
zip -r ../PlayerApp-unsigned.ipa Payload SwiftSupport
