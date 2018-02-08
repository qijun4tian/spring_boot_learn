import com.sun.media.jfxmedia.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import springboot.Application;
import springboot.Performance.PerformanceTestComfirmReceiver;
import springboot.Performance.PerformanceTestConfirmProducer;
import springboot.Performance.PerformanceTestTranscatedProducer;
import springboot.Performance.PerformanceTestTranscatedReceiver;
import springboot.config.RabbitMQTestConfig;

/**
 * @author 祁军
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@Slf4j
public class TestPerformance {
    @Autowired
    PerformanceTestConfirmProducer performanceTestConfirmProducer;

    @Autowired
    PerformanceTestTranscatedProducer performanceTestTranscatedProducer;

    @Test
    public void send() {


        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            performanceTestConfirmProducer.send();
            log.info("异步方式一秒钟总共发送了多少数据" + RabbitMQTestConfig.count);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            performanceTestTranscatedProducer.send();
//        try {
//            Thread.sleep(20000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("异步方式一秒钟总共接收了" + PerformanceTestComfirmReceiver.confirmReceiveCount + "条数据");
            log.info("事务的方式一秒钟总共接收了" + PerformanceTestTranscatedReceiver.transcatedReceiveCount + "条数据");

        }

        log.info("异步方式测试10次一秒钟总共发送了多少数据" + RabbitMQTestConfig.count);
        log.info("同步方式测试10次一秒钟总共发送了多少数据" + PerformanceTestTranscatedProducer.count);
    }







}
