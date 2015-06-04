//    _____  ____             __        __   _____  ____               ____
//   / ____||___ \           /_ |      /_ | / ____||___ \             |___ \
//  | |  __   __) | _ __ ___  | | _ __  | || (___    __) | _ __ __   __ __) | _ __
//  | | |_ | |__ < | '_ ` _ \ | || '_ \ | | \___ \  |__ < | '__|\ \ / /|__ < | '__|
//  | |__| | ___) || | | | | || || | | || | ____) | ___) || |    \ V / ___) || |
//   \_____||____/ |_| |_| |_||_||_| |_||_||_____/ |____/ |_|     \_/ |____/ |_|
//
//  Author: Erkan Colak - 01.02.2014 / 09.05.2015
//
//

import java.io.*;
import java.util.*;
import java.lang.*;
import javax.xml.bind.DatatypeConverter;
import G3m1n1S3rv3r.*;

//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;

public class knx2j
{
  private static int iSleepTime   = 10;   // default 30 minutes
  private static boolean bDebug   = true; // default false

  // KNX Settings
  private static String strGRPResponseBIN= "/usr/bin/groupreadresponse";
  private static String strKNX_IP   = "localhost";
  private static String strKNX_PORT = "6720";

  private static List<String> KNXSensors= Arrays.asList(
 //"DESCRIPTION"     ,  KNX Group Adress: "0/0/0", type: "thermal" | "humidity", UUID: "00000000-0000-0000-0000-000000000000",
   "Außen Süden"     , "6/0/0"                   , "thermal"                   , "c8758450-aac3-11e3-ae70-71656855ff53",
   "Außen Norden"    , "6/0/1"                   , "thermal"                   , "c3407300-9d4b-11e3-8eb8-99224c3b70e8",
   "Garage"          , "6/0/4"                   , "thermal"                   , "912e60f0-f67f-11e4-a9e6-331b8e5a9090",
   "Garage DHT"      , "6/1/0"                   , "humidity"                  , "673a3720-f691-11e4-a198-17fbca1892ef",
   "Gaestezimmer"    , "6/0/5"                   , "thermal"                   , "c09c1090-f67f-11e4-82a4-87bc55045e87",
   "Gaestezimmer"    , "6/1/1"                   , "humidity"                  , "82784290-f691-11e4-8080-97a60fc9e550",
   "Buero"           , "6/0/6"                   , "thermal"                   , "d59bd540-f67f-11e4-8edf-abb3094bd0b0",
   "Buero"           , "6/1/2"                   , "humidity"                  , "a7a3d030-f691-11e4-9a92-f7299691870c",
   "Wohnzimmer"      , "6/0/7"                   , "thermal"                   , "e9f0ca40-f67f-11e4-9b88-399913118058",
   "Wohnzimmer"      , "6/1/3"                   , "humidity"                  , "cba1ab60-f691-11e4-90dd-cbb5b479a26f",
   "Esszimmer"       , "6/0/8"                   , "thermal"                   , "00c8b520-f680-11e4-875c-614b91d0efd1",
   "Esszimmer"       , "6/1/4"                   , "humidity"                  , "3ca0b220-f692-11e4-90e4-5d261be15ebc",
   "Kueche"          , "6/0/9"                   , "thermal"                   , "190d48c0-f680-11e4-9769-ef0904de4f35",
   "Kueche"          , "6/1/5"                   , "humidity"                  , "8b0808f0-f692-11e4-a548-11407954ac7e",
   "Schlafzimmer"    , "6/0/10"                  , "thermal"                   , "27b9a0b0-f680-11e4-8348-df416d69339c",
   "Schlafzimmer"    , "6/1/6"                   , "humidity"                  , "125c8b40-f693-11e4-9f4e-4bcb5ce0a511",
   "Taylan"          , "6/0/11"                  , "thermal"                   , "45677110-f680-11e4-ab93-513bbf1c5ded",
   "Taylan"          , "6/1/7"                   , "humidity"                  , "217c9510-f693-11e4-81f3-bbac56cd481e",
   "Helin"           , "6/0/12"                  , "thermal"                   , "55d19df0-f680-11e4-a3b1-9d56ef45b14d",
   "Helin"           , "6/1/8"                   , "humidity"                  , "2e88bcc0-f693-11e4-af32-a56a796d387f",
   "Badezimmer"      , "6/0/13"                  , "thermal"                   , "67063430-f680-11e4-a790-3125d777dff2",
   "Badezimmer"      , "6/1/9"                   , "humidity"                  , "3be37270-f693-11e4-8918-254846c58684",
   "Studio"          , "6/0/14"                  , "thermal"                   , "7cf3df80-f680-11e4-af30-919eebf2cd30",
   "Studio"          , "6/1/10"                  , "humidity"                  , "4b0221d0-f693-11e4-adf0-a15873e8fffb",
   "Geaste Bad"      , "6/0/15"                  , "thermal"                   , "9110eef0-f680-11e4-b064-53d78011f460",
   "Geaste Bad"      , "6/1/11"                  , "humidity"                  , "6029db90-f693-11e4-8629-5d69f324e739",
   "Heizungsraum"    , "6/0/16"                  , "thermal"                   , "e53fc6b0-fcd9-11e4-8e0d-675b5eeba782",
   "Heizungsraum"    , "6/1/12"                  , "humidity"                  , "eb2b9c90-fcd9-11e4-8757-fb0c275f6535"
  );

