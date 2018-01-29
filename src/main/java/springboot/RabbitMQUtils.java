package springboot;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import springboot.config.RabbitMqProperties;

import java.util.Map;

/**
 * @author 祁军
 */
@Slf4j
public class RabbitMQUtils {
    @Autowired
    private static RabbitMqProperties rabbitMqProperties;

    private static RabbitTemplate utilsSendTemplate;

    private static RabbitTemplate utilsResendTemplate;

    private static volatile boolean hasInit = false;

    private static Map<String,Integer> map;


    public static void init() {
        if (!hasInit) {
            synchronized (RabbitMQUtils.class) {
                if (!hasInit) {
                    CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
                    cachingConnectionFactory.setHost(rabbitMqProperties.getHost());
                    cachingConnectionFactory.setPort(rabbitMqProperties.getPort());
                    cachingConnectionFactory.setUsername(rabbitMqProperties.getUsername());
                    cachingConnectionFactory.setPassword(rabbitMqProperties.getPassword());
                    cachingConnectionFactory.setVirtualHost(rabbitMqProperties.getVirtualhost());
                    cachingConnectionFactory.setPublisherConfirms(true);
                    //先定义重发的resend的 rabbit template
                    utilsResendTemplate = new RabbitTemplate(cachingConnectionFactory);
                    utilsResendTemplate.setConfirmCallback((correlationData, ack, cause) -> {
                        if (!ack) {
                            log.info("sender not send message to the right exchange" + " correlationData=" + correlationData + " ack=" + ack + " cause" + cause);
                        }
                    });
                    //消息是否到达正确的消息队列，如果没有会把消息返回
                    utilsResendTemplate.setReturnCallback((message, replyCode, replyText, tmpExchange, tmpRoutingKey) -> {
                        log.info("Sender send message failed: " + message + " " + replyCode + " " + replyText + " " + tmpExchange + " " + tmpRoutingKey);
                        //try to resend msg
                    });

                    utilsSendTemplate = new RabbitTemplate(cachingConnectionFactory);
                    utilsSendTemplate.setConfirmCallback((correlationData, ack, cause) -> {
                        if (!ack) {
                            log.error("Sender not send message to the right exchange" + " correlationData=" + correlationData + " ack=" + ack + " cause" + cause);
                        }
                    });
                    //消息是否到达正确的消息队列，如果没有会把消息返回
                    utilsSendTemplate.setReturnCallback((message, replyCode, replyText, tmpExchange, tmpRoutingKey) -> {
                        log.error("Sender send message failed: " + message + " " + replyCode + " " + replyText + " " + tmpExchange + " " + tmpRoutingKey);
                        map.put(message.getMessageProperties().getCorrelationIdString(),new Integer(3));

                    });

                    RetryTemplate retryTemplate = new RetryTemplate();
                    ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
                    backOffPolicy.setInitialInterval(500);
                    backOffPolicy.setMultiplier(10.0);
                    backOffPolicy.setMaxInterval(10000);
                    retryTemplate.setBackOffPolicy(backOffPolicy);
                    utilsSendTemplate.setRetryTemplate(retryTemplate);
                    utilsSendTemplate.setMandatory(true);


                }
            }
        }
    }



//    public static void sendMessage(String exchangeName, String queueName, Object Message) {
//        utilsRabbitTemplate.convertAndSend(exchangeName,queueName,Message);
//    }
}
