echo off

cd ..
set appHome=.
set cp=%appHome%/classes;%appHome%/resources;%appHome%/lib

SETLOCAL
FOR %%f IN (%appHome%/lib\*.jar) DO call :append_classpath %%f
GOTO :end
:append_classpath
set cp=%cp%;%1
GOTO :eof
:end

set javaOptions=-Xms12g -Xmx12g -XX:+UseG1GC -Dsun.java2d.d3d=false -cp "%cp%"
set mainClass=com.jbooktrader.platform.startup.JBookTrader

java %javaOptions% %mainClass%

