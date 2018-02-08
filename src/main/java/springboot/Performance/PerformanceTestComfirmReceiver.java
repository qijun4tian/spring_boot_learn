package springboot.Performance;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 祁军
 */
@Service
public class PerformanceTestComfirmReceiver {
    public static AtomicInteger confirmReceiveCount = new AtomicInteger(0);


    @RabbitListener(queues = "test.comfirm",containerFactory = "myContainerFactory1")
    public void receive(String message){
        if(message.equals(PerformanceTestConfirmProducer.message) && System.currentTimeMillis()< PerformanceTestConfirmProducer.startTime+Constant.oneSecond) {
//            System.out.println("接收到的消息为"+message);
            confirmReceiveCount.incrementAndGet();
        }
    }
}
