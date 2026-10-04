oc set env deployment/property-consumer-uat SPRING_KAFKA_LISTENER_AUTO_STARTUP="false"

oc set env deployment/property-producer-uat SPRING_KAFKA_LISTENER_AUTO_STARTUP="false"
