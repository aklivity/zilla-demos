# Taxi Caller Demo

This demo was created to showcase the MQTT protocol brokered by Zilla. It uses [Open Street Maps](https://www.openstreetmap.org/), [Open Route Service](https://openrouteservice.org/), and the [MQTT Simulator](https://github.com/DamascenoRafael/mqtt-simulator) to demonstrate a real world taxi hailing and location tracking service.

## Requirements

- Docker Compose

## Setup

1. Start all of the services using `docker-compose`. The `startup.sh` script will `build` and `start` all of the needed services. This command will also `restart` an existing stack.

    ```bash
    ./startup.sh
    ```

    > This will take a long time to build the first time it is run since it will need to download maven and npm packages.

1. Open the Open Street Maps UI at [localhost](http://localhost/). The map is centered on the San Jose Convention center.
1. Click somewhere or search for a local destination.
1. Click the directions button for the selected location.
1. The location will be filed as the destination with the San Jose Convention center being the origin.
1. A Taxi marker will appear along the route and travel along it for the duration shown in the popup.


## Load Testing

The mqtt-simulation service includes a `default_routes.json` file which starts a looping set of routes used in the demo. An additional file `default_routes_load_test.json` is available which leverages the simulators ability to generate multiple topics. 

1. You will see in the json file the config for managing the number of topics to generate by updating the `"RANGE_END"` value:

    ```json
    "TYPE": "multiple",
    "RANGE_START": 1,
    "RANGE_END": 500,
    ```

1. The `taxi-service` in the [docker-compose.yaml](docker-compose.yaml) file mounts the default config. Update the volume mount to map the load_test file.

    ```yaml
    volumes:
        - ./grpc/service/default_routes_load_test.json:/usr/src/app/default_routes.json
    ```

1. Ensure the `DEFAUlT_ROUTES` env var is true so the service will start the sim and the `PRINT_SIM_LOGS` is true so the container will print the simulator output.

    ```yaml
    environment:
        DEFAUlT_ROUTES: true
        PRINT_SIM_LOGS: true
    ```

1. Happy Load Testing!
