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
