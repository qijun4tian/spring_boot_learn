package springboot;

import org.springframework.stereotype.Service;

/**
 * 角色表Service
 *
 * @author 祁军
 * 使用 SimpleMessageListenerContainer 和listenerAdapter方式来接收消息
 */
@Service
public class Receiver {

    public void receiveMessage(String message) {
        System.out.println("Received<" + message + ">");
    }

    public void receive2(String in) throws InterruptedException {

        System.out.println("Receiver in message" + in);
    }
}
