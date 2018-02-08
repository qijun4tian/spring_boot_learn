package springboot;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springboot.config.PropertiesConfig;
//import springboot.config.RabbitMQConfig;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by qijun123 on 2018/1/16.
 */

@SpringBootApplication
@RestController
@Slf4j
@Api(tags = "main controller")
@EnableScheduling
@ComponentScan(basePackages = {"springboot"})
public class Application {
    @Autowired
    private PropertiesConfig propertiesConfig;
    @Autowired
    private AsyncService asyncService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ApiOperation(value = "主页")
    public String index() {
        log.info("test log4j2 test log4j2 test log4j2");
        return "Spring Boot Application..." + propertiesConfig.getName();
    }

    @RequestMapping(value = "/testThreadPoll", method = RequestMethod.GET)
    public void testThreadPoll() {
        asyncService.testAsyncService();
    }

    @ApiOperation(value = "测试接口")
    @ApiImplicitParam(dataType = "map", name = "map", required = true)
    @RequestMapping(value = "/testMap", method = RequestMethod.POST)
    public void testMap(@RequestBody Map<String, Object> map) {
        log.info("test log4j2 test log4j2 test log4j2");
        System.out.println(map.get("name") + "   " + map.get("list"));
        System.out.println(map.get("list") instanceof List);
        System.out.println(map.get("list").getClass());
    }

    @RequestMapping("/testRequestBody")
    public void testMap(@RequestBody InputBody input) {
        System.out.println(input.getName() + "   " + input.getName());
    }

//    @RequestMapping("/testRabbitMQUtil")
//    public void testRabbitMQUtil() {
//        RabbitMQUtils.sendMessage("hello util",RabbitMQConfig.EXCHANGE_NAME,"11111") ;
//    }

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx  = SpringApplication.run(Application.class, args);



    }

    @Data
    static class InputBody {
        private String name;
        private List<String> list;

    }


}
