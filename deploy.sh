docker-compose down
docker image rm dvemuri/dist_fog_comp:distML-lnx
gradle clean build shadowJar
docker build -t dvemuri/dist_fog_comp:distML-lnx .
docker push dvemuri/dist_fog_comp:distML-lnx
# docker stack deploy akkaClusterRouting --compose-file docker-compose.yml 
