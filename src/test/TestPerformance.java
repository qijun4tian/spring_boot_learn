import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import springboot.Application;
import springboot.Performance.PerformanceTestConfirmProducer;
import springboot.Performance.PerformanceTestTranscatedProducer;

/**
 * @author 祁军
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class TestPerformance {
    @Autowired
    PerformanceTestConfirmProducer performanceTestConfirmProducer;

    @Autowired
    PerformanceTestTranscatedProducer performanceTestTranscatedProducer;

    @Test
    public void send() {
        performanceTestConfirmProducer.send();
        performanceTestTranscatedProducer.send();
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
