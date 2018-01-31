package springboot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import springboot.config.RabbitMqProperties;
import springboot.pojo.CacheMessage;
import springboot.pojo.ResendCacheMessage;
import springboot.schedule.ResendTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 祁军
 */
@Slf4j
@Component
@EnableConfigurationProperties(RabbitMqProperties.class)
public class RabbitMQUtils {
    @Autowired
    private static RabbitMqProperties rabbitMqProperties;

    @Autowired
    private static ResendTask resendTask;

    public static RabbitTemplate utilsSendTemplate;

    private static RabbitTemplate utilsResendTemplate;

    private static volatile boolean hasInit = false;

    public static final Map<String, ResendCacheMessage> ResendcacheMap = new ConcurrentHashMap<>();
    public static final Map<String, CacheMessage> cacheMap = new HashMap<>();


    public static void init(RabbitMqProperties rabbitMqProperties) {
        if (!hasInit) {
            synchronized (RabbitMQUtils.class) {
                if (!hasInit) {
                    resendTask = new ResendTask();
                    resendTask.startCron();
                    CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
                    cachingConnectionFactory.setHost("localhost");
                    cachingConnectionFactory.setPort(5672);
                    cachingConnectionFactory.setUsername("guest");
                    cachingConnectionFactory.setPassword("guest");
                    cachingConnectionFactory.setVirtualHost("/");
                    cachingConnectionFactory.setPublisherConfirms(true);
                    cachingConnectionFactory.setPublisherReturns(true);
                    //先定义重发的resend的 rabbit template
                    utilsSendTemplate = new RabbitTemplate(cachingConnectionFactory);
                    //消息是否到达正确的excharge，如果没有会把消息返回
                    utilsSendTemplate.setConfirmCallback((correlationData, ack, cause) -> {
                        if (!ack) {
                            log.info("sender not send message to the right exchange" + " correlationData=" + correlationData + " ack=" + ack + " cause=" + cause);
                            if (ResendcacheMap.containsKey(correlationData.getId())) {
                                if (ResendcacheMap.get(correlationData.getId()).getResendTimes().equals(3) && ResendcacheMap.get(correlationData.getId()).getHasSend().equals(true)) {
                                    // do nothing
                                    log.error("try three times still not send message to the right exchange");
                                } else {
                                    ResendcacheMap.get(correlationData.getId()).setHasSend(false);
                                }
                            } else {
                                CacheMessage cacheMessage = cacheMap.get(correlationData.getId());
                                ResendCacheMessage resendCacheMessage = ResendCacheMessage.builder().messageBody(cacheMessage.getMessageBody())
                                        .exchargeName(cacheMessage.getExchargeName())
                                        .routingKey(cacheMessage.getRoutingKey())
                                        .resendTimes(0).hasSend(false).messageID(cacheMessage.getMessageID()).build();
                                ResendcacheMap.put(correlationData.getId(), resendCacheMessage);
                            }

                        }
                    });
                    //消息是否到达正确的消息队列，如果没有会把消息返回
                    utilsSendTemplate.setReturnCallback((message, replyCode, replyText, tmpExchange, tmpRoutingKey) -> {
                        log.info("Sender send message failed: " + message + " " + replyCode + " " + replyText + " " + tmpExchange + " " + tmpRoutingKey);
                        String messageId = message.getMessageProperties().getMessageId();
                        if (ResendcacheMap.containsKey(messageId)) {
                            if (ResendcacheMap.get(messageId).getResendTimes().equals(3) && ResendcacheMap.get(messageId).getHasSend().equals(true)) {
                                // do nothing just log error
                                log.error("try three times still not send message to the right route");

                            } else {
                                ResendcacheMap.get(messageId).setHasSend(false);
                            }
                        } else {
                            ResendCacheMessage resendMessage = ResendCacheMessage.builder().messageBody(new String(message.getBody()))
                                    .exchargeName(tmpExchange)
                                    .routingKey(tmpRoutingKey).resendTimes(0).hasSend(false).messageID(messageId).build();
                            ResendcacheMap.put(messageId, resendMessage);
                        }
                        //try to resend msg
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

    public static void sendMessage(String message, String exchargeName, String routingKey) {
        String messageID = UUID.randomUUID().toString();
        cacheMap.put(messageID, CacheMessage.builder().messageBody(message).exchargeName(exchargeName).routingKey(routingKey).build());
        utilsSendTemplate.convertAndSend(exchargeName, routingKey, new Message(message.getBytes(), MessagePropertiesBuilder.newInstance().setMessageId(messageID).build()), new CorrelationData(messageID));


    }


//    public static void sendMessage(String exchangeName, String queueName, Object Message) {
//        utilsRabbitTemplate.convertAndSend(exchangeName,queueName,Message);
//    }
}
