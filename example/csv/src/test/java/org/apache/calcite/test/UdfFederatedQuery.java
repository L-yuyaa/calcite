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
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
import java.io.File;

public class UdfFederatedQuery {

  @Test
  public void testUdfPrecisionQuery() throws Exception {
    Properties info = new Properties();
    String modelPath = new File(UdfFederatedQuery.class.getResource("/udf-model.json").toURI()).getAbsolutePath();
    info.put("model", modelPath);
    info.put("caseSensitive", "false");

    try (Connection connection = DriverManager.getConnection("jdbc:calcite:", info)) {
      Statement statement = connection.createStatement();

      String sql = "SELECT * FROM \"adhoc\".\"USERS_FRIENDS_VIEW\" WHERE \"adhoc\".\"HAS_FRIEND\"(\"friends_str\", 'zs')";
      System.out.println("Executing UDF Precision Query: " + sql);

      try (ResultSet resultSet = statement.executeQuery(sql)) {
        System.out.println("--- Results (testUdfPrecisionQuery) ---");
        while (resultSet.next()) {
          System.out.println("User ID: " + resultSet.getInt("id") +
              ", Name: " + resultSet.getString("name") +
              ", Friends: " + resultSet.getString("friends_str"));
        }
      }
    }
  }

@Test
  public void testUdfUnnestQuery() throws Exception {
    System.setProperty("calcite.debug", "true");

    Properties info = new Properties();
    String modelPath = new File(UdfFederatedQuery.class.getResource("/udf-model.json").toURI()).getAbsolutePath();
    info.put("model", modelPath);
    info.put("caseSensitive", "false");

    try (Connection connection = DriverManager.getConnection("jdbc:calcite:", info)) {
        Statement statement = connection.createStatement();

        // 终极 SQL：在子查询中先完成 CAST，确保类型在传递给 UDF 前是纯粹的 String
        // 然后使用 LATERAL TABLE 展开
        String sql = "SELECT v.\"name\" AS \"user_name\", f.\"friend_name\" " +
                     "FROM (SELECT \"name\", CAST(\"friends_str\" AS VARCHAR) as s FROM \"adhoc\".\"USERS_FRIENDS_VIEW\") v, " +
                     "LATERAL TABLE(\"adhoc\".\"EXTRACT_NAMES\"(v.s)) AS f(\"friend_name\")";

        System.out.println("Executing Protected Lateral Query: " + sql);

        try (ResultSet resultSet = statement.executeQuery(sql)) {
            System.out.println("--- Results (testUdfUnnestQuery) ---");
            int count = 0;
            while (resultSet.next()) {
                count++;
                System.out.println("User: " + resultSet.getString("user_name") +
                                   ", Friend: " + resultSet.getString("friend_name"));
            }
            System.out.println("Total rows: " + count);
            assertEquals(2, count, "Expected 2 rows from UNNEST operation");
        }
    }
}

@Test
  public void testUdfUnnestIsolation() throws Exception {
    Properties info = new Properties();
    String modelPath = new File(UdfFederatedQuery.class.getResource("/udf-model.json").toURI()).getAbsolutePath();
    info.put("model", modelPath);

    try (Connection connection = DriverManager.getConnection("jdbc:calcite:", info)) {
      Statement statement = connection.createStatement();
      // 使用 TABLE() 包装函数调用
      String sql = "SELECT * FROM TABLE(\"adhoc\".\"EXTRACT_NAMES\"('[{\"name\": \"test1\"}, {\"name\": \"test2\"}]')) AS t(n)";
      System.out.println("Executing UDF Isolation Query: " + sql);

      try (ResultSet resultSet = statement.executeQuery(sql)) {
         System.out.println("--- Results (testUdfUnnestIsolation) ---");
         while(resultSet.next()) {
             System.out.println("Name: " + resultSet.getString("n"));
         }
      }
    }
  }
}
