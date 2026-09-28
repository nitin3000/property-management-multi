set OC_HOME=C:\Users\nitin\property-management-main\java\kafka-openshift\oc

rem For Prod
%OC_HOME%\oc new-app postgresql-ephemeral  -p POSTGRESQL_USER=postgresadmin   -p POSTGRESQL_PASSWORD=   -p POSTGRESQL_DATABASE=property_db   --name=property-postgres

