oc get pods 

oc exec -it <podname> -- psql -U postgresadmin -d property_db -c "SELECT * FROM listings LIMIT 10;"
