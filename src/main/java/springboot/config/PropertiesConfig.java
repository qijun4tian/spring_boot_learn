package springboot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by qijun123 on 2018/1/16.
 */
@Component
@Data
@ConfigurationProperties(prefix = "springboot")
public class PropertiesConfig {
//    private String name = "123";
    private String host = "localhost";
    private int port = 15672;

    private List<String> name;

    private String password;

    private String virtualhost = "/";



}
