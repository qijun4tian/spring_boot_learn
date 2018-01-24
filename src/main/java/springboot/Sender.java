package springboot;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springboot.config.RabbitMQConfig;

/**
 * 角色表Service
 *
 * @author 祁军
 */
@Service
public class Sender {
    private RabbitTemplate rabbitTemplate;

    @Autowired
    public Sender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send() {
        System.out.println("sender is sending message");
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_EXCHANGE_NAME, "aaa.orange.bbb", "hello,world1 0");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 1; i < 10; i++) {
            rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_EXCHANGE_NAME, "aaa.orange.ccc", "hello,world " + i);
        }
    }
}
