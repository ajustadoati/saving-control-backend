
package com.ajustadoati.sc.config.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JwtProviderProperty class
 *
 * @author rojasric
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "jwt")
public class JwtProviderProperties {

  private String secretKey;

}
