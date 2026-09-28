echo {"city":"Pleasantville","price":550000.0,"bedrooms":3,"title":"Load Test Item"} > payload.json


 curl -X POST -H "Content-Type: application/json" -d @payload.json  http://property-service-uat-nitin3000-dev.apps.rm2.thpm.p1.openshiftapps.com/api/listings
 