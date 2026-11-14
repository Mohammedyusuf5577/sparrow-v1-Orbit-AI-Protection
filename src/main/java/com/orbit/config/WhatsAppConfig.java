package com.orbit.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Getter
@Configuration
public class WhatsAppConfig {

    @Value("${app.whatsapp.token:${WHATSAPP_TOKEN:}}")
    private String token;

    @Value("${app.whatsapp.phone-number-id:${WHATSAPP_PHONE_NUMBER_ID:}}")
    private String phoneNumberId;

    @Value("${app.whatsapp.api-version:${WHATSAPP_API_VERSION:v19.0}}")
    private String apiVersion;

    @Value("${app.whatsapp.template-name:${WHATSAPP_TEMPLATE_NAME:project_assignment}}")
    private String templateName;

    @Value("${app.whatsapp.template-lang:${WHATSAPP_TEMPLATE_LANG:en}}")
    private String templateLang;

    public boolean isConfigured() {
        return token != null && !token.trim().isEmpty() &&
               phoneNumberId != null && !phoneNumberId.trim().isEmpty();
    }

    @Bean(name = "whatsappRestTemplate")
    public RestTemplate whatsappRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        return new RestTemplate(factory);
    }
}
