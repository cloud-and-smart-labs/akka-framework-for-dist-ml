# The CANTO Framework
Enables the specification of neural network training jobs for which training happens in parallel on fog nodes. Provides an interface for job creation where parameters like dataset, neural network architecture, activation function, etc can be specified.

## Running the framework through docker
All the workers can be started on the same machine. The image the machine is running has to match its hardware architecture. There are two docker images: one for linux-based machines, which uses 'openJDK' and the other for RaspberryPis, which uses 'arm32v7/gradle'.

<code>docker-compose up</code>

### Running the framework on fog nodes
In order to deploy the framework on a network of fog nodes, a [docker swarm](https://docs.docker.com/engine/reference/commandline/swarm/) has to be established after which it can be deployed through:

<code>docker stack deploy akkaFramework --compose-file docker-compose.yml</code>

## Running the framework locally
The framework can also be run locally using the gradle build tool. We build a FatJar out of the gradle project.
<code>gradle clean build shadowJar</code>

After the jar file has been built, actors are spun into existence.
<code>gradle run --args="master <PORT>"</code>
<code>gradle run --args="worker <PORT>"</code>
