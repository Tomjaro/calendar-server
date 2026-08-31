package com.fiveelements.calendar.config;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ProductionConfigurationValidatorTest {
  @Test
  void acceptsExplicitHttpsOriginsAndGateway() {
    assertThatCode(
            () ->
                new ProductionConfigurationValidator(
                        "https://calendar.example.com,https://app.example.com",
                        "https://sms.example.com/send")
                    .run(null))
        .doesNotThrowAnyException();
  }

  @Test
  void rejectsWildcardOrInsecureCorsOrigins() {
    assertThatThrownBy(
            () ->
                new ProductionConfigurationValidator("*", "https://sms.example.com/send").run(null))
        .hasMessageContaining("explicit");
    assertThatThrownBy(
            () ->
                new ProductionConfigurationValidator(
                        "http://calendar.example.com", "https://sms.example.com/send")
                    .run(null))
        .hasMessageContaining("HTTPS");
  }

  @Test
  void rejectsInsecureSmsGateway() {
    assertThatThrownBy(
            () ->
                new ProductionConfigurationValidator(
                        "https://calendar.example.com", "http://sms.example.com/send")
                    .run(null))
        .hasMessageContaining("HTTPS");
  }
}
