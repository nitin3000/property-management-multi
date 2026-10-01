set OC_HOME=C:\Users\nitin\property-management-main\java\kafka-openshift\oc

oc set env deployment/property-service-uat DB_HOST=property-postgres DB_PORT=5432 DB_NAME=property_db DB_USERNAME=postgresadmin DB_PASSWORD=your_secure_password


%OC_HOME%\oc get pods

%OC_HOME%\oc get deployments

%OC_HOME%\oc logs -f deployment/property-service-prod
