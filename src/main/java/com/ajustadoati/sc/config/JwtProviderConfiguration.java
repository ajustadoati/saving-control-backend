
package com.ajustadoati.sc.config;

import com.ajustadoati.sc.config.properties.JwtProviderProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JwtProviderProperties.class)
@Slf4j
public class JwtProviderConfiguration {

}
