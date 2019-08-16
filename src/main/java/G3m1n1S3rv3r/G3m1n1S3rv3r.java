//    _____  ____             __        __   _____  ____               ____
//   / ____||___ \           /_ |      /_ | / ____||___ \             |___ \
//  | |  __   __) | _ __ ___  | | _ __  | || (___    __) | _ __ __   __ __) | _ __
//  | | |_ | |__ < | '_ ` _ \ | || '_ \ | | \___ \  |__ < | '__|\ \ / /|__ < | '__|
//  | |__| | ___) || | | | | || || | | || | ____) | ___) || |    \ V / ___) || |
//   \_____||____/ |_| |_| |_||_||_| |_||_||_____/ |____/ |_|     \_/ |____/ |_|
//
//  Author: Erkan Colak - 01.02.2014
//  Will read the 1-Wire Temperatur Sensors.
//  class to write to a mySQL database
//

package G3m1n1S3rv3r;

import java.io.*;
import java.util.*;
import java.sql.*;
import java.net.InetAddress;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.io.PrintStream;
//import javax.net.ssl.HttpsURLConnection;

public class G3m1n1S3rv3r{
  private static String strHOST       = "kronos"; //hostname
  private static String strMiddleWare = "sz/middleware.php/data/";
  private static String USER_AGENT     = "Mozilla/5.0";
  private static boolean bDebug       = false;


  public static boolean SetConfig( String strSetHOST, String strSetMiddleWare, String strSetUserAgent, boolean bSetDebug ) {
          strHOST= strSetHOST;
    strMiddleWare= strSetMiddleWare;
       USER_AGENT= strSetUserAgent;
           bDebug= bSetDebug;
   return true;
  }


  private static String GetIP(String strFQDN)  {
    String strRet="0";
    try {
       InetAddress inet = InetAddress.getByName(strFQDN);
       if(bDebug) System.out.println ("G3m1n1S3rv3r class: IP: " + inet.getHostAddress());
       strRet= inet.getHostAddress();
    } catch ( Exception e ) {
      if(bDebug) System.out.println("G3m1n1S3rv3r class: Exception "+e);
    }
    return strRet;
  }

  public static boolean httpGet( String url ) throws Exception {
    G3m1n1S3rv3r http = new G3m1n1S3rv3r();
    if(bDebug) System.out.println("G3m1n1S3rv3r class: Send Http Get request");
    boolean bRet= http.sendGetAndPost(url, "", true);
    return bRet;
  }

  public static boolean httpPost( String url, String urlParameters ) throws Exception {
    G3m1n1S3rv3r http = new G3m1n1S3rv3r();
    if(bDebug) System.out.println("G3m1n1S3rv3r class: Send Http POST request");
    boolean bRet= http.sendGetAndPost(url, urlParameters, false);
    return bRet;
  }

  public static boolean URLPost( String url, String urlParameters ) throws Exception {
    G3m1n1S3rv3r http = new G3m1n1S3rv3r();
    if(bDebug) System.out.println("G3m1n1S3rv3r class: Send URL POST request");
    boolean bRet= http.sendURLGet(url, urlParameters );
    return bRet;
  }

  private boolean sendURLGet(String url, String urlParameters ) {
    boolean bRet= false;
    try {
      if(bDebug) System.out.println("G3m1n1S3rv3r class: Connecting to URL : " + url+urlParameters);
      URL obj = new URL(url+urlParameters);
      URLConnection con = obj.openConnection();
      con.setDoOutput(true);
      PrintStream ps = new PrintStream(con.getOutputStream());
      con.getInputStream();
      String line;
      BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
      while ((line = reader.readLine()) != null) {
        if(bDebug) System.out.println("G3m1n1S3rv3r class: Returned Stream: "+line);
      }
      reader.close();
      ps.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
    return bRet;
  }

  private boolean sendGetAndPost(String url, String urlParameters, boolean bSendGet ) throws Exception {
     boolean bRet= false;
     URL obj = new URL(url);
     HttpURLConnection con = (HttpURLConnection) obj.openConnection();

     //add header
     // optional default is GET
     con.setRequestMethod( bSendGet ? "GET" : "POST");
     con.setRequestProperty("User-Agent", USER_AGENT);
     if( !bSendGet ) con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

     int responseCode= 0;
     if( bSendGet ) {
       // HTTP GET request
       responseCode = con.getResponseCode();
       if(bDebug) System.out.println("G3m1n1S3rv3r class: Sending 'GET' request to URL : " + url);
       if(bDebug) System.out.println("G3m1n1S3rv3r class: Response Code : " + responseCode);
     } else {
       // HTTP Send post request
       con.setDoOutput(true);
       DataOutputStream wr = new DataOutputStream(con.getOutputStream());
       wr.writeBytes(urlParameters);
       wr.flush(); wr.close();

       responseCode = con.getResponseCode();
       if(bDebug) System.out.println("G3m1n1S3rv3r class: Sending 'POST' request to URL : " + url);
       if(bDebug) System.out.println("G3m1n1S3rv3r class: Post parameters : " + urlParameters);
       if(bDebug) System.out.println("G3m1n1S3rv3r class: Response Code : " + responseCode);

     }
     if(responseCode > 0 && responseCode == 200 ) {
       BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()));
       String inputLine; StringBuffer response = new StringBuffer();
       while ((inputLine = in.readLine()) != null) { response.append(inputLine); }
       in.close();
       if(bDebug) System.out.println(response.toString());
     }
     return bRet;
   }

  public static boolean SendTemp( long lTime, String strDescription, String str1WireID, String strUUID, String strType, double dValue ) throws Exception {
    String strHost= GetIP(strHOST);
    String strMiddleWarePath= strMiddleWare;
    String strSendUUID=""; strSendUUID= strSendUUID.format( "%s.json?",strUUID );

    String url=""; // url="http://host/sz/middleware.php/data/c3407300-9d4b-11e3-8eb8-99224c3b70e8.json?";
    String urlParameters=""; // ts=%d&value=23

    url= url.format("http://%s/%s%s", strHost, strMiddleWarePath, strSendUUID );

    urlParameters= urlParameters.format("value=")+dValue;
    //urlParameters= urlParameters.format("ts=%d&value=", lTime )+dValue;

    boolean bRet= URLPost(url, urlParameters);
    //boolean bRet= httpPost(url, urlParameters);

    return bRet;
  }
}
