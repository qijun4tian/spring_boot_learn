package springboot;

import javafx.scene.media.SubtitleTrack;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springboot.config.PropertiesConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
    @RequestMapping("/testMap")
    public void testMap(@RequestBody Map<String,Object> map){
        System.out.println(map.get("name")+"   "+(map.get("list")));
        System.out.println(map.get("list") instanceof List);
        System.out.println(map.get("list").getClass());
    }

    @RequestMapping("/testRequestBody")
    public void testMap(@RequestBody InputBody input){
        System.out.println(input.getName()+"   "+input.getList());
    }
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

//        Optional<String> optional = Optional.ofNullable(null);
//        System.out.println(optional.map(l->"123").orElse("345"));


    }

    @Data
    static class InputBody{
        private String name;
        private List<String> list;

    }


}
