set OC_HOME=C:\Users\nitin\property-management-main\java\kafka-openshift\oc

# Make sure you are in your dev project
%OC_HOME%\oc project nitin3000-dev

# Deploy the UAT version of the app
%OC_HOME%\oc new-app https://github.com/nitin3000/property-management-multi.git --context-dir=java --name=property-service-uat

# Deploy the Prod version of the app
%OC_HOME%\oc new-app https://github.com/nitin3000/property-management-multi.git --context-dir=java --name=property-service-prod


