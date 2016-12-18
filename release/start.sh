export CLASSPATH=$CLASSPATH:/usr/share/java/mysql-connector-java.jar;/usr/share/java/* &
$nohup java knx2j > knx2j.log 2>&1&
echo $! > save_pid.txt
