export CLASSPATH=$CLASSPATH:/usr/share/java/mysql-connector-java.jar;/usr/share/java/* &
$nohup java -jar knx2j.jar > knx2j.log 2>&1&
echo $! > save_pid.txt
