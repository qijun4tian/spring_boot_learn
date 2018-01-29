package springboot;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * @author 祁军
 */
//@Service
public class ErrorHandler {
    @RabbitListener(queues = "dead_queue", containerFactory = "myContainerFactory")
    public void handleError(Object message){
        System.out.println("XXXXXXX"+message);
    }
}
