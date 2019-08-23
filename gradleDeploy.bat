for %%a in (.) do set currentfolder=%%~na
echo %currentfolder%
if exist build\libs\%currentfolder%*.war del build\libs\%currentfolder%*.war
echo.|gradlew build
if exist %TOMCAT_HOME%\webapps\%currentfolder% rmdir %TOMCAT_HOME%\webapps\%currentfolder% /s /q
if exist %TOMCAT_HOME%\webapps\%currentfolder%.war del %TOMCAT_HOME%\webapps\%currentfolder%.war
move build\libs\%currentfolder%*.war %TOMCAT_HOME%\webapps\%currentfolder%.war
cd %TOMCAT_HOME%\bin
echo.|startup
echo Press any key after deploy test already finished
pause
echo.|shutdown
echo Press any key after shutdown process already finished
pause
if exist %TOMCAT_HOME%\webapps\%currentfolder% rmdir %TOMCAT_HOME%\webapps\%currentfolder% /s /q
if exist %TOMCAT_HOME%\webapps\%currentfolder%.war del %TOMCAT_HOME%\webapps\%currentfolder%.war