package springboot;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 祁军
 */
public class TestOption<T extends Number> {
    private static final List<?> EMPTY = new ArrayList<>();

    public static <D> void copyMethod(List<? extends D> source, List<? super D> desc) {
        desc.add(source.get(0));
    }

    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        List<Integer> list1 = new ArrayList<>();
//        copyMethod(list,list1);
//        Collections.copy(list,list1);

    }
}
