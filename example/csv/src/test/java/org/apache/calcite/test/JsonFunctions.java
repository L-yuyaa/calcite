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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

public class JsonFunctions {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // hasFriend 保持不变，它是标量函数，运行很稳定
    public static boolean hasFriend(String jsonStr, String targetName) {
        if (jsonStr == null || targetName == null) return false;
        try {
            JsonNode root = MAPPER.readTree(jsonStr);
            if (root.isArray()) {
                for (JsonNode node : root) {
                    if (node.has("name") && targetName.equals(node.get("name").asText())) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {}
        return false;
    }

    /**
     * 修改点 1: 将参数改为 Object 以兼容 byte[]
     * 修改点 2: 将返回类型改为 Iterable<String>。
     * Calcite 的 UNNEST 对 Iterable 的兼容性远好于 String[]。
     */
    public static List<String> extractNames(String jsonStr) {
    System.out.println("DEBUG: extractNames(String) called");
    return extractNamesInternal(jsonStr);
  }

/*
  public static List<String> extractNames(byte[] jsonBytes) {
    String jsonStr = new String(jsonBytes, java.nio.charset.StandardCharsets.UTF_8);
    System.out.println("DEBUG: extractNames(byte[]) called");
    return extractNamesInternal(jsonStr);
  }
*/
  private static List<String> extractNamesInternal(String jsonStr) {
    System.out.println("DEBUG: extractNames called with: " + jsonStr);
    List<String> names = new ArrayList<>();
    if (jsonStr == null) {
      return names;
    }
    try {
      JsonNode root = MAPPER.readTree(jsonStr);
      if (root.isArray()) {
        for (JsonNode node : root) {
          if (node.has("name")) {
            names.add(node.get("name").asText());
          }
        }
      }
    } catch (Exception e) {
      System.err.println("DEBUG Error: " + e.getMessage());
    }
    return names;
  }
}
