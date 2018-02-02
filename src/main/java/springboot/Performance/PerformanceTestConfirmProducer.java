package springboot.Performance;

import com.sun.media.jfxmedia.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

/**
 * @author 祁军
 */
@Service
@Slf4j
public class PerformanceTestConfirmProducer {
    @Autowired
    @Qualifier("comfirmRabbitTemplate")
    private RabbitTemplate rabbitTemplate;
    public void send(){
        log.info("异步确认的方式开始制造字符串");
        StringBuffer stringBuffer = new StringBuffer();
        for(int i =0 ;i<50;i++) {
            stringBuffer.append(UUID.randomUUID().toString());
        }
        log.info("stringBuffer=",stringBuffer);
        log.info("异步确认的方式制造完毕");
        log.info("异步确认的方式开始发送一万条数据");
        for(int i = 0;i<10000;i++) {
            rabbitTemplate.convertAndSend("direct.excharge", "test.comfirm", stringBuffer);
        }
        log.info("异步确认的方式发送结束");
    }

}
