//    _____  ____             __        __   _____  ____               ____
//   / ____||___ \           /_ |      /_ | / ____||___ \             |___ \
//  | |  __   __) | _ __ ___  | | _ __  | || (___    __) | _ __ __   __ __) | _ __
//  | | |_ | |__ < | '_ ` _ \ | || '_ \ | | \___ \  |__ < | '__|\ \ / /|__ < | '__|
//  | |__| | ___) || | | | | || || | | || | ____) | ___) || |    \ V / ___) || |
//   \_____||____/ |_| |_| |_||_||_| |_||_||_____/ |____/ |_|     \_/ |____/ |_|
//
//  Name: knx2j
//  Description:
//  Version: 0.0.6
//
//  Author: Erkan Colak
//

import java.io.*;
import java.util.*;
import java.lang.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.xml.bind.DatatypeConverter;
import java.net.URL;
import java.net.URLConnection;
import G3m1n1S3rv3r.*;
import com.eclipsesource.json.*;

public class knx2j
{
  private static String strStartString = "Name: knx2j\nVersion: v0.0.6\nAuthor: Erkan Colak";
  private static String strCfgFile = "knx2j.json"; // default json settings file

  private static int iSleepTime           = 10;    // Read and write timer in minutes. Default is 10 minutes
  private static boolean bDebug           = false; // print all debug-log informations
  private static boolean bClassDebug      = false; // print all debug-log informations
  private static String strHOST           = "";    // hostname
  private static String strMiddleWare     = "";    // url-path to vz middleware
  private static String USER_AGENT        = "";    // user-agent strint
  private static String strGRPResponseBIN = "";    // KNX read and response binarie
  private static String strGRPResponseMSG = "";    // Message identifier to convert the reponse value after ':'
  private static String strGRPResponseIDT = "";    // Message identifier to convert the reponse value after ':'
  private static String strKNX_IP         = "";    // knx ip of eibd or knxd or of your ip gateway
  private static String strKNX_PORT       = "";    // knx port

  private static boolean ReadConfig () {
    boolean bRet=true;
    FileReader rjson= null;
    try {
      rjson = new FileReader(strCfgFile);
      JsonObject ObjParse= Json.parse(rjson).asObject();
      if(ObjParse != null) {
        if( ObjParse.get("general") != null && ObjParse.get("general").asArray() != null && ObjParse.get("general").asArray().get(0).asObject() != null ) {
          JsonObject objsettings = ObjParse.get("general").asArray().get(0).asObject();
          iSleepTime  = objsettings.getInt("update-interval", 10);
          bDebug = bClassDebug = objsettings.getBoolean("enable-logging", true);

        } else { System.out.println("No 'general' settings defined."); bRet= false; }
        if( ObjParse.get("middleware") != null && ObjParse.get("middleware").asArray() != null && ObjParse.get("middleware").asArray().get(0).asObject() != null ) {
          JsonObject objmiddleware = ObjParse.get("middleware").asArray().get(0).asObject();

          strHOST       = objmiddleware.getString("host","kronos");
          strMiddleWare = objmiddleware.getString("path","sz/middleware.php/data/");
          USER_AGENT    = objmiddleware.getString("user-agent","Mozilla/5.0");

        } else { System.out.println("No 'middleware' settings defined."); bRet= false; }
        if( ObjParse.get("knx") != null && ObjParse.get("knx").asArray() != null && ObjParse.get("knx").asArray().get(0).asObject() != null ) {
          JsonObject objknx = ObjParse.get("knx").asArray().get(0).asObject();

          strGRPResponseBIN = objknx.getString("groupreadresponse","/usr/bin/groupreadresponse");
          strGRPResponseMSG = objknx.getString("responsemessage","esponse");
          strGRPResponseIDT = objknx.getString("responseidentifier",":");
          strKNX_IP         = objknx.getString("ip","localhost");
          strKNX_PORT       = objknx.getString("port","6720");

        } else { System.out.println("No 'knx' settings defined."); bRet= false; }
      } else bRet= false;

      if(bRet && bDebug){
       System.out.println("#### Extracted settings");
       System.out.println("  iSleepTime: "+iSleepTime);
       System.out.println("  bDebug: "+bDebug);
       System.out.println("  bClassDebug: "+bClassDebug);
       System.out.println("  strHOST: "+strHOST);
       System.out.println("  strMiddleWare: "+strMiddleWare);
       System.out.println("  USER_AGENT: "+USER_AGENT);
       System.out.println("  strGRPResponseBIN: "+strGRPResponseBIN);
       System.out.println("  strGRPResponseMSG: "+strGRPResponseMSG);
       System.out.println("  strGRPResponseIDT: "+strGRPResponseIDT);
       System.out.println("  strKNX_IP: "+strKNX_IP);
       System.out.println("  strKNX_PORT: "+strKNX_PORT);
       System.out.println("#### Extracted settings");
       System.out.println("");
      }
    }
    catch (IOException e) { e.printStackTrace(); }
    finally { try { if (rjson != null) rjson.close(); } catch (IOException ex) { ex.printStackTrace(); } }
    return bRet;
  }

