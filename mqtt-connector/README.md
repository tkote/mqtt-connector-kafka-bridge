# MQTT Connector

## parameter (should be set in microprofile-config.properties)

| parameter         | meaning                                    | default   |
|-------------------|--------------------------------------------|-----------|
| server            | hostname of mqtt broker                    | localhost |
| port              | port of mqtt proker                        | 1883      |
| topic             | topic to send                              |           |
| qos               | qos (0,1,2)                                | 1         |
| payload-type      | (publisher) conver to String when "string" | bytearray |
| encoding          | when payload-type is string                | UTF-8     |
| buffer-size       | (publisher) buffer size                    | 256       |
| monitor-interval  | interval of connection check in msec       | 5000      |  
| monitor-publisher | log publisher status                      | false     |   