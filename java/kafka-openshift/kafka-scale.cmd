set OC_HOME=C:\Users\nitin\property-management-main\java\kafka-openshift\oc

%OC_HOME%\oc scale deployment/property-service-prod --replicas=24

%OC_HOME%\oc logs -f deployment/property-service-prod

