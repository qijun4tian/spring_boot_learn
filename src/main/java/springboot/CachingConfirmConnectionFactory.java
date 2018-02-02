package springboot;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import springboot.config.RabbitMqProperties;

/**
 * @author 祁军
 */
public class CachingConfirmConnectionFactory extends CachingConnectionFactory{

    public static CachingConfirmConnectionFactory getCachingConfirmConnectionFactory(RabbitMqProperties rabbitMqProperties){
        CachingConfirmConnectionFactory cachingConnectionFactory = new CachingConfirmConnectionFactory(rabbitMqProperties);
        return cachingConnectionFactory;
    }

    public CachingConfirmConnectionFactory(RabbitMqProperties rabbitMqProperties){
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
//        cachingConnectionFactory.

    }
}
