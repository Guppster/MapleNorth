@echo off
@title MapleNorth
set CLASSPATH=.;dist\*
java -Xmx2048m -Dwzpath=wz\ net.server.Server
pause