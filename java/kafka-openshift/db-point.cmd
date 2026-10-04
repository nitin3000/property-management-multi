set OC_HOME=C:\Users\nitin\property-management-main\java\kafka-openshift\oc

%OC_HOME%\oc set env deployment/property-service-prod SPRING_DATASOURCE_URL="jdbc:postgresql://postgresql:5432/sampledb" SPRING_DATASOURCE_USERNAME="" SPRING_DATASOURCE_PASSWORD="" SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT="org.hibernate.dialect.PostgreSQLDialect"

%OC_HOME%\oc set env deployment/property-service-uat SPRING_DATASOURCE_URL="jdbc:postgresql://postgresql:5432/sampledb" SPRING_DATASOURCE_USERNAME="" SPRING_DATASOURCE_PASSWORD="" SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT="org.hibernate.dialect.PostgreSQLDialect"

%OC_HOME%\oc get pods

%OC_HOME%\oc get deployments

%OC_HOME%\oc logs -f deployment/property-service-prod
