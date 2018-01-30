package springboot.utils;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.listener.FatalExceptionStrategy;
import org.springframework.amqp.rabbit.listener.exception.ListenerExecutionFailedException;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.handler.annotation.support.MethodArgumentTypeMismatchException;
import springboot.MyException;

@Slf4j
public class MessageFatalExceptionStrategy implements FatalExceptionStrategy {


	@Override
	public boolean isFatal(Throwable t) {
		if (t instanceof ListenerExecutionFailedException && causeIsFatal(t.getCause())) {
			log.error("系统存在致命bug，导致"+t.getMessage()+"，使得消费端瘫痪");
			return true;
		}
		return false;
	}
	
	public static boolean causeIsFatal(Throwable cause) {
		return cause instanceof MessageConversionException
				|| cause instanceof org.springframework.messaging.converter.MessageConversionException
				|| cause instanceof MethodArgumentNotValidException
				|| cause instanceof MethodArgumentTypeMismatchException
				|| cause instanceof NullPointerException
			    || isUserCauseFatal(cause);
	}

	// 可以在此处增加用户自定义的fatal exception
	// 此处使用MyException
	private static boolean isUserCauseFatal(Throwable cause) {
		return cause instanceof MyException;
	}
}
