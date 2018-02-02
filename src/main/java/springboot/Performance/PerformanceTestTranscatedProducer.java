package springboot.Performance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author 祁军
 */
@Service
@Slf4j
public class PerformanceTestTranscatedProducer {
    @Autowired
    @Qualifier("transactedRabbitTemplate")
    private RabbitTemplate rabbitTemplate;

    public void send() {
        log.info("事务的方式开始制造字符串");
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < 50; i++) {
            stringBuffer.append(UUID.randomUUID().toString());
        }
        log.info("stringBuffer=", stringBuffer);
        log.info("事务的方式制造完毕");
        log.info("事务的方式开始发送一万条数据");
        for (int i = 0; i < 10000; i++) {
            try {

                rabbitTemplate.convertAndSend("direct.excharge", "test.transacted", stringBuffer);

            } catch (Exception e) {
                log.info("transacted 发送错误");
            }
        }
        log.info("事务的方式发送结束");
    }




}
