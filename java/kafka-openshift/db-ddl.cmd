set OC_HOME=C:\Users\nitin\property-management-main\java\kafka-openshift\oc

%OC_HOME%\oc set env deployment/property-service-uat SPRING_JPA_HIBERNATE_DDL_AUTO="update"

%OC_HOME%\oc set env deployment/property-service-prod SPRING_JPA_HIBERNATE_DDL_AUTO="update"


%OC_HOME%\oc logs -f deployment/property-service-prod


