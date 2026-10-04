echo {"city":"Pleasantville","price":550000.0,"bedrooms":3,"title":"Load Test Item","source":"MANUAL"} > payload.json

ab -n 5000 -c 50 -p payload.json -T application/json http://property-service-uat-nitin3000-dev.apps.rm2.thpm.p1.openshiftapps.com/api/listings

  
