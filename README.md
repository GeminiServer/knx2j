# knx2j
Knx2j will read from groupadress (knx/eib) temperature and humidity values and writes them to a database or via sql or sz middleware. In this sample, the values will be written with a middleware into mysql database.

## Setup
- strGRPResponseBIN= "/usr/bin/groupreadresponse";
- strKNX_IP   = "localhost";
- strKNX_PORT = "6720";
- KNXSensors= Arrays.asList(
- //"DESCRIPTION"     ,  KNX Group Adress: "0/0/0", type: "thermal" | "humidity", UUID: "00000000-0000-0000-0000-000000000000",
-    "Außen Süden"     , "6/0/0"                   , "thermal"                   , "c8758450-aac3-11e3-ae70-71656855ff53",
