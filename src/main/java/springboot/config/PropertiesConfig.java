package springboot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by qijun123 on 2018/1/16.
 */
@Component
@Data
@ConfigurationProperties(prefix = "spring.rabbitmq")
public class PropertiesConfig {
//    private String name = "123";
    private String host = "localhost";
    private int port = 15672;

    private String username;

    private String password;

    private String virtualhost = "/";



}
