package ekoolab.com.show.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.androidnetworking.interfaces.Parser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * @author Army
 * @version V_1.0.0
 * @date 2018/9/30
 * @description
 */
public class FastJsonParserFactory extends Parser.Factory {
    public static final Feature[] EMPTY_SERIALIZER_FEATURES = new Feature[0];

    public FastJsonParserFactory() {
    }

    @Override
    public Parser<ResponseBody, ?> responseBodyParser(Type type) {
        return new FastJsonResponseBodyParser<>(type);
    }

    @Override
    public Parser<?, RequestBody> requestBodyParser(Type type) {
        return new FastJsonRequestBodyParser<>();
    }

    @Override
    public Object getObject(String string, Type type) {
        try {
            return JSON.parseObject(string, type, EMPTY_SERIALIZER_FEATURES);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getString(Object object) {
        try {
            return JSON.toJSONString(object);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public HashMap<String, String> getStringMap(Object object) {
        try {
            Type type = new TypeToken<HashMap<String, String>>() {
            }.getType();
            return JSON.parseObject(JSON.toJSONString(object), type, EMPTY_SERIALIZER_FEATURES);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }
}
