/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.calcite.test;

import org.junit.jupiter.api.Test;
import java.sql.*;
import java.util.Properties;
import java.io.File;

public class StockFederatedTest {
    @Test
    public void testStockJoin() throws Exception {
        // 1. 设置字符集，解决证券数据中的中文名称显示
        System.setProperty("saffron.default.charset", "UTF-8");

        Properties info = new Properties();
        // 使用 toURI() 转换路径，避免 Windows 下 file: 前缀问题
        String modelPath = new File(StockFederatedTest.class.getResource("/stock-federated.json").toURI()).getAbsolutePath();

        info.put("model", modelPath);
        info.put("caseSensitive", "false"); // 忽略大小写差异

        try (Connection connection = DriverManager.getConnection("jdbc:calcite:", info)) {
            // 2. 编写联邦查询 SQL：筛选市值大于 50000 的高价值客户
            String sql = "SELECT * FROM \"adhoc\".\"V_STOCK_REPORT\" WHERE \"market_val\" > 50000";

            Statement statement = connection.createStatement();
            System.out.println("--- 正在执行证券数据联邦查询 (MySQL 客户 + Ignite 持仓) ---");

            try (ResultSet resultSet = statement.executeQuery(sql)) {
                boolean hasData = false;
                while (resultSet.next()) {
                    hasData = true;
                    System.out.println("------------------------------------");
                    System.out.println("客户ID:   " + resultSet.getInt("cust_id"));
                    System.out.println("客户姓名: " + resultSet.getString("cust_name"));
                    System.out.println("风险等级: " + resultSet.getString("risk_level"));
                    System.out.println("证券代码: " + resultSet.getString("sec_code"));
                    System.out.println("实时市值: " + resultSet.getBigDecimal("market_val"));
                }
                if (!hasData) System.out.println("未找到市值大于 50,000 的记录");
            }
        }
    }
}
