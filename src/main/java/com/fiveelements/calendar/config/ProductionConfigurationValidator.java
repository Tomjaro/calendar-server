package com.fiveelements.calendar.config;

import java.net.URI;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class ProductionConfigurationValidator implements ApplicationRunner {
  private final String origins, smsGateway;

  public ProductionConfigurationValidator(
      @Value("${app.cors.allowed-origins}") String origins,
      @Value("${app.auth.sms.gateway-url}") String smsGateway) {
    this.origins = origins;
    this.smsGateway = smsGateway;
  }

  public void run(ApplicationArguments args) {
    if (origins.isBlank() || origins.contains("*"))
      throw new IllegalStateException("Production CORS origins must be explicit");
    Arrays.stream(origins.split(","))
        .map(String::trim)
        .forEach(value -> requireHttps(value, "CORS origin"));
    requireHttps(smsGateway, "SMS gateway URL");
  }

  private void requireHttps(String value, String label) {
    try {
      URI uri = URI.create(value);
      if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null)
        throw new IllegalStateException(label + " must use HTTPS: " + value);
    } catch (IllegalArgumentException e) {
      throw new IllegalStateException(label + " is invalid: " + value, e);
    }
  }
}
