#!/bin/bash

OS=${1:?"Need OS type (windows|linux)"}

echo "Packaging..."

FILE="./target/FavThanker-1.0-SNAPSHOT-jar-with-dependencies.jar"
JAR_TO_UPDATE="FavThanker-${OS}.jar"

if [ "$OS" == "windows" ]; then
  JAR_TO_KEEP="FavThanker-linux.jar"
else
  JAR_TO_KEEP="FavThanker-windows.jar"
fi

if test -f "$FILE"; then
  unzip FavThanker.zip $JAR_TO_KEEP
  cp $FILE "$JAR_TO_UPDATE"
  rm FavThanker.zip
  zip -j FavThanker.zip package/Template.json $JAR_TO_KEEP "$JAR_TO_UPDATE"
  rm "$JAR_TO_UPDATE"
  rm $JAR_TO_KEEP
  echo "Done packaging!"
fi

