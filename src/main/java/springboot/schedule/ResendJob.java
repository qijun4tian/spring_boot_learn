package springboot.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import springboot.RabbitMQUtils;
import springboot.pojo.ResendCacheMessage;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 祁军
 */
@Component
@Slf4j
public class ResendJob {
    /**
     * 定时重发
     */
    @Scheduled(fixedRate = 1000)
    public void ResendMessage() {
        List<ResendCacheMessage> resendCacheMessages =
                RabbitMQUtils.ResendcacheMap.values().stream().filter(v -> v.getHasSend().equals(false) && v.getResendTimes() < 3).collect(Collectors.toList());
        for (ResendCacheMessage resendCacheMessage : resendCacheMessages) {
            RabbitMQUtils.utilsSendTemplate.convertAndSend(resendCacheMessage.getExchargeName(), resendCacheMessage.getRoutingKey(), resendCacheMessage.getMessageBody(), new CorrelationData(resendCacheMessage.getCorrelationDataID()));
            resendCacheMessage.setHasSend(true);
            resendCacheMessage.setResendTimes(resendCacheMessage.getResendTimes()+1);
            System.out.println("正在重发消息 消息体为: "+resendCacheMessage.getMessageBody()+"重发次数为:"+resendCacheMessage.getResendTimes());
        }


    }
}
