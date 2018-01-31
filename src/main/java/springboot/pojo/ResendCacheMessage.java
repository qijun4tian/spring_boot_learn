package springboot.pojo;

import lombok.Builder;
import lombok.Data;

/**
 * @author 祁军
 */
@Data
@Builder
public class ResendCacheMessage {
    Integer resendTimes;
    String messageBody;
    String exchargeName;
    String routingKey;
    Boolean hasSend;
    String messageID;
}
