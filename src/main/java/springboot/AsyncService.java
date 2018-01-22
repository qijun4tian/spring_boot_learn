package springboot;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author 祁军
 */
@Service
public class AsyncService {
    @Async(value = "testExecutor")  //如果不指定名字，会使用缺省的“asyncExecutor”
    public void testAsyncService() {
        System.out.println("test in thread pool "+Thread.currentThread());
    }
}
