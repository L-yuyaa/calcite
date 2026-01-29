package org.apache.calcite.test;

import org.junit.jupiter.api.Test;
import java.sql.*;
import java.util.Properties;
import java.io.File;

public class DualIgniteClusterTest {
  // Ignite + Ignite 跨集群（双集群）JOIN
  @Test
  public void testIgniteDualClusterJoin() throws Exception {
    // 设置字符集（避免中文乱码）
    System.setProperty("saffron.default.charset", "UTF-8");

    Properties info = new Properties();
    String modelPath = new File(
        getClass().getResource("/dual-ignite.json").toURI()
    ).getAbsolutePath();
    info.put("model", modelPath);

    try (Connection conn = DriverManager.getConnection("jdbc:calcite:", info)) {
      String sql = "SELECT " +
          "c.CUST_NAME AS \"cust_name\", " +
          "c.RISK_LEVEL AS \"risk_level\", " +
          "p.SEC_CODE AS \"sec_code\", " +
          "p.MARKET_VAL AS \"market_val\" " +
          "FROM ignite1.CUSTOMER_INFO c " +
          "JOIN ignite2.POSITION_LIVE p " +
          "ON c.CUST_ID = p.CUST_ID " +
          "WHERE p.MARKET_VAL > 50000 " +
          "ORDER BY p.MARKET_VAL DESC";

      try (Statement stmt = conn.createStatement();
           ResultSet rs = stmt.executeQuery(sql)) {

        System.out.println("--- Ignite 跨集群 JOIN 查询结果 ---");
        while (rs.next()) {
          System.out.printf("客户: %s | 风险: %s | 证券: %s | 市值: %.2f%n",
              rs.getString("cust_name"),
              rs.getString("risk_level"),
              rs.getString("sec_code"),
              rs.getBigDecimal("market_val")
          );
        }
      }
    }
  }
}
