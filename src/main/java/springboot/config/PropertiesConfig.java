package springboot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by qijun123 on 2018/1/16.
 */
@Component
@ConfigurationProperties(prefix = "springboot")
public class PropertiesConfig {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
