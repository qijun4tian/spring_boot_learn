import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import springboot.Application;
import springboot.RabbitMQUtils;
import springboot.Sender;
import springboot.Sender1;

/**
 * 角色表Service
 *
 * @author 祁军
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class RabbitMQTest {

    @Autowired
    private Sender sender;
    @Autowired
    private Sender1 sender1;

    @Test
    public void send() throws Exception {
//        RabbitMQUtils.init();
        sender.send();
//        sender1.send();
//       Thread.sleep(20000);

//        Thread.sleep(10000);

        Thread.sleep(5000);
    }

}

