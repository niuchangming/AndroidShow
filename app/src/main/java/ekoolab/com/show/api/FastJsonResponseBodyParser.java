package ekoolab.com.show.api;

import com.alibaba.fastjson.JSON;
import com.androidnetworking.interfaces.Parser;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;

/**
 * @author Army
 * @version V_1.0.0
 * @date 2018/9/30
 * @description
 */
public final class FastJsonResponseBodyParser<T> implements Parser<ResponseBody, T> {

    private Type type;

    FastJsonResponseBodyParser(Type type) {
        this.type = type;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        return JSON.parseObject(value.string(), type, FastJsonParserFactory.EMPTY_SERIALIZER_FEATURES);
    }
}
