docker-compose down
docker image rm dvemuri/dist_fog_comp:distML-rasp
gradle clean build shadowJar
docker build -t dvemuri/dist_fog_comp:distML-rasp .
docker push dvemuri/dist_fog_comp:distML-rasp
# docker stack deploy akkaClusterRouting --compose-file docker-compose.yml 