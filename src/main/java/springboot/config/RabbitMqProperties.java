package springboot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author 祁军
 */
@Data
@ConfigurationProperties(prefix = "mq")
public class RabbitMqProperties {

    private String host = "localhost";

    private int port = 5672;

    private String username = "guest";

    private String password = "guest";

    private String virtualhost = "/";
}
