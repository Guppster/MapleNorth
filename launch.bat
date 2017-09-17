@echo off
@title MapleNorth
set CLASSPATH=.;dist\*
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -Xmx2048m -Dwzpath=wz\ net.server.Server
pause