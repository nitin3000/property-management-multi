set OC_HOME=C:\Users\nitin\property-management-main\java\kafka-openshift\oc

%OC_HOME%\oc set env deployment/property-service-uat SPRING_JPA_HIBERNATE_DDL_AUTO="update"

%OC_HOME%\oc set env deployment/property-service-prod SPRING_JPA_HIBERNATE_DDL_AUTO="update"


%OC_HOME%\oc logs -f deployment/property-service-prod

oc exec -it postgresql-2-rkp6n -- psql -U postgres -d property_db -c "CREATE TABLE IF NOT EXISTS processed_messages (message_id VARCHAR(255) PRIMARY KEY, processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);"

oc exec -it postgresql-2-rkp6n -- psql -U postgres -d property_db -c "\dt processed_messages"

