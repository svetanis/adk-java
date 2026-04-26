package com.google.adk.agents.code;

import static com.google.common.collect.ImmutableMap.copyOf;
import static java.lang.String.format;

import com.google.adk.tools.mcp.StreamableHttpServerParameters;
import com.google.common.collect.ImmutableMap;
import jakarta.inject.Provider;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ServerParamsProvider implements Provider<StreamableHttpServerParameters> {

  private static final String API_KEY = "Bearer %s";
  private static final String CGT_KEY = "COPILOT_GITHUB_TOKEN";
  private static final String URL = "https://api.githubcopilot.com/mcp";

  @Override
  public StreamableHttpServerParameters get() {
    String apiKey = apiKey(CGT_KEY);
    return StreamableHttpServerParameters //
        .builder() //
        .url(URL) //
        .headers(headers(apiKey)) //
        .build();
  }

  private ImmutableMap<String, String> headers(String apiKey) {
    Map<String, String> map = new HashMap<>();
    map.put("Authorization", apiKey);
    map.put("X-MCP-Toolsets", "all");
    map.put("X-MCP-Readonly", "false");
    //    map.put("User-Agent", "ADK-Java-Client/1.0");
    //    map.put("Accept", "text/event-stream");
    //    map.put("Editor-Version", "vscode/1.90.0");
    //    map.put("User-Agent", "github-mcp-server");
    return copyOf(map);
  }

  private String apiKey(String key) {
    String apiKey =
        Optional.ofNullable(System.getenv(key))
            .orElseThrow(() -> new IllegalStateException(format("%s is not set", key)));
    return format(API_KEY, apiKey);
  }
}
