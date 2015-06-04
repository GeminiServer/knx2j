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
//import javax.net.ssl.HttpsURLConnection;

public class G3m1n1S3rv3r{
  private final String strHOST     = "kronos"; //hostname
  private final String strPort     = "3306";   // port
  private final String strDB       = "test";
  private final String strDBTable  = "temperatur";
  private final String strDBUser   = "geragepi";
  private final String strDBPass   = "garagepi";

  private final String USER_AGENT  = "Mozilla/5.0";

  private static boolean bDebug= false;

  private static String GetIP(String strFQDN)  {
    String strRet="0";
    try {
       InetAddress inet = InetAddress.getByName(strFQDN);
       if(bDebug) System.out.println ("IP  : " + inet.getHostAddress());
       strRet= inet.getHostAddress();
    } catch ( Exception e ) {
      if(bDebug) System.out.println("G3m1n1S3rv3r class: "+e);
    }
    return strRet;
  }

  public static boolean httpGet( String url ) throws Exception {
    G3m1n1S3rv3r http = new G3m1n1S3rv3r();
    if(bDebug) System.out.println("\nSend Http Get request");
    boolean bRet= http.sendGetAndPost(url, "", true);
    return bRet;
  }

  public static boolean httpPost( String url, String urlParameters ) throws Exception {
    G3m1n1S3rv3r http = new G3m1n1S3rv3r();
    if(bDebug) System.out.println("\nSend Http POST request");
    boolean bRet= http.sendGetAndPost(url, urlParameters, false);
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
      if(bDebug) System.out.println("\nSending 'GET' request to URL : " + url);
      if(bDebug) System.out.println("Response Code : " + responseCode);
    } else {
      // HTTP Send post request
      con.setDoOutput(true);
      DataOutputStream wr = new DataOutputStream(con.getOutputStream());
      wr.writeBytes(urlParameters);
      wr.flush(); wr.close();

      responseCode = con.getResponseCode();
      if(bDebug) System.out.println("\nSending 'POST' request to URL : " + url);
      if(bDebug) System.out.println("Post parameters : " + urlParameters);
      if(bDebug) System.out.println("Response Code : " + responseCode);

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

  public static boolean SendTemp( long lTime, String strDescription, String str1WireID, String strUUID, String strType, double dValue, boolean bDebugIn ) throws Exception {
    String strHost= GetIP(strHOST);
    String strMiddleWarePath= "sz/middleware.php/data/";
    String strSendUUID=""; strSendUUID= strSendUUID.format( "%s.json?",strUUID );
    bDebug= bDebugIn;
    String url=""; // url="http://host/sz/middleware.php/data/c3407300-9d4b-11e3-8eb8-99224c3b70e8.json?";
    String urlParameters="";

    url= url.format("http://%s/%s%s", strHost, strMiddleWarePath, strSendUUID );
    urlParameters= urlParameters.format("timestamp=%d&value=", lTime );

    boolean bRet= httpPost(url, urlParameters+dValue);
    return bRet;
  }

  public static boolean WriteTemp( long lTime, String strDescription, String str1WireID, String strUUID, String strType, double dValue, boolean bDebug ) {
    Connection conn = null;
    Statement stmt = null;
    boolean bRet= false;
    try {
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      if(bDebug) System.out.println("G3m1n1S3rv3r class: WriteTemp() started...");

      String strHost     = GetIP(strHOST);
      String strmySqlConn= "jdbc:mysql://"+strHost+":"+strPort+"/"+strDB;
      String strINSERT   = "INSERT INTO "+strDBTable+" VALUES ";
      if(bDebug) System.out.println("G3m1n1S3rv3r class: DEBUG:  "+strmySqlConn+","+strDBUser+","+strDBPass);
      conn = DriverManager.getConnection(strmySqlConn,strDBUser,strDBPass);
      stmt = conn.createStatement();
      String strSql="";
      strSql= strSql.format(strINSERT+"('%d','%s','%s','%s','%f')",
                                         lTime,
                                              strDescription,
                                                   str1WireID,
                                                        strType,
                                                             dValue );
      if(bDebug) System.out.println("G3m1n1S3rv3r class: "+ strSql );
      stmt.executeUpdate(strSql);
      bRet= true;
      //for( int i=1; i < 100000; ++i) {
        //String strSql="";
        //strSql= strSql.format("INSERT INTO %s VALUES (%d,%d,%d,%d)",strDBTable,i,i*2,i*3,i*4);
        //System.out.println( strSql );
        //stmt.executeUpdate(strSql);
      //}
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
    }
    return bRet;
  }
}
