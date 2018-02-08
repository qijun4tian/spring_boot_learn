package springboot.Performance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 祁军
 */
@Service
@Slf4j
public class PerformanceTestTranscatedProducer {
    @Autowired
    @Qualifier("transactedRabbitTemplate")
    private RabbitTemplate rabbitTemplate;
    public static long startTime;
    public static  AtomicInteger count;
    public void send() {

        log.info("事务的方式开始发送数据");
        count  = new AtomicInteger(0) ;
        ExecutorService service = Executors.newFixedThreadPool(100);
        startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() <= startTime + Constant.oneSecond) {

            Runnable runnable = ()-> {
                try {
//                log.info("第" + count + "次发送开始");
                    rabbitTemplate.convertAndSend("direct.excharge", "test.transacted", PerformanceTestConfirmProducer.message);

                } catch (Exception e) {
                    log.info("transacted 发送错误");
                }
                if (System.currentTimeMillis() <= startTime +  Constant.oneSecond) {
                    count.incrementAndGet();
                }
            };
            service.execute(runnable);
        }
        log.info("事务的方式发送结束 总共发送了"+ count);
    }


}
