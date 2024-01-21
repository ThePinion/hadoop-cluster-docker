#!/bin/bash
cd ./books
find . -maxdepth 1 -type f -name '*' | while read fname; do
    # Strip './' from the start of each filename
    stripped_fname=${fname:2}
    # Replace non-alphanumeric characters with underscores and compress multiple underscores
    newname=$(echo $stripped_fname | sed 's/[^a-zA-Z0-9.]/_/g' | tr -s '_')
    mv "$fname" "./$newname"
done
cd ..