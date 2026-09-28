set OC_HOME=C:\Users\nitin\property-management-main\java\kafka-openshift\oc

rem Make sure you are in your dev project
%OC_HOME%\oc project nitin3000-dev

rem Deploy the UAT version of the app
%OC_HOME%\oc get svc

rem Deploy the UAT version of the app
%OC_HOME%\oc expose svc/property-service-prod

rem Deploy the UAT version of the app
%OC_HOME%\oc get route property-service-prod