  private static int mtomil(int iMinute) { return iMinute*60000; }

  private static int strToInt( String str ) {
    int i= 0; int num= 0; boolean isNeg= false;
    //check for negative sign; if it's there, set the isNeg flag
    if( str.charAt(0) == '-') { isNeg = true; i = 1; }
    //process each char of the string;
    while( i < str.length()) { num *= 10; num += str.charAt(i++) - '0'; }
    if(  isNeg ) num= -num; return num;
  }

  private static int Hex2Dec(String hex, int maxDigits) {
    String hexNumber = "0123456789ABCDEF";
    int address = 0;
    int errorNr =0;

    hex = hex.toUpperCase();
    if (hex.length() > maxDigits)
    {
      errorNr = 3;
      return errorNr;
    }

    for (int byteCount = 0; byteCount < hex.length(); byteCount++)
    {
      if (hexNumber.indexOf(hex.substring(byteCount, byteCount+1)) == -1)
      {
        errorNr = 1;
        return errorNr;
      }
      else
      {
        address = address * 16 + hexNumber.indexOf(hex.substring(byteCount, byteCount+1));
      }
    }
    return address;
  }

  private static int Value2Eis5( int value ) {
    int eis5 = 0;
    int exponent = 0;
    if ( value < 0)
    {
      eis5 = 0x08000;
      value = -value;
    }
    while (value > 0x07ff)
    {
      value >>= 1;
      exponent++;
    }
    if (eis5 != 0) value = - value;
    eis5 |= value & 0x7ff;
    eis5 |= (exponent << 11) & 0x07800;
    return eis5 & 0x0ffff;
  }

  private static int Eis52Value( int eis5 ) {
    int value = eis5 & 0x07ff;
    if ((eis5 & 0x08000) != 0)
    {
      value |= 0xfffff800;
      value = -value;
    }
    value <<=  ((eis5 & 0x07800) >> 11);
    if ((eis5 & 0x08000) != 0) value = -value;
    return value;
  }

  private static String ReadStream(String cmd) throws Exception {
    String strRet= "ffff"; boolean bNoBreak= true;
    while( bNoBreak )
    {
      String thisLine;
      List<String> ListCMD= new ArrayList<String>();
      Runtime r= Runtime.getRuntime(); Process p= r.exec(cmd);
      if(bDebug) System.out.println(cmd);
      try{ // open input stream for reading
         BufferedReader br= new BufferedReader(new InputStreamReader( p.getInputStream()));
         while( (thisLine = br.readLine()) != null) {
            ListCMD.add(thisLine);
          if(bDebug) System.out.println(thisLine);
        }
      } catch(Exception e)  { e.printStackTrace(); }

      if(bDebug) System.out.println("list size: "+ ListCMD.size());
      if( ListCMD.size() > 2 && ListCMD.get(2).indexOf("esponse") > 0 ) {
        String strValue= ListCMD.get(2).substring(ListCMD.get(2).indexOf(":") + 1).replaceAll("\\s","");
        if( strValue.length() > 0 && !strValue.toLowerCase().equals("ffff") )
        {
          strRet= strValue;
          bNoBreak= false;
        }
      } else {
        if( ListCMD.size() < 3 ) strRet= "fffe"; //READ ERROR
          bNoBreak= false;
      }
    }
   return strRet;
  }

  private static boolean AddToMySQL(long lTime, String strDescription, String strType, String strValue) {
    boolean bRet= true;
    return bRet;
  }

  private static String toHexString(byte[] array) {
    return DatatypeConverter.printHexBinary(array);
  }

