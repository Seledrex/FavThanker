echo "Packaging..."

FILE="./target/FavThanker-1.0-SNAPSHOT-jar-with-dependencies.jar"
if test -f "$FILE"; then
  rm FavThanker.zip
  cp $FILE package/FavThanker.jar
  zip -r FavThanker.zip package
  rm package/FavThanker.jar
  echo "Done packaging!"
fi

