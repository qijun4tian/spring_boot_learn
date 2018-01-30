package springboot;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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

    public static RabbitTemplate utilsSendTemplate;

    private static RabbitTemplate utilsResendTemplate;

    private static volatile boolean hasInit = false;

    public static final Map<String, ResendCacheMessage> ResendcacheMap = new ConcurrentHashMap<>();
    public static final Map<String, CacheMessage> cacheMap = new HashMap<>();


    public static void init() {
        if (!hasInit) {
            synchronized (RabbitMQUtils.class) {
                if (!hasInit) {
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
                                        .resendTimes(0).hasSend(false).correlationDataID(cacheMessage.getCorrelationDataID()).build();
                                ResendcacheMap.put(correlationData.getId(), resendCacheMessage);
                            }

                        }
                    });
                    //消息是否到达正确的消息队列，如果没有会把消息返回
                    utilsSendTemplate.setReturnCallback((message, replyCode, replyText, tmpExchange, tmpRoutingKey) -> {
                        log.info("Sender send message failed: " + message + " " + replyCode + " " + replyText + " " + tmpExchange + " " + tmpRoutingKey);
                        String correlationDataID = message.getMessageProperties().getCorrelationIdString();
                        if (ResendcacheMap.containsKey(correlationDataID)) {
                            if (ResendcacheMap.get(correlationDataID).getResendTimes().equals(3) && ResendcacheMap.get(correlationDataID).getHasSend().equals(true)) {
                                // do nothing just log error
                                log.error("try three times still not send message to the right route");

                            } else {
                                ResendcacheMap.get(correlationDataID).setHasSend(false);
                            }
                        } else {
                            ResendCacheMessage resendMessage = ResendCacheMessage.builder().messageBody(message.getBody().toString())
                                    .exchargeName(tmpExchange)
                                    .routingKey(tmpRoutingKey).resendTimes(0).hasSend(false).correlationDataID(correlationDataID).build();
                            ResendcacheMap.put(correlationDataID, resendMessage);
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
        String correlationDataId = UUID.randomUUID().toString();
        cacheMap.put(correlationDataId, CacheMessage.builder().messageBody(message).exchargeName(exchargeName).routingKey(routingKey).build());
        utilsSendTemplate.convertAndSend(exchargeName, routingKey, message, new CorrelationData(correlationDataId));
    }






//    public static void sendMessage(String exchangeName, String queueName, Object Message) {
//        utilsRabbitTemplate.convertAndSend(exchangeName,queueName,Message);
//    }
}
