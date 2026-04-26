package com.google.adk.agents.traveler;

import com.google.adk.agents.base.AgentConfigsProvider;
import com.google.adk.web.AdkWebServer;

/*
 * run command:
 * cd contrib/multi-agent-patterns
 * mvn compile exec:java -Dexec.mainClass=com.google.adk.agents.traveler.TravelerApp
 */
public class TravelerApp {

  public static void main(String[] args) throws Exception {
    AgentConfigsProvider configs = new AgentConfigsProvider();
    TravelerRootAgent root = new TravelerRootAgent(configs.get());
    AdkWebServer.start(root.get());
  }
}
