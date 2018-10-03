package ekoolab.com.show.api;

/**
 * @author Army
 * @version V_1.0.0
 * @date 2018/7/17
 * @description
 */
public class HttpException extends Exception {

    private int status;

    public HttpException(String message) {
        super(message);
    }

    public HttpException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
