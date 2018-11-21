package com.xtc.lint.rules;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * 加载Properties配置文件到Map中
 * </p>
 * created by OuyangPeng at 2017/9/7 16:28
 */
public final class LoadPropertiesFile {

    private static Properties props;

    /**
     * 加载sql.properties文件,并获取其中的内容(key-value)
     */
    public static void loadPropertiesFile() {
        System.out.println("****************  开始加载properties文件内容 ****************");
        // 属性列表
        props = new Properties();
        InputStream is = null;
        try {
            // 获取资源文件
            is = LoadPropertiesFile.class.getClassLoader().getResourceAsStream("lint_user_map.properties");
            // 从输入流中读取属性列表
            if (is != null) {
                props.load(is);
                for (Entry<Object, Object> entry : props.entrySet()) {
                    String key = (String) entry.getKey();
                    String value = (String) entry.getValue();
//                  System.out.println("key = " + key + " , value = " + value);
                    Constants.loadPropertiesMap.put(key, value);
                }
            } else {
                System.err.println("InputStream is null");
            }
        } catch (FileNotFoundException e){
            System.err.println("lint_user_map.properties文件未找到");
        } catch (IOException e) {
            System.err.println("load file faile. e = " + e.toString());
        } finally {
            try {
                if (null != is){
                    is.close();
                }
            } catch (IOException e) {
                System.err.println("lint_user_map.properties 文件流关闭失败 ：e =" + e.getMessage());
            }
        }
        System.out.println("****************  加载properties文件内容完成 ****************");
    }

    public static String getPropery(String key){
        if(null == props) {
            loadPropertiesFile();
        }
        return props.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        if(null == props) {
            loadPropertiesFile();
        }
        return props.getProperty(key, defaultValue);
    }
}