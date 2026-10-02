
oc run cloud-load-tester --image=jordi/ab --rm -it --restart=Never --overrides="{\"spec\":{\"containers\":[{\"name\":\"cloud-load-tester\",\"image\":\"jordi/ab\",\"command\":[\"/bin/sh\"],\"stdin\":true,\"tty\":true}]}}"
cd /tmp
  
# 2. Generate your clean JSON payload file
echo '{"city":"Pleasantville","price":550000.0,"bedrooms":3,"title":"Load Test Item","source":"MANUAL"}' > payload.json

# 3. Fire the 500 requests/sec load test directly at your application service
ab -n 5000 -c 50 -p payload.json -T application/json http://property-service-uat-nitin3000-dev.apps.rm2.thpm.p1.openshiftapps.com/api/listings
 

