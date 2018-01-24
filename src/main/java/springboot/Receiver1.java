package springboot;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * @author 祁军
 * 使用 SimpleRabbitListenerContainerFactory 和 @RabbitListener 方式接收mq 的消息
 */
@Service
public class Receiver1 {
    @RabbitListener(queues = "${rabbitConfiguration.queue}", containerFactory = "myContainerFactory")
    public void processMessage(String msg) {
        System.out.println("Receiver1 got message" + msg);
    }
}
