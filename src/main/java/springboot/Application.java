package springboot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springboot.config.PropertiesConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Created by qijun123 on 2018/1/16.
 */

@SpringBootApplication
@RestController
@ComponentScan(basePackages = { "springboot" })
public class Application {
    @Autowired
    private PropertiesConfig propertiesConfig;
    @Autowired
    private AsyncService asyncService;

    @RequestMapping("/")
    public String index() {
        return "Spring Boot Application..."+propertiesConfig.getName() ;
    }

    @RequestMapping("/testThreadPoll")
    public void testThreadPoll() {
        asyncService.testAsyncService();
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

//        Optional<String> optional = Optional.ofNullable(null);
//        System.out.println(optional.map(l->"123").orElse("345"));


    }



}