  private static byte[] toByteArray(String s) {
    return DatatypeConverter.parseHexBinary(s);
  }

  public static void main( String[] args ) throws IOException, Exception {
    int iSizeKNXSensors= KNXSensors.size();
    if( iSizeKNXSensors < 2 ) {
      System.out.println("No Sensors defines!");
      return;
    } else { System.out.println("Found: "+iSizeKNXSensors/4+" Sensors. Starting now to read and write the values ..."); }

    G3m1n1S3rv3r  gCL= new G3m1n1S3rv3r();
    //JSONParser parser=new JSONParser();
    boolean bThreadSleep= true;
    int iReTrySensor= 0;

    while(true)
    {
      int i=0;
      long unixTime = System.currentTimeMillis() / 1000L;
      for( i= bThreadSleep ? 0 : iReTrySensor ; i < KNXSensors.size(); i=i+4)
      {
        String strDescription= KNXSensors.get(i);  // Description
        String strID= KNXSensors.get(i+1);         // GROUP i.e. 6/0/0
        String strType= KNXSensors.get(i+2);       // Type
        String strUUID= KNXSensors.get(i+3);       // UUID

        if( strID.length() >0 )
        {
          String strExec="";
          String strHex="";
          String strDisplayUnit=" ??";

          switch (strType.toLowerCase()) {
            case "thermal":  strDisplayUnit=" °C"; break;
            case "humidity": strDisplayUnit=" %%"; break;
          }

          if(bDebug) System.out.println("Entering case: thermal");
          strExec= strExec.format("%s ip:%s:%s %s",strGRPResponseBIN, strKNX_IP, strKNX_PORT, strID);// | grep : | cut -d':' -f2",strID );
          strHex= ReadStream(strExec);
          if( strHex.length() >1 &&
              !strHex.toLowerCase().equals("ffff") && // recieved wrong value
              !strHex.toLowerCase().equals("fffe") )  // no data received
          {
            if(!bThreadSleep)
            {
              bThreadSleep=true;
              if(bDebug) System.out.println("################# Retry MODE #####################");
            }
            String strRxData=""; strRxData= strRxData.format("%d", Eis52Value(Hex2Dec(strHex, 4)));
            double dRxData = (double)strToInt(strRxData) / 100;

            boolean bSendHttp= gCL.SendTemp( unixTime,strDescription, strID, strUUID, strType, dRxData, bDebug );
            if(bDebug)
            {
              String strPrintValue=""; strPrintValue= strPrintValue.format("Time:%d ID: %s Description: %s Value: "+dRxData+strDisplayUnit,unixTime,strID,strDescription);
              System.out.println(strPrintValue);
              System.out.println("");
              System.out.println("i: "+i);
              System.out.println("iReTrySensor: "+iReTrySensor);
              System.out.println("bThreadSleep: "+bThreadSleep);
            }
          }
          else
          {
            switch(strHex.toLowerCase())
            {
              case "ffff": {
                if(bDebug) System.out.println("################# FFFF: Wrong Value - Try Retry MODE #####################");
                iReTrySensor= ( i == 0 ) ? i=0: i-4;
                bThreadSleep= false; // retry immediately 2 sec. wait
                if(bDebug)
                {
                  System.out.println("Sensor ID: "+strID);
                  System.out.println("Sensor Value: "+strHex);
                  System.out.println("iReTrySensor: "+iReTrySensor);
                  System.out.println("bThreadSleep: "+bThreadSleep);
                  System.out.println("####################################################################");
                }
              } break;

              case "fffe": {
                if(bDebug) System.out.println("################# FFFE: No Data received. Skip to next #####################");
                if(bDebug)
                {
                  System.out.println("Sensor ID: "+strID);
                  System.out.println("Sensor Value: "+strHex);
                  System.out.println("iReTrySensor: "+iReTrySensor);
                  System.out.println("####################################################################");
                }
              } break;

              default:
                if(bDebug) System.out.println("################# Unknown Return-Code: "+ strHex.toLowerCase());
              break;
            } // switch( ...
          } // else ...
        } //  if( strID ...
      } // for( ...
      // Sleep in minutes
      try { if( bThreadSleep ) Thread.sleep(mtomil(iSleepTime)/*5000*/); else { Thread.sleep(/*Sleep 2 Seconds and try again*/ 3000); } } catch(InterruptedException ex) { Thread.currentThread().interrupt(); }
    } // while(true)
  } //public static void main
} //public class knx2j
