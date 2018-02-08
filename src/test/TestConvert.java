import org.hamcrest.core.IsInstanceOf;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import springboot.Application;
import springboot.AsyncService;
import springboot.convert.TestConvertDate;

import java.util.Date;

/**
 * @author 祁军
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class TestConvert {
    @Autowired
    private ConfigurableApplicationContext configurableApplicationContext;

    @Test
    public void testAsync() {
        TestConvertDate testConvert = (TestConvertDate)configurableApplicationContext.getBean("testConvert");
        testConvert.setDate("2014-03-04 09:21:20");
        System.out.println(testConvert);
        if(testConvert.getDate()instanceof Date)
        {
            System.out.println("convert work");
        }
        if(testConvert.getDate()instanceof String)
        {
            System.out.println("convert not work");
        }
    }

}