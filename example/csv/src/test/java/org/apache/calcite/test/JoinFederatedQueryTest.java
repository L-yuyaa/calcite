package org.apache.calcite.test;

import org.junit.jupiter.api.Test;
import java.sql.*;
import java.util.Properties;

public class JoinFederatedQueryTest {

    @Test
    public void testFullDataQuery() throws Exception {
        // 1. 强制设定字符集，防止中文乱码
        System.setProperty("saffron.default.charset", "UTF-8");

        Properties info = new Properties();
        // 获取配置文件路径
        String modelPath = JoinFederatedQueryTest.class.getResource("/dual-table-join.json").getPath();
        
        info.put("model", modelPath);
        info.put("caseSensitive", "false");
        info.put("defaultCharset", "UTF-8");

        try (Connection connection = DriverManager.getConnection("jdbc:calcite:", info)) {
            Statement statement = connection.createStatement();
            
            // 2. 执行关联查询：获取所有字段
            String sql = "SELECT * FROM \"adhoc\".\"v_full_match\" WHERE \"friend_name\" = 'zs'";
            
            System.out.println("--- 执行全字段关联查询 ---");

            try (ResultSet resultSet = statement.executeQuery(sql)) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();

                boolean found = false;
                while (resultSet.next()) {
                    found = true;
                    System.out.println("------------------------------------");
                    for (int i = 1; i <= columnCount; i++) {
                        String colName = metaData.getColumnName(i);
                        Object value = resultSet.getObject(i);
                        System.out.println(colName + " : " + value);
                    }
                }
                if (!found) System.out.println("未找到匹配数据");
            }
        }
    }
}