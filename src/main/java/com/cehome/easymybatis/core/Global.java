package com.cehome.easymybatis.core;

public class Global {
    public static String SQL_UPDATE = "<script>\r\n update {} <set>{}</set> \r\n {} \r\n </script> ";
    public static String SQL_SELECT = "<script>\r\n select {} from {} {}\r\n</script>";
    public static String SQL_DELETE = "<script>\r\n delete {} from {} {}\r\n</script>";
    public static int SQL_TYPE_INSERT = 1;
    public static int SQL_TYPE_UPDATE = 2;
    public static int SQL_TYPE_DELETE = 3;
    public static int SQL_TYPE_SELECT = 4;
}
