package ekoolab.com.show.api;

import com.alibaba.fastjson.JSON;
import com.androidnetworking.interfaces.Parser;

import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * @author Army
 * @version V_1.0.0
 * @date 2018/9/30
 * @description
 */
public final class FastJsonRequestBodyParser<T> implements Parser<T, RequestBody> {
    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    FastJsonRequestBodyParser() {
    }

    @Override
    public RequestBody convert(T value) throws IOException {
        return RequestBody.create(MEDIA_TYPE, JSON.toJSONString(value));
    }
}
