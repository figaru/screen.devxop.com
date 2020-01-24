package com.devxop.screen.App;

/**
 * Created by admin on 18/05/2017.
 */

public class AppConfig {

    // Server generic api url
    public static String URL_API = "http://10.0.2.2:3000/";
    // Server app authenticate url
    public static String URL_REGISTER = URL_API + "/api/device/register";
    // Server app authenticate url
    public static String URL_LOGIN = URL_API + "/api/device/register";
    // Server user get details
    public static String URL_SYNC = URL_API + "/api/device/sync";
    public static String URL_UPDATE = URL_API + "/api/device/update";

}
