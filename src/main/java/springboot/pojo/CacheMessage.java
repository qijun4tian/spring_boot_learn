package springboot.pojo;

import lombok.Builder;
import lombok.Data;

/**
 * @author 祁军
 */
@Data
@Builder
public class CacheMessage {
    String messageBody;
    String exchargeName;
    String routingKey;
    String correlationDataID;
}