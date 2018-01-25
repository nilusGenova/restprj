#!/bin/sh

pathlog="/tmp/"
echo "Update write rights on following log files:"
for file in $pathlog*.log
do 
     echo "File: ${file}"
     touch ${file}
     chmod g+w ${file}
done

#java -DLogPath=$pathlog -DDebugLogLevel=defined -jar target/restprj-1.1-SNAPSHOT.jar
java -DLogPath=$pathlog -jar target/restprj-1.1-SNAPSHOT.jar

