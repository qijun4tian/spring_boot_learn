package springboot;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * 角色表Service
 *
 * @author 祁军
 */
@Service
public class Receiver {

    public void receiveMessage(String message) {
        System.out.println("Received<" + message + ">");
    }

    public void receive2(String in) throws InterruptedException {
        System.out.println("in message"+in);
    }
}
