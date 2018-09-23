
![Alt Text](https://media.giphy.com/media/dYGkUpZeNgbcVq1rNt/giphy.gif)

# knx2j

Knx2j is a native Java command line application. It is designed to read knx Temperature and Humidity values from given groupadresses and writes them to a Database using the [vz middleware]. You need a working java and knx router/gateway installation, including the knxtools to read the groupadress values from the knx bus. Knx2j is also designed to work with the old [eibd]. But I prefer the open source [knxd].

### Installation and configuration
```sh
git clone https://github.com/GeminiServer/knx2j.git
cd knx2j/release
```
Open the knx2j.json file with a editor of your choice and set your settings:
```sh
$ nano knx2j.json
```
### knx2j.json - Configuration Description and file example
  
| Section | Setting | Description |Value|
|------ | ------ | ----------- |---|
|general   | update-interval    | Read and write timer in Minutes. Default is 10 minutes.       | minute     |
|general   | enable-logging     | Enables output of debug-log informations.                     | true/false |
|middleware| host               | [FQDN] or IP adress of the host.                              | IPv4/url   |
|middleware| path               | Path to the Middleware. ( http://host/middleware.php/data/    | path       |
|middleware| user-agent         | [User Agent] Default is Mozilla/5.0.                          | string     |
|knx       | groupreadresponse  | Knx tool path to the groupreadresponse binary                 | path       |
|knx       | responsemessage    | Response message identification string. Default: "Write from" | string     |
|knx       | ip                 | IP adress of the knx router/gateway                           | IPv4       |
|knx       | port               | Port of the knx router/gateway. Default 6720                  | port       |
|sensors   | description        | Description of the Sensor                                     | String     |
|sensors   | groupadress        | Groupadress of the sensor to receive the value from. (i.e. 6/0/0) |  GA    |
|sensors   | type               | 'thermal': DPT_Value_Temp - 'humidity': DPT_Value_Humidity    | 'thermal' or 'humidity'  | port       |
|sensors   | uuid               | UUID of the Sensor in VZ. For more Information check [VZ Wiki]      | UUID       |

Here is a example of the knx2j.json file: 
```json
{
  "general":[{
    "update-interval": 10,
    "enable-logging": true
  }],

  "middleware":[{
     "host": "kronos",
     "path": "middleware.php/data/",
     "user-agent": "Mozilla/5.0"
  }],

  "knx":[{
     "groupreadresponse_old":    "/usr/lib/knxd/groupreadresponse",
     "groupreadresponse_new":    "/usr/bin/knxtool groupreadresponse",
     "groupreadresponse_cached": "/usr/bin/knxtool groupcacheread",
     "groupreadresponse":        "/usr/bin/knxtool groupcachereadsync",
     "responsemessage":          "rite from",
     "reponseidentifier":        ":",
     "ip": "localhost",
     "port": "6720"
  }],

  "sensors": [
    { "description": "Außen Süden"       ,"groupadress": "6/0/0"   ,"type": "thermal"   ,"uuid": "c8758450-aac3-11e3-ae70-71656855ff53" },
    { "description": "Außen Norden"      ,"groupadress": "6/0/1"   ,"type": "humidity"   ,"uuid": "c3407300-9d4b-11e3-8eb8-99224c3b70e8" }
  ]
}
```

After your configurations is set, just start the knx2j:
```sh
java -jar knx2j.jar
```


[vz middleware]: <https://wiki.volkszaehler.org/software/middleware/installation>
[knxd]: <https://github.com/knxd/knxd>
[FQDN]: <https://en.wikipedia.org/wiki/Fully_qualified_domain_name>
[User Agent]: <https://en.wikipedia.org/wiki/User_agent>
[VZ Wiki]: <https://wiki.volkszaehler.org/software/middleware/einrichtung>
