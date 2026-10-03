set OC_HOME=C:\Users\nitin\property-management-main\java\kafka-openshift\oc

rem For Prod
%OC_HOME%\oc logs -f deployment/property-service-prod

rem For UAT
%OC_HOME%\oc logs -f deployment/property-service-uat


