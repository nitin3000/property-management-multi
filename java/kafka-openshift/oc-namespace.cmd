set OC_HOME=C:\Users\nitin\property-management-main\java\kafka-openshift\oc

rem Create the UAT namespace
%OC_HOME%\oc new-project property-service-uat --description="Property Service UAT Environment"

rem Create the Production namespace
%OC_HOME%\oc new-project property-service-prod --description="Property Service Production Environment"

