package springboot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springboot.config.RabbitMQConfig;

import java.util.UUID;

/**
 * @author 祁军
 */
@Service
@Slf4j
public class Sender1 {

    private RabbitTemplate rabbitTemplate;

    @Autowired
    public Sender1(RabbitTemplate rabbitTemplate) {
        //消息是否到达交换机的回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.info("sender1 not send message to the right exchange" + " correlationData=" + correlationData + " ack=" + ack + " cause" + cause);
            } else {
                log.info("sender1 send message to the right exchange" + " correlationData=" + correlationData + " ack=" + ack + " cause" + cause);
            }
        });
        //消息是否到达正确的消息队列，如果没有会把消息返回
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, tmpExchange, tmpRoutingKey) -> {
            log.info("Sender1 send message failed: " + message + " " + replyCode + " " + replyText + " " + tmpExchange + " " + tmpRoutingKey);
            //try to resend msg
        });
        rabbitTemplate.setMandatory(true);
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send() {
        System.out.println("sender1 is sending message");
        //先发送一条正确的消息
        CorrelationData correlationData1 = new CorrelationData(UUID.randomUUID().toString());
        System.out.println("correlationData1 = "+ correlationData1);
       rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_EXCHANGE_NAME, "aaa.orange.bbb", "hello,world1 0", correlationData1);
        //在发送一条交换机错误的消息
//        rabbitTemplate.convertAndSend("测试交换机名", "aaa.orange.ccc", "测试错误的交换机名", new CorrelationData(UUID.randomUUID().toString()));
        //正确的交换机错误的队列
//        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_EXCHANGE_NAME, "1111111", "测试错误的队列名", new CorrelationData(UUID.randomUUID().toString()));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
