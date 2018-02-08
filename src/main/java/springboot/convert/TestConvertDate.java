package springboot.convert;

import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author 祁军
 */
@Component(value = "testConvert")
@Data
public class TestConvertDate {

    public void setDate(String date) {
        this.date = date;
    }

    private Object date;

    public Object getDate() {
        return date;
    }

}