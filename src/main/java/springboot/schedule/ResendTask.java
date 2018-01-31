package springboot.schedule;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import springboot.RabbitMQUtils;
import springboot.pojo.ResendCacheMessage;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

/**
 * @author 祁军
 */

public class ResendTask {

    private ScheduledFuture<?> future;


    public void startCron() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.initialize();
        future = threadPoolTaskScheduler.schedule(new resendJob(), new CronTrigger("0/2 * * * * *"));
    }





    public void stopCron() {
        if (future != null) {
            future.cancel(true);
        }
    }


    private class resendJob implements Runnable {
        @Override
        public void run() {
            System.out.println("ResendJob is working");
            List<ResendCacheMessage> resendCacheMessages =
                    RabbitMQUtils.ResendcacheMap.values().stream().filter(v -> v.getHasSend().equals(false) && v.getResendTimes() < 3).collect(Collectors.toList());
            for (ResendCacheMessage resendCacheMessage : resendCacheMessages) {
                System.out.println("正在重发消息");
                String messageID = resendCacheMessage.getMessageID();
                RabbitMQUtils.utilsSendTemplate.convertAndSend(resendCacheMessage.getExchargeName(), resendCacheMessage.getExchargeName(),
                        new Message(resendCacheMessage.getMessageBody().getBytes(), MessagePropertiesBuilder.newInstance().setMessageId(messageID).build()), new CorrelationData(messageID));
                resendCacheMessage.setHasSend(true);
                resendCacheMessage.setResendTimes(resendCacheMessage.getResendTimes()+1);
                System.out.println("正在重发消息 消息体为: "+resendCacheMessage.getMessageBody()+"重发次数为:"+resendCacheMessage.getResendTimes());
            }
        }

    }
}
