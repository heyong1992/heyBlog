package com.blog.utils.autoGeneration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class AutoGenerationJavaCode {


    private String url;
    private String name;
    private String passWord;
    private String driver;
    private String sql;
    private String tableName;
    private String templateDir;
    private String[] autoGeneratedFile = new String[6];
    private static String[][] fileNameArray = new String[6][2];
    private String classType;

    static {
        fileNameArray[0][0] = "entityTemplate.ftl";
        fileNameArray[0][1] = ".java";

        fileNameArray[1][0] = "serviceTemplate.ftl";
        fileNameArray[1][1] = "Service.java";

        fileNameArray[2][0] = "daoTemplate.ftl";
        fileNameArray[2][1] = "Repository.java";

        fileNameArray[3][0] = "controllerTemplate.ftl";
        fileNameArray[3][1] = "Controller.java";

        fileNameArray[4][0] = "list.ftl";
        fileNameArray[4][1] = "List.jsp";

        fileNameArray[5][0] = "Detail.ftl";
        fileNameArray[5][1] = "Detail.jsp";
    }

    public AutoGenerationJavaCode(String url, String name, String passWord, String driver, String tableName,
                                  String autoGeneratedFile, String templateDir, String classType) {
        this.url = url;
        this.name = name;
        this.passWord = passWord;
        this.driver = driver;
        this.sql = "select * from " + tableName;
        this.tableName = tableName;
        this.templateDir = templateDir;
        this.autoGeneratedFile[0] = autoGeneratedFile + "\\domain";
        this.autoGeneratedFile[1] = autoGeneratedFile + "\\service";
        this.autoGeneratedFile[2] = autoGeneratedFile + "\\repository";
        this.autoGeneratedFile[3] = autoGeneratedFile + "\\controller";
        this.autoGeneratedFile[4] ="E:\\heywork\\blog\\src\\main\\webapp\\WEB-INF\\jsp";
        this.autoGeneratedFile[5] ="E:\\heywork\\blog\\src\\main\\webapp\\WEB-INF\\jsp";
        this.classType = classType;
    }


    public void autoGenerationJavaCode() throws IOException, TemplateException, ClassNotFoundException,
            SQLException {
        Configuration cfg = new Configuration();
        cfg.setDefaultEncoding("utf-8");

        //String className = dealTableName();
        String className=underline2Camel(tableName,true);
        String fileName = dealClassName(className);
        //Map<String, Object> columnMap = getColumn();
        getColunm(tableName);
        Map<String, String> columnMap = getJavaType(tableName);
        List<ColumnDto> columnList=getColunm(tableName);
        //设置模板文件路径
        cfg.setDirectoryForTemplateLoading(new File(templateDir));

        Map<String, Object> rootMap = new HashMap<String, Object>();
        rootMap.put("className", className);
        rootMap.put("columnMap", columnMap);
        rootMap.put("columnList", columnList);
        rootMap.put("tableName", tableName);
        int j=0;
        for (int i = 0; i < fileNameArray.length; i++) {
            if ("entity".equals(classType)) {
                j=0;
                i=fileNameArray.length;
            }
            if ("service".equals(classType) ) {
                j=1;
                i=fileNameArray.length;
            }
            if ("dao".equals(classType)) {
                j=2;
                i=fileNameArray.length;
            }
            if ("controller".equals(classType)) {
                j=3;
                i=fileNameArray.length;
            }
            if ("jspList".equals(classType)) {
                j=4;
                i=fileNameArray.length;
                autoGeneratedFile[4]+="\\"+className;
                File dir = new File(autoGeneratedFile[4] );
                //检查目录是否存在，不存在则创建
                if (!dir.exists()) {
                    dir.mkdir();
                }
            }
            if ("jspDetail".equals(classType)) {
                j=5;
                i=fileNameArray.length;
                autoGeneratedFile[5]+="\\"+className;
                File dir = new File(autoGeneratedFile[5]);
                //检查目录是否存在，不存在则创建
                if (!dir.exists()) {
                    dir.mkdir();
                }
            }
            if ("all".equals(classType)) {
                j=i;
            }

            Template temp = cfg.getTemplate(fileNameArray[j][0]);

            File docFile = new File(autoGeneratedFile[j] + "\\" + fileName + fileNameArray[j][1]);

            Writer docout = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(docFile)));
            //输出文件
            temp.process(rootMap, docout);
        }
        System.out.println("==============文件生产成功===============");

    }


    //获取数据库表字段名放入map中
    public Map<String, Object> getColumn() throws ClassNotFoundException, SQLException {
        Connection conn;
        PreparedStatement pStemt = null;
        Class.forName(driver);
        conn = DriverManager.getConnection(url, name, passWord);
        pStemt = conn.prepareStatement(sql);
        ResultSetMetaData rsmd = pStemt.getMetaData();

        Map<String, Object> columnMap = new HashMap<String, Object>();
        int size = rsmd.getColumnCount();
        for (int i = 0; i < size; i++) {
            String columnName = rsmd.getColumnName(i + 1).toLowerCase();
            columnName = dealColumnName(columnName);

            columnMap.put(columnName, columnName);
        }
        conn.close();
        return columnMap;
    }

    /**
     * 获取表所有字段对应的java数据类型
     *
     * @param tableName
     * @return
     */
    public Map<String, String> getJavaType(String tableName) throws ClassNotFoundException, SQLException {
        //  获取字段数
        Map<String, String> columnJavaTypeMap = new HashMap<String, String>();

        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(url, name, passWord);
            st = conn.createStatement();
            String sql = "select * from " + tableName + " where 1 != 1 ";
            rs = st.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();

            int columns = rsmd.getColumnCount();
            for (int i = 1; i <= columns; i++) {
                //获取字段名
                String columnName = rsmd.getColumnName(i).toLowerCase();
                String columnClassName = rsmd.getColumnClassName(i);
                if (columnClassName.equals("[B")) {
                    columnClassName = "byte[]";
                }
                columnJavaTypeMap.put(columnName, columnClassName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            conn.close();
        }
        return columnJavaTypeMap;
    }

    /**
     * 获取表所有字段对应的java数据类型
     *
     * @param tableName
     * @return
     */
    public List<ColumnDto> getColunm(String tableName) throws ClassNotFoundException, SQLException {
        List<ColumnDto> list=new ArrayList<>();
        //  获取字段数
        Map<String, String> columnJavaTypeMap = new HashMap<String, String>();
        Connection conn = null;
        //Statement st = null;
        ResultSet rs = null;
        DatabaseMetaData dbmd = null;

        try {
            conn = DriverManager.getConnection(url, name, passWord);
            dbmd = conn.getMetaData();
            rs = dbmd.getColumns(null, "%", tableName, "%");
            while (rs.next()) {
                System.out.println(rs.getString("COLUMN_NAME") + "----" + rs.getString("REMARKS") + "----" + rs.getString("TYPE_NAME") + "----" + rs.getString("COLUMN_SIZE") + "----" );
                String column_name = rs.getString("COLUMN_NAME");
                String charAfterLine = String.valueOf(column_name.charAt((column_name.indexOf("_") + 1)));
                String convertedChar = charAfterLine.toUpperCase();
                column_name = column_name.replace("_" + charAfterLine, convertedChar);
                String column_type = rs.getString("TYPE_NAME");
                String column_length = rs.getString("COLUMN_SIZE");
                String comments = rs.getString("REMARKS");

                // 需要跳过的字段
                if("id".equals(column_name) || "yyy".equals(column_name) || "zzz".equals(column_name)){
                    continue;
                }

                ColumnDto table = new ColumnDto();
                table.setTable_name(tableName);
               // table.setTable_desc(tableDesc);

                table.setColumn_name(column_name);
                //table.setColumn_name_upperCaseFirstOne(ToolString.toUpperCaseFirstOne(column_name));

                table.setColumn_type(convertColumnType(column_type));
                table.setColumn_length(column_length);
                table.setColumn_desc(comments);

                //table.setColumn_className(columnJavaTypeMap.get(column_name.toLowerCase()));

                list.add(table);

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            conn.close();
        }
        return list;
    }

    public String convertColumnType(String columnType){
        if("varchar".equalsIgnoreCase(columnType)){
            return "String";
        }else if("int".equalsIgnoreCase(columnType)){
            return "Integer";
        }else if("date".equalsIgnoreCase(columnType)){
            return "Date";
        }else if("timestamp".equalsIgnoreCase(columnType)){
            return "Timestamp";
        }else{
            return "String";
        }
    }


   /* @Override
    public List<ColumnDto> getColunm(String tableName) {
        String dbUser = getDataBase().getUserName();
        String name = getDataBase().getName();

        // 1.查询表和字段描述信息
        String tcSql = ToolSqlXml.getSqlMy("platform.db2.getTableComments");
        String tableDesc = Db.use(name).findFirst(tcSql, dbUser, tableName).getStr("REMARKS");

        // 2.查询表字段信息
        String ccSql = ToolSqlXml.getSqlMy("platform.db2.getColumnComments");
        List<Record> listColumnComments = Db.use(name).find(ccSql, dbUser, tableName);

        // 3.查询表字段对应的所有java数据类型
        Map<String, String> columnJavaTypeMap = getJavaType(tableName);

        List<ColumnDto> list = new ArrayList<ColumnDto>();

        // 4.循环合并表字段详细信息
        for (Record record : listColumnComments) {
            String column_name = record.getStr("COLNAME");
            String column_type = record.getStr("TYPENAME");
            String column_length = String.valueOf(record.getNumber("LENGTH"));
            String comments = record.getStr("REMARKS");

            // 需要跳过的字段
            if("xxx".equals(column_name) || "yyy".equals(column_name) || "zzz".equals(column_name)){
                continue;
            }

            ColumnDto table = new ColumnDto();
            table.setTable_name(tableName);
            table.setTable_desc(tableDesc);

            table.setColumn_name(column_name);
            table.setColumn_name_upperCaseFirstOne(ToolString.toUpperCaseFirstOne(column_name));

            table.setColumn_type(column_type);
            table.setColumn_length(column_length);
            table.setColumn_desc(comments);

            table.setColumn_className(columnJavaTypeMap.get(column_name.toLowerCase()));

            list.add(table);
        }

        return list;
    }
*/

    /*//将表名转换为DMO的字段名，比如 operate_type 转换后为 operateType
    private String dealColumnName(ResultSetMetaData rsmd, int i) throws SQLException {
        String columnName = rsmd.getColumnName(i + 1).toLowerCase();
        String charAfterLine = String.valueOf(columnName.charAt((columnName.indexOf("_") + 1)));
        String convertedChar = charAfterLine.toUpperCase();
        columnName = columnName.replace("_" + charAfterLine, convertedChar);
        return columnName;
    }*/

    //将表名转换为DMO的字段名，比如 operate_type 转换后为 operateType
    private String dealColumnName(String columnName) throws SQLException {
        //String columnName = rsmd.getColumnName(i + 1).toLowerCase();
        String charAfterLine = String.valueOf(columnName.charAt((columnName.indexOf("_") + 1)));
        String convertedChar = charAfterLine.toUpperCase();
        columnName = columnName.replace("_" + charAfterLine, convertedChar);
        return columnName;
    }

    //将表名转换为类型类名 比如 t_operate_log 转换后为 operateLog ,类名首字母应为大写，这里在freemarker的模板里直接转换
    private String dealTableName() {
        String className = tableName.toLowerCase().substring(tableName.indexOf("_") + 1);
        String charAfterLine = String.valueOf(className.charAt((className.indexOf("_") + 1)));
        String convertedChar = charAfterLine.toUpperCase();
        className = className.replace("_" + charAfterLine, convertedChar);
        return className;
    }

    //将类名转换为文件名，java公共类名与其文件名应该相同，这里将首字母转换为大写 如operateLog 转换后为 OperateLog
    private String dealClassName(String className) {
        String first = className.substring(0, 1).toUpperCase();
        String rest = className.substring(1, className.length());
        String fileName = new StringBuffer(first).append(rest).toString();
        return fileName;
    }


    /**
     * 下划线转驼峰法
     * @param line 源字符串
     * @param smallCamel 大小驼峰,是否为小驼峰
     * @return 转换后的字符串
     */
    public static String underline2Camel(String line,boolean smallCamel){
        if(line==null||"".equals(line)){
            return "";
        }
        StringBuffer sb=new StringBuffer();
        Pattern pattern=Pattern.compile("([A-Za-z\\d]+)(_)?");
        Matcher matcher=pattern.matcher(line);
        while(matcher.find()){
            String word=matcher.group();
            sb.append(smallCamel&&matcher.start()==0?Character.toLowerCase(word.charAt(0)):Character.toUpperCase(word.charAt(0)));
            int index=word.lastIndexOf('_');
            if(index>0){
                sb.append(word.substring(1, index).toLowerCase());
            }else{
                sb.append(word.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }

}