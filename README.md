# The CANTO Framework
Enables the specification of neural network training jobs for which training happens in parallel on fog nodes. Provides an interface for job creation where parameters like dataset, neural network architecture, activation function, etc can be specified.

## Running the framework through docker
The image the node is running has to match its hardware architecture. There are two docker images: one for linux-based machines and the other for ARM architecture machines (RaspberryPis)
### On the same machine
All the workers can be started on the same machine. 
<code>docker stack deploy akkaFramework --compose-file docker-compose.yml</code>

### On a network of fog nodes


## Running the framework locally
