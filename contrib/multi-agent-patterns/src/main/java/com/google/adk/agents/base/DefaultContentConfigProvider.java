package com.google.adk.agents.base;

import static com.google.common.collect.ImmutableList.of;

import com.google.common.collect.ImmutableList;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.HttpRetryOptions;
import jakarta.inject.Provider;

// https://adk.dev/agents/models/google-gemini/#error-code-429-resource_exhausted

public class DefaultContentConfigProvider implements Provider<GenerateContentConfig> {

  private static final int MAX_RETRY = 3;
  private static final int MAX_TOKENS = 2000;
  private static final double TEMP = 0.1;
  private static final double JITTER = 1.0;
  private static final double INITIAL_DELAY = 1.0;
  private static final double DELAY_MULTIPLIER = 7.0;
  private static final ImmutableList<Integer> STATUS_CODES = of(429, 500, 503, 504);

  @Override
  public GenerateContentConfig get() {
    return GenerateContentConfig.builder() //
        .temperature(Double.valueOf(TEMP).floatValue()) //
        .maxOutputTokens(MAX_TOKENS) //
        .httpOptions(httpOptions()) //
        .build();
  }

  private HttpOptions httpOptions() {
    return HttpOptions.builder() //
        .retryOptions(retryOptions()) //
        .build(); //
  }

  private HttpRetryOptions retryOptions() {
    return HttpRetryOptions.builder()
        .initialDelay(INITIAL_DELAY) //
        .expBase(DELAY_MULTIPLIER) // delay multiplier
        .attempts(MAX_RETRY) // max retry attempts
        .jitter(JITTER) //
        .httpStatusCodes(STATUS_CODES) // retry on these HTTP errors
        .build(); //
  }
}
