set OC_HOME=C:\Users\nitin\property-management-main\java\kafka-openshift\oc

# Make sure you are in your dev project
%OC_HOME%\oc project nitin3000-dev

# Deploy the UAT version of the app
%OC_HOME%\oc logs -f property-service-prod

