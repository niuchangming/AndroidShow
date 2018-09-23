package ekoolab.com.show.api;

/**
 * @author Army
 * @version V_1.0.0
 * @date 2018/9/22
 * @description 响应体统一处理
 */
public class ResponseData<T> {

    public int errorCode;
    public String message;
    public int total;
    public T data;
}
