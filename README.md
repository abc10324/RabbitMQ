# RabbitMQ  
Testing pub/sub with RabbitMQ server   
  
## Environment  
 > Project: Gradle  
 > Framework: Spring MVC + Spring AMQP + Spring AMQP Rabbit  
 > Server: Tomcat v9.0  
 > MQ Server: RabbitMQ  
  
## Function  
 > Send message to RabbitMQ server  
 > Receive message from RabbitMQ server  
  
## how to use gradleDeploy.bat and gradleDebugDeploy.bat  
 1.set environment variable TOMCAT_HOME to your tomcat install location (ex:D:\apache-tomcat-9.0.22)  
 2.set environment variable JAVA_HOME to your java install location (ex:C:\Program Files\ojdkbuild\java-1.8.0-openjdk)  
 3.run gradleDeploy.bat/gradleDebugDeploy.bat(for remote debug, open port 8000)
 
