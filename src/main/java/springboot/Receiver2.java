package springboot;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * @author 祁军
 */
@Service
public class Receiver2 {
    @RabbitListener(queues = "${rabbitConfiguration.queue}", containerFactory = "myContainerFactory")
    public void processMessage(String msg) {
        System.out.println("Receiver2 got message" + msg);
    }
}
