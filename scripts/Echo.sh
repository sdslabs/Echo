#!/bin/bash

APP_NAME = Echo
APP_ROOT = $APPLICATIONS/$APP_NAME

dependencyFile = $APP_ROOT/bin/$APP_NAME.dependency
classpath = $APP_ROOT/bin/$APP_NAME.jar

while read jars; do
  classpath = $classpath;$APP_ROOT/lib/jars
done < dependencyFile
  
java -Dlogging.properties=$APPROOT/config/EchoLogging.cfg -DAPPROOT=$APP_ROOT -DAPPNAME=$APP_NAME -classpath=$classpath sdslabs.echo.EchoMain


