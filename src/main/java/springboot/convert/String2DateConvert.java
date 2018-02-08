package springboot.convert;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author 祁军
 */
@Component
public class String2DateConvert implements Converter<String, Date> {

    private String dateFormat;

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    @Override
    public Date convert(String source) {
        DateFormat df = new SimpleDateFormat(dateFormat);
        try {
            return df.parse(source);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }


}
