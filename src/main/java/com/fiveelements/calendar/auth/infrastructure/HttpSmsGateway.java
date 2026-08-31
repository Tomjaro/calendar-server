package com.fiveelements.calendar.auth.infrastructure;

import com.fiveelements.calendar.auth.service.SmsGateway;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Profile("prod")
public class HttpSmsGateway implements SmsGateway {
  private final RestClient client;
  private final String template;

  public HttpSmsGateway(
      RestClient.Builder builder,
      @Value("${app.auth.sms.gateway-url}") String url,
      @Value("${app.auth.sms.gateway-token}") String token,
      @Value("${app.auth.sms.template-code:REGISTER}") String template) {
    if (url.isBlank() || token.isBlank())
      throw new IllegalStateException("SMS gateway URL and token are required");
    this.client = builder.baseUrl(url).defaultHeader("Authorization", "Bearer " + token).build();
    this.template = template;
  }

  public void sendRegisterCode(String phone, String code) {
    client
        .post()
        .contentType(MediaType.APPLICATION_JSON)
        .body(Map.of("phone", phone, "code", code, "scene", "REGISTER", "templateCode", template))
        .retrieve()
        .toBodilessEntity();
  }
}
