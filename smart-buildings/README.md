# Running the IoT Sensors MQTT Sparkplug-B example

```
docker compose up -d
```
```
[+] Running 13/13
 ✔ Network iotsensorsmqtt-spb_main-net     Created                                                                                                           0.1s
 ✔ Network iotsensorsmqtt-spb_kafka-net    Created                                                                                                           0.1s
 ✔ Network iotsensorsmqtt-spb_nodered-net  Created                                                                                                           0.1s
 ✔ Network iotsensorsmqtt-spb_spB-net      Created                                                                                                           0.1s
 ✔ Container kafka                         Healthy                                                                                                          11.4s
 ✔ Container influxdb                      Healthy                                                                                                          29.0s
 ✔ Container kafka-init                    Exited                                                                                                           22.1s
 ✔ Container kafka-ui                      Started                                                                                                          11.6s
 ✔ Container grafana                       Started                                                                                                           6.6s
 ✔ Container mqtt                          Healthy                                                                                                          28.8s
 ✔ Container nodered                       Healthy                                                                                                          33.8s
 ✔ Container eonNode                       Started                                                                                                          34.1s
 ✔ Container prometheus                    Started                                                                                                          34.4s
 ```

Zilla MQTT server is available at mqtt://localhost:1883 and ws://localhost:8083/.

Kafka UI is available at http://localhost:8081/.

Node-RED sparkplug flow visualization is available at http://localhost:1880/.

Grafana is available at http://localhost:3000/.

Note: Grafana credentials are admin/mypasswordmypasswordmypassword.
