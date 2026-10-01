set OC_HOME=C:\Users\nitin\property-management-main\java\kafka-openshift\oc

oc new-app centos/postgresql-12-centos7 -e POSTGRESQL_USER=postgresadmin -e POSTGRESQL_PASSWORD= -e POSTGRESQL_DATABASE=property_db --name=property-postgres


