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
    public static long startTime;

    public static String message;

    public void send(){
        StringBuffer stringBuffer = new StringBuffer();
        for(int i =0 ;i<50;i++) {
            stringBuffer.append(UUID.randomUUID().toString());
        }
        message = stringBuffer.toString();
        log.info("stringBuffer=",stringBuffer);
        log.info("异步确认的方式开始发送数据");
        startTime = System.currentTimeMillis();
        while( System.currentTimeMillis() <= startTime +  Constant.oneSecond){
            rabbitTemplate.convertAndSend("direct.excharge", "test.comfirm", message);
        }
        log.info("异步确认的方式发送结束");
    }

}
