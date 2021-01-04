# Getting Started


## Primer
1. Build the mockserver docker file 
   `./docker/build.sh`
2. Run the mockserver 
   `docker-compose up mockserver`
3. Run the test. Only HttpSource and HttpEntity test for now. 
   `mvn clean test -Dtest=HttpSourceTest -DtrimStackTrace=false`
