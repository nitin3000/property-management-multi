set JAVA_HOME=C:\Users\nitin\spring-boot-redis\day1\jdk-17.0.12_windows-x64_bin\jdk-17.0.12

set PROJECT_HOME=C:\Users\nitin\property-management

cd %PROJECT_HOME%

mvnw clean package

%JAVA_HOME%\java -jar target/propapp-0.0.1-SNAPSHOT.jar