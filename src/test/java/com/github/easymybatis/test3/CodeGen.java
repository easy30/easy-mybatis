package com.github.easymybatis.test3;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeGen {
    static String author =System.getProperty("user.name");
    static String date=getCurrentFormatDateWeek();
    static String table;
    static String serviceId;
    static String ip ;
    static String port ;
    static String user ;
    static String password;
    static String db ;
    static String url ;
    static String outputBasePath;

    static String basePackage;
    static String poPackage;
    static String dtoPackage;
    static String reqDtoPackage;
    static String mapperPackage;
    static String apiPackage;
    static String implPackage;
    static String testPackage;
    static String modelVar;
    static String modelName;

    // 类 注释
    private final static String CLASS_NOTES = "\r\n/**\r\n *\r\n * @author " + author + "\r\n * @since 1.0.0\r\n */\r\n";

    /**
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();

        // 服务名
        serviceId = "equipment";
        // 数据库表名，多个逗号分隔
        String tableNames = "equipment_sms2";
        // 导出文件的路径
        outputBasePath = "/Users/apple/workspace4/spring_cloud/equipment/equipment-service/src/test/java";


        ip = "192.168.0.23";
        port = "3306";
        user = "root";
        password = "asdf1234!";
        db = "ershouji";


        basePackage = "com.cehome.cloud." + serviceId + "";

        // 实现服务的包名路径
        String apiPackageName = "com.cehome.cloud." + serviceId + "";

        if(!outputBasePath.endsWith("/") && !outputBasePath.endsWith("\\"))outputBasePath+="/";
        //outputBasePath+=serviceId+"/";
        String[] array = tableNames.split(",");
        for (String tableName : array) {
            table=tableName;
            // build model name
            modelName = "";
            String[] tableNameWords = tableName.toLowerCase().split("_");

            for (String word : tableNameWords) {
                modelName += word.substring(0, 1).toUpperCase() + word.substring(1);
            }
            modelVar=  modelName.substring(0, 1).toLowerCase() + modelName.substring(1);
            // get columns
            Map<String, Object> map = getDbInfo(tableName);
            List<JavaColumn> javaList = (List<JavaColumn>) map.get("javaList");
            List<DbColumn> dbList = (List<DbColumn>) map.get("dbList");

            //---------------- model所在包名，实体名

            //String filePackage;
            String modelPackageName = basePackage + ".model";

            // output po file
            poPackage=modelPackageName + ".po";
            dtoPackage=modelPackageName + ".dto";
            reqDtoPackage=modelPackageName + ".req";
            apiPackage =basePackage + ".api";
            implPackage =basePackage + ".service";
            testPackage=basePackage + ".test";
            mapperPackage=basePackage + ".easymapper";

            List<String> poLines = writePo(javaList);
            outputFile(poPackage,modelName + ".java", poLines);

            // output dto file

            List<String> dtoLines = writeDto();
            outputFile(dtoPackage, modelName + "Dto.java", dtoLines);

            // output reqDto file

            List<String> reqDtoLines = writeReqDto();
            outputFile(reqDtoPackage, modelName + "ReqDto.java", reqDtoLines);

            // output servcer service

            List<String> apiServiceLine = writeServiceAPI();
            outputFile(apiPackage, modelName + "Service.java", apiServiceLine);
            // -----------------------api End--------------------------------------------
            // output serviceImpl

            List<String> serviceLine = writeServiceImpl();
            outputFile(implPackage,modelName + "ServiceImpl.java", serviceLine);


            List<String> list = writeTest();
            outputFile(testPackage,modelName + "Test.java", list);

            // output mapper

            List<String> mapperLines = writeMapper(mapperPackage, modelPackageName,modelName);
            outputFile(mapperPackage, modelName + "Mapper.java", mapperLines);

        }
        System.out.println("生成文件完成，共花费:" + (System.currentTimeMillis() - start));

    }

    /**
     * 得到当前系统日期和星期并转化为字符串：系统默认显示格式
     * @return 系统默认日期和星期显示格式 15位字符串
     */
    public static String getCurrentFormatDateWeek() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return dateFormat.format(date);
    }

    public static Map<String, Object> getDbInfo(String tableName) {
        Map<String, Object> map = new HashMap<String, Object>();
        List<JavaColumn> javaList = new ArrayList<JavaColumn>();
        List<DbColumn> dbList = new ArrayList<DbColumn>();
        url = "jdbc:mysql://" + ip + ":" + port + "/" + db + "?user=" + user + "&password=" + password
                + "&useUnicode=true&characterEncoding=UTF8";
        Connection conn = null;
        String sql = "select column_name, data_type ,column_comment from information_schema.columns t where table_name='" + tableName
                + "' and table_schema = '" + db + "'";
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String columnName = rs.getString("column_name");
                String dataType = rs.getString("data_type");
                String remark = rs.getString("column_comment");
                javaList.add(new JavaColumn(columnName, dataType, remark));
                dbList.add(new DbColumn(columnName, dataType, remark));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        map.put("javaList", javaList);
        map.put("dbList", dbList);

        return map;
    }

    public static List<String> writePo(List<JavaColumn> columns) {
        List<String> fileLines = new ArrayList<String>();
        fileLines.add("package " + poPackage + ";\n\n");
        fileLines.add(CLASS_NOTES);
        fileLines.add("import java.io.Serializable;\n");
        fileLines.add("import javax.persistence.Id;\n");
        fileLines.add("import javax.persistence.Table;\n");
        fileLines.add("import java.util.Date;\n");

        fileLines.add("import com.cehome.easymybatis.annotation.ColumnDefault;\n\n");
        fileLines.add("@Table(name = \""+table+"\")\n\n");
        fileLines.add("public class " + modelName + "  implements Serializable {\n\n");

        if (columns != null && columns.size() > 0) {

            // create fields
            for (JavaColumn c : columns) {
                fileLines.add("\t/**" + c.getRemark() + "**/ \r\n");
                if(c.getName().equals("id")){
                    fileLines.add("\t@Id\n");
                }
                fileLines.add("\tprivate " + c.getType() + " " + c.getName() + ";\n\n");
            }

            // create getters and setters
            for (JavaColumn c : columns) {
                fileLines.add("\tpublic " + c.getType() + " get" + c.getName().substring(0, 1).toUpperCase() + c.getName().substring(1) + "() {\n");
                fileLines.add("\t\treturn this." + c.getName() + ";\n");
                fileLines.add("\t}\n\n");
                fileLines.add("\tpublic void set" + c.getName().substring(0, 1).toUpperCase() + c.getName().substring(1) + "(" + c.getType() + " "
                        + c.getName() + ") {\n");
                fileLines.add("\t\tthis." + c.getName() + " = " + c.getName() + ";\n");
                fileLines.add("\t}\n\n");
            }
        }

        fileLines.add("}");
        return fileLines;
    }

    public static List<String> writeDto() {
        List<String> fileLines = new ArrayList<String>();
        fileLines.add("package " + dtoPackage + ";\n\n");
        fileLines.add("import java.io.Serializable;\n\n");
        fileLines.add("import " + poPackage + "." + modelName + ";\n\n");
        fileLines.add(CLASS_NOTES);

        fileLines.add("public class " + modelName + "Dto extends " + modelName + "  implements Serializable {\n\n");
        fileLines.add("}");
        return fileLines;
    }

    public static List<String> writeReqDto() {
        List<String> fileLines = new ArrayList<String>();
        fileLines.add("package " + reqDtoPackage + ";\n\n");
        fileLines.add("import java.io.Serializable;\n\n");
        fileLines.add("import " + poPackage + "." + modelName + ";\n\n");
        fileLines.add(CLASS_NOTES);
        fileLines.add("public class " + modelName + "ReqDto extends " + modelName + "  implements Serializable {\n\n");

        fileLines.add("}");
        return fileLines;
    }

    public static List<String> writeServiceAPI() {
        List<String> fileLines = new ArrayList<String>();
        String text=read("/codeGen/Service.txt");
        text=replaceText(text);
        fileLines.add(text);
        return fileLines;

    }

    // -----------------------api End--------------------------------------------
    public static List<String> writeServiceImpl() {
        List<String> fileLines = new ArrayList<String>();
        String text=read("/codeGen/ServiceImpl.txt");
        text=replaceText(text);
        fileLines.add(text);
        return fileLines;
    }

    public static List<String> writeTest() {
        List<String> fileLines = new ArrayList<String>();
        String text=read("/codeGen/Test.txt");
        text=replaceText(text);
        fileLines.add(text);
        return fileLines;
    }

    private static String replaceText(String text){
       return text.replace("${modelVar}",modelVar).replace("${modelName}",modelName)
                .replace("${author}", author)
                .replace("${date}",date)
               .replace("${service}",serviceId.substring(0,1).toUpperCase()+serviceId.substring(1))
                .replace("${package}",basePackage)
                .replace("${poPackage}",poPackage)
                .replace("${dtoPackage}",dtoPackage)
                .replace("${reqDtoPackage}",reqDtoPackage)
                .replace("${mapperPackage}",mapperPackage)
                .replace("${apiPackage}", apiPackage)
                .replace("${implPackage}", implPackage)
                .replace("${testPackage}",testPackage)

        ;
    }

    public static List<String> writeMapper(String apiPackageName, String modelPackageName, String fileName) {
        List<String> fileLines = new ArrayList<String>();
        fileLines.add("package " + apiPackageName + ";\n\n");
        fileLines.add("import com.cehome.easymybatis.Mapper;\n");
        fileLines.add("import " + modelPackageName + ".po." + fileName + ";\n");
        fileLines.add("import " + modelPackageName + ".dto." + fileName + "Dto;\n");
        fileLines.add("import " + modelPackageName + ".req." + fileName + "ReqDto;\n");
        fileLines.add(CLASS_NOTES);
        fileLines.add("public interface " + fileName + "Mapper" + " extends Mapper<"+fileName+","+fileName+"Dto> {\n\n");
        fileLines.add("}\n\n");
        return fileLines;
    }


    public static void outputFile(String pkg,String name, List<String> fileLines) throws IOException {
        String fileName=outputBasePath+pkg.replace('.','/')+"/"+name;
        File file = new File(fileName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            file.createNewFile();
        }
        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
        for (String line : fileLines) {
            writer.write(line);
        }
        writer.flush();
        writer.close();
    }

    public static String read(String resource) {
        InputStream is = CodeGen.class.getResourceAsStream(resource);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        int b = -1;
        try {
            while ((b = is.read()) != -1) {

                os.write(b);
            }

            is.close();


            return new String(os.toByteArray(), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

class DbColumn {

    private String name;

    private String type;
    private String remark;

    public DbColumn(String name, String type, String remark) {
        this.name = name;
        this.type = type;
        this.remark = remark;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}

class JavaColumn {

    private String name;

    private String type;

    private String remark;

    public JavaColumn(String name, String type, String remark) {
        String[] words = name.split("_");
        for (int i = 0; i < words.length; i++) {
            if (i == 0) {
                name = words[i];
            } else {
                name += words[i].substring(0, 1).toUpperCase() + words[i].substring(1);
            }
        }

        this.name = name;
        this.remark = remark;

        if ("INT".equalsIgnoreCase(type)) {
            this.type = "Integer";
        } else if ("INT UNSIGNED".equalsIgnoreCase(type)) {
            this.type = "Integer";
        } else if ("BIGINT".equalsIgnoreCase(type)) {
            this.type = "Long";
        } else if ("SMALLINT".equalsIgnoreCase(type)) {
            this.type = "short";
        } else if ("FLOAT".equalsIgnoreCase(type)) {
            this.type = "float";
        } else if ("DECIMAL".equalsIgnoreCase(type)) {
            this.type = "BigDecimal";
        } else if ("VARCHAR".equalsIgnoreCase(type)) {
            this.type = "String";
        } else if (type.contains("TEXT")) {
            this.type = "String";
        } else if (type.contains("BLOB")) {
            this.type = "byte[]";
        } else if ("datetime".equalsIgnoreCase(type)) {
            this.type = "Date";
        } else if ("double".equalsIgnoreCase(type)) {
            this.type = "Double";
        } else if ("char".equalsIgnoreCase(type)) {
            this.type = "Integer";
        } else if ("text".equalsIgnoreCase(type)) {
            this.type = "String";
        } else if ("tinyint".equalsIgnoreCase(type)) {
            this.type = "Integer";
        }

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}