package springboot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springboot.config.PropertiesConfig;

/**
 * Created by qijun123 on 2018/1/16.
 */

@SpringBootApplication
 @RestController
 public class Application {
    @Autowired
    private PropertiesConfig propertiesConfig;
    @RequestMapping("/")
    public String index(){ return "Spring Boot Application..."+ propertiesConfig.getName(); }
    public static void main(String[] args)
    {
        SpringApplication.run(Application.class, args); }

}