  private static JsonArray ReadSensors () {
    FileReader rjson= null;
    JsonArray jRet= null;
    try {
      rjson = new FileReader(strCfgFile);
      jRet= Json.parse(rjson).asObject().get("sensors").asArray();
    }
    catch (IOException e) { e.printStackTrace(); }
    finally { try { if (rjson != null) rjson.close(); } catch (IOException ex) { ex.printStackTrace(); } }
    return jRet;
  }

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
      try { // open input stream for reading
         BufferedReader br= new BufferedReader(new InputStreamReader( p.getInputStream()));
         while( (thisLine = br.readLine()) != null) ListCMD.add(thisLine);
      } catch(Exception e) { e.printStackTrace(); }

      for( int i=0; i < ListCMD.size(); i++ ) {
        if( ListCMD.get(i).indexOf(strGRPResponseMSG) > 0 ) {
          String strValue= ListCMD.get(i).substring(ListCMD.get(i).indexOf(strGRPResponseIDT) + 1).replaceAll("\\s","");
          if(bDebug) {
            System.out.println("   Original Response: '"+ListCMD.get(i)+"'");
            System.out.println("   Extracted Response: '"+strValue+"' at line: "+i);
          }
          if( strValue.length() > 0 && !strValue.toLowerCase().equals("ffff") ) { strRet= strValue; bNoBreak= false; }
          i= ListCMD.size(); // found the response line, ending search
        } else {
          strRet= "fffe"; //  read error. sensor is not responsing
          bNoBreak= false;
        }
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

    System.out.println(strStartString);
    System.out.println("Starting...");
    System.out.println("");

    System.out.println("Reading knx2j.json settings file...");
    if( !ReadConfig() ) {
     System.out.println("Error: Cloud not read all settings! ");
     return;
    }

    JsonArray jKNXSensors = ReadSensors();
    int iSizeKNXSensors= jKNXSensors.size();
    if( iSizeKNXSensors < 1 ) {
      System.out.println("No sensor is defined! Please define sensors in knx2j json.");
      return;
    } else {
      System.out.println("Found: "+iSizeKNXSensors+" Sensors.");
      System.out.println("The values will be read and written every: "+iSleepTime+" minutes.");
    }

    G3m1n1S3rv3r  gCL= new G3m1n1S3rv3r();
    if( gCL.SetConfig( strHOST, strMiddleWare, USER_AGENT, bClassDebug ) ) {
      boolean bThreadSleep= true;
      SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SS");

      while(true)
      {
        int i=0;
        long unixTime = System.currentTimeMillis() / 1000L;
        String strCurTime= sdf.format( new Date( System.currentTimeMillis() ) );
        for( i= bThreadSleep ? 0 : i; i < iSizeKNXSensors; i++)
        {
          String strDescription= jKNXSensors.get(i).asObject().getString("description","");
          String strID         = jKNXSensors.get(i).asObject().getString("groupadress","");
          String strType       = jKNXSensors.get(i).asObject().getString("type","");
          String strUUID       = jKNXSensors.get(i).asObject().getString("uuid","");
          boolean bUnitIsThermal=true;

          if(bDebug) System.out.print("\n\n\n######## BEGIN Sensor: "+strDescription);
          if( strID.length() >0 )
          {
            String strExec="";
            String strHex="";
            String strDisplayUnit=" ??";

            switch (strType.toLowerCase()) {
              case "thermal":  strDisplayUnit=" °C"; break;
              case "humidity": strDisplayUnit=" %%"; bUnitIsThermal=false; break;
            }

            if(bDebug) System.out.println(bUnitIsThermal ? " (Sensor case: thermal)" : " (Sensor case: humidity)" );
            strExec= strExec.format("%s ip:%s:%s %s",strGRPResponseBIN, strKNX_IP, strKNX_PORT, strID);// | grep : | cut -d':' -f2",strID );
            if(bDebug) System.out.println("### BEGIN -  Read");
            strHex= ReadStream(strExec);


            if( strHex.length() >1 &&
                !strHex.toLowerCase().equals("ffff") && // recieved wrong value
                !strHex.toLowerCase().equals("fffe") )  // no data received
            {
              if(!bThreadSleep)
              {
                bThreadSleep=true;
              }
              String strRxData=""; strRxData= strRxData.format("%d", Eis52Value(Hex2Dec(strHex, 4)));
              double dRxData = (double)strToInt(strRxData) / 100;
              if(bDebug) System.out.println("   Converted Response: "+dRxData);


              if(bDebug) System.out.println("## BEGIN - Sending DATA");
              boolean bSendHttp= gCL.SendTemp( unixTime, strDescription, strID, strUUID, strType, dRxData );
              if(bDebug)
              {
                String strPrintValue="";
                strPrintValue= strPrintValue.format("Time:%d ID: %s Description: %s Value: "+dRxData+strDisplayUnit,unixTime,strID,strDescription);
                System.out.println("          Time: "+unixTime+"  ("+strCurTime+")");
                System.out.println("     Sensor ID: "+strID);
                System.out.println("   Description: "+strDescription);
                System.out.println("  Sensor Value: "+dRxData+strDisplayUnit);
                System.out.println("      Sensor #: "+i);
                System.out.println("## END - Sending DATA");
              }
            }
            else
            {
              if(bDebug) {
                System.out.println("## BEGIN - ERROR");
                System.out.println("          Time: "+unixTime+"  ("+strCurTime+")");
                System.out.println("     Sensor ID: "+strID);
                System.out.println("   Description: "+strDescription);
                System.out.println("  Sensor Value: "+strHex);
                System.out.println("      Sensor #: "+i);
              }

              switch(strHex.toLowerCase())
              {
                case "ffff": {
                  bThreadSleep= false; // retry immediately 2 sec. wait
                  if(bDebug) System.out.println("     E R R O R: Wrong value received from sensor. Trying to read again! (Entering Retry-Mode)");
                } break;
                case "fffe": {
                  if(bDebug) System.out.println("     E R R O R: No data received. Sensor is not responding!?");
                } break;
                default:
                  if(bDebug) System.out.println("     E R R O R: Unknown return code received. Code is:"+ strHex.toLowerCase());
                break;
              } // switch( ...
              if(bDebug) System.out.println("## END - ERROR");
            } // else ...
          } //  if( strID ...
          if(bDebug) System.out.println("### END - Read");
          if(bDebug) System.out.print("######## END   Sensor: "+strDescription);
          if(bDebug) System.out.println(bUnitIsThermal ? " (Sensor case: thermal)" : " (Sensor case: humidity)" );
        } // for( ...
        // Sleep in minutes
        try {
          if( bThreadSleep ) {
            if(bDebug) System.out.println("\n\n### Before starting next read & write. Sleeping for: "+iSleepTime+" Minutes.");
            Thread.sleep(mtomil(iSleepTime)/*5000*/);
          } else {
            if(bDebug) System.out.println("\n\n### Starting retry read in 2 Seconds.");
            Thread.sleep(/*Sleep 2 Seconds and try again*/ 2000);
          }
        } catch( InterruptedException ex ) { Thread.currentThread().interrupt(); }
      } // while(true)
    } // gCL.SetConfig
  } //public static void main
} //public class knx2j
