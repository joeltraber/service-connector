rem set default directory
cd "%~dp0"
rem start demo server
rem it will stop automatically
java -Dlog4j.configuration=file:..\conf\log4j-demo-server.properties -jar demo-server-${sc.version}.jar