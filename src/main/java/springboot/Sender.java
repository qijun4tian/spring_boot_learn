package springboot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import springboot.config.RabbitMQConfig;

import javax.print.DocFlavor;
import java.util.UUID;

/**
 * 角色表Service
 *
 * @author 祁军
 */
@Service
@Slf4j
public class Sender {
    private RabbitTemplate rabbitTemplate;

    @Autowired
    public Sender(RabbitTemplate rabbitTemplate) {
        //消息是否到达交换机的回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.info("sender not send message to the right exchange" + " correlationData=" + correlationData + " ack=" + ack + " cause" + cause);
            } else {
                log.info("sender send message to the right exchange" + " correlationData=" + correlationData + " ack=" + ack + " cause" + cause);
            }
        });
        //消息是否到达正确的消息队列，如果没有会把消息返回
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, tmpExchange, tmpRoutingKey) -> {
            System.out.println(message);
            log.info("Sender send message failed: " + message + " " + replyCode + " " + replyText + " " + tmpExchange + " " + tmpRoutingKey);
            //try to resend msg
        });
//        rabbitTemplate.setChannelTransacted(true);

        RetryTemplate retryTemplate = new RetryTemplate();
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(500);
        backOffPolicy.setMultiplier(10.0);
        backOffPolicy.setMaxInterval(10000);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        rabbitTemplate.setRetryTemplate(retryTemplate);
        rabbitTemplate.setMandatory(true);


        this.rabbitTemplate = rabbitTemplate;

    }

    public void send() {
        System.out.println("sender is sending message");
        //先发送一条正确的消息
        // 不要三条消息一起发送

        String uuid1 = UUID.randomUUID().toString();
        String uuid2 = UUID.randomUUID().toString();
        String uuid3 = UUID.randomUUID().toString();
        System.out.println("UUID="+uuid1+"---"+uuid2+"---"+uuid3);


//        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "aaa.orange.bbb", "hello,world1 2", new CorrelationData(uuid1));
//        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "11111", "hello,world1 2", new CorrelationData(uuid1));
        byte [] messageBody = "hello,world1 2".getBytes();
//        rabbitTemplate.send(RabbitMQConfig.EXCHANGE_NAME, "aaa.orange.bbb",new Message(messageBody, MessagePropertiesBuilder.newInstance().setCorrelationIdString(uuid3).
//                        setMessageId(uuid3).setContentType("text/x-json").build()),
//                new CorrelationData(uuid3)
//                );

//        try {
//            rabbitTemplate.send("111111", "aaa.orange.bbb", new Message(messageBody, MessagePropertiesBuilder.newInstance().setCorrelationIdString(uuid3).
//                            setMessageId(uuid3).setContentType("text/x-json").build()),
//                    new CorrelationData(uuid3));
//            rabbitTemplate.send(RabbitMQConfig.EXCHANGE_NAME, "aaa.orange.bbb", new Message(messageBody, MessagePropertiesBuilder.newInstance().setCorrelationIdString(uuid3).
//                            setMessageId(uuid3).setContentType("text/x-json").build()),
//                    new CorrelationData(uuid3));
//            rabbitTemplate.send(RabbitMQConfig.EXCHANGE_NAME, "aaa.orange.bbb", new Message(messageBody, MessagePropertiesBuilder.newInstance().setCorrelationIdString(uuid3).
//                            setMessageId(uuid3).setContentType("text/x-json").build()),
//                    new CorrelationData(uuid3));
//        }catch (Exception e){
//            log.info("commit error");
//        }


//        RabbitMQUtils.sendMessage("这是一个测试错误交换器名","错误的交换机名","错误的routing_key");
//
//        RabbitMQUtils.sendMessage("这是一个测试错误routingkey",RabbitMQConfig.EXCHANGE_NAME,"错误的routing_key");


        //在发送一条交换机错误的消息
//
//      rabbitTemplate.convertAndSend("测试交换机名", "aaa.orange.ccc", "测试错误的交换机名", new CorrelationData(uuid2));
        //正确的交换机错误的队列
//       rabbitTemplate.convertAndSend("测试交换机名", "1111111", "测试错误的队列名", new CorrelationData(uuid3));


        rabbitTemplate.convertAndSend("fanout","","fanout 交换机");
        rabbitTemplate.convertAndSend("direct","temp.queue.1","direct 交换机");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {


        byte [] messageBody = "hello,world1 2".getBytes();

        System.out.println(new String(messageBody));
    }
}
