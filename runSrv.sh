#!/bin/sh
pathlog="/home/alberto/hal9000Logs/"
templog="/tmp/"
echo "Update write rights on following log files:"
for file in $templog*.log
do 
     echo "File: ${file}"
     touch ${file}
     chmod a+w ${file}
done
for file in $pathlog*.log
do 
     echo "File: ${file}"
     touch ${file}
     chmod a+w ${file}
done

#java -DLogPath=$pathlog -DDebugLogLevel=defined -jar /home/alberto/restprj.jar
java -DLogPath=$pathlog -jar /home/alberto/restprj.jar
