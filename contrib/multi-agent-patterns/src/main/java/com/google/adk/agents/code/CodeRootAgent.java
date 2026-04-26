package com.google.adk.agents.code;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.copyOf;

import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.LoopAgent;
import com.google.adk.agents.SequentialAgent;
import com.google.adk.agents.base.AgentConfig;
import com.google.adk.agents.base.AgentContext;
import com.google.adk.agents.base.LlmAgentProvider;
import com.google.adk.agents.base.tools.CodeExecutionToolProvider;
import com.google.adk.tools.AgentTool;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import jakarta.inject.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CodeRootAgent implements Provider<LlmAgent> {

  private static final String CRA_KEY = "code.root.agent";
  private static final String CGA_KEY = "code.generator.agent";
  private static final String CTA_KEY = "code.converter.agent";
  private static final String CCA_KEY = "code.critic.agent";
  private static final String CFA_KEY = "code.refactor.agent";
  private static final String CBA_KEY = "code.bundler.agent";

  public CodeRootAgent(Map<String, AgentConfig> configs) {
    this.configs = copyOf(checkNotNull(configs, "configs"));
  }

  private final ImmutableMap<String, AgentConfig> configs;

  @Override
  public LlmAgent get() {
    AgentContext ctx =
        AgentContext.builder() //
            .withConfig(configs.get(CRA_KEY)) //
            .withTools(agentTools()) //
            .build(); //
    return new LlmAgentProvider(ctx).get();
  }

  private ImmutableList<AgentTool> agentTools() {
    List<AgentTool> tools = new ArrayList<>();
    tools.add(AgentTool.create(generationWorkflow(configs)));
    tools.add(AgentTool.create(conversionWorkflow(configs)));
    tools.add(AgentTool.create(fullLoop(configs)));
    tools.add(AgentTool.create(new CommitAgent(configs).get()));
    return ImmutableList.copyOf(tools);
  }

  private SequentialAgent fullLoop(Map<String, AgentConfig> configs) {
    SequentialAgent generator = generationWorkflow(configs);
    SequentialAgent converter = conversionWorkflow(configs);
    LlmAgent bundler = new LlmAgentProvider(AgentContext.build(configs.get(CBA_KEY))).get();
    return SequentialAgent.builder() //
        .name("FullLoopWorkflow") //
        .description("Generates and converts code") //
        .subAgents(generator, converter, bundler) //
        .build();
  }

  private SequentialAgent conversionWorkflow(Map<String, AgentConfig> configs) {
    AgentTool tool = new CodeExecutionToolProvider(configs).get();
    LlmAgent convert = new LlmAgentProvider(AgentContext.build(configs.get(CTA_KEY), tool)).get();
    LlmAgent review = new LlmAgentProvider(AgentContext.build(configs.get(CCA_KEY), tool)).get();
    LlmAgent refactor = new LlmAgentProvider(AgentContext.build(configs.get(CFA_KEY), tool)).get();
    return SequentialAgent.builder() //
        .name("CodeConversionWorkflow") //
        .description("Converts code with review-refactor") //
        .subAgents(convert, review, refactor) //
        .build();
  }

  private SequentialAgent generationWorkflow(Map<String, AgentConfig> configs) {
    AgentTool tool = new CodeExecutionToolProvider(configs).get();
    AgentContext ctx = AgentContext.build(configs.get(CGA_KEY), tool);
    LlmAgent generator = new LlmAgentProvider(ctx).get();
    LoopAgent refiner = new CodeRefinementLoop(tool, configs).get();
    return SequentialAgent.builder() //
        .name("CodeGenerationWorkflow") //
        .description("Generates code with Refinement Loop") //
        .subAgents(generator, refiner) //
        .build();
  }
}
