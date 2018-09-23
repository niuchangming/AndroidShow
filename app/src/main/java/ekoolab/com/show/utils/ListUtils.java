package ekoolab.com.show.utils;

import java.util.Collection;

/**
 * @author Army
 * @version V_1.0.0
 * @date 2018/9/22
 * @description 集合的工具类
 */
public class ListUtils {

    public static boolean isNotEmpty(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }
}
