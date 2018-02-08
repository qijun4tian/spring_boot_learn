package springboot.Performance;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 祁军
 */
@Service
public class PerformanceTestTranscatedReceiver {
    public static AtomicInteger transcatedReceiveCount = new AtomicInteger(0);


    @RabbitListener(queues = "test.transacted",containerFactory = "myContainerFactory")
    public void receive(String message){
        if(message.equals(PerformanceTestConfirmProducer.message) && System.currentTimeMillis() < PerformanceTestTranscatedProducer.startTime +Constant.oneSecond) {
            transcatedReceiveCount.incrementAndGet();
        }
    }
}
