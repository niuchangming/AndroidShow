package ekoolab.com.show.utils.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ekoolab.com.show.utils.Utils;

/**
 * @author cmniu
 *
 */
public class JSONParser {
	private static JSONParser parser;
	private static ParserListener listener;
	private String error;
	
	public static JSONParser getInstance(Object context){
		if(parser == null){
			parser = new JSONParser();
		}

		if(ParserListener.class.isAssignableFrom(context.getClass())){
			listener = (ParserListener)context;
		}
		return parser;
	}
	
	public void start(String json, Class<?> clz){
		if(listener == null) return;

		Object obj = null;
		try {
			Object objCheck = new JSONTokener(json).nextValue();
			if (objCheck instanceof JSONObject){
				JSONObject jsonObj = new JSONObject(json);
				obj = parseObject(clz, jsonObj);
			}else if (objCheck instanceof JSONArray){
				JSONArray jsonArr = new JSONArray(json);
				obj = parseArray(clz, jsonArr);
			}else{
				error = "Incorrect json string.";
			}
		} catch (JSONException e) {
			error = e.toString();
		}

		if(!Utils.isBlank(error)){
			listener.onParseError(error);
			return;
		}
		listener.onParseSuccess(obj);
	}
	
	public List<?> parseArray(Class<?> clz, JSONArray arrJson){
		List<Object> objList = new ArrayList<Object>();
		try {
			if(arrJson != null){
				objList = new ArrayList<Object>();
				for(int i = 0; i < arrJson.length(); i++){
					objList.add(parseObject(Class.forName(clz.getName()), arrJson.getJSONObject(i)));
				}
			}
		} catch (ClassNotFoundException | IllegalArgumentException | JSONException e) {
			error = e.toString();
		}
		return objList;
	}
	
	public Object parseObject(Class<?> clz, JSONObject jsonObj){
		Object obj = null;
		try {
			obj = clz.newInstance();
			JSOName annotation;
			String jsonField = "";
			for(Field field : obj.getClass().getDeclaredFields()){
				if(field.getAnnotation(FieldExclude.class) != null){
					continue;
				}

				annotation = field.getAnnotation(JSOName.class);
				jsonField = field.getName();
				if(annotation != null){
					jsonField = annotation.name();
				}
				
				if(field.getType() == String.class){
					field.set(obj, jsonObj.optString(jsonField));
				}else if(field.getType().toString().equals("double")){
					field.set(obj, jsonObj.optDouble(jsonField));
				}else if(field.getType().toString().equals("int")){
					field.set(obj, jsonObj.optInt(jsonField));
				}else if(field.getType().toString().equals("long")){
					field.set(obj, jsonObj.optLong(jsonField));
				}else if(field.getType().toString().equals("boolean")){
					field.set(obj, jsonObj.optBoolean(jsonField));
				}else if(field.getType() == List.class){
					JSONArray jsonArr = jsonObj.optJSONArray(jsonField);
					
					Type type = field.getGenericType();
				    if (type instanceof ParameterizedType) {
				        field.set(obj, 
				        		parseArray(Class.forName(((ParameterizedType)type)
				        				.getActualTypeArguments()[0].toString().substring(6)), 
				        				jsonArr));
				    }
				}else if(field.getType() == Date.class){
					if(!jsonObj.optString(jsonField).equalsIgnoreCase("null")){
						field.set(obj, Utils.getDateByMillis(jsonObj.optLong(jsonField)));
					}else{
						field.set(obj, new Date());
					}
				}else if(field.getType().getName().toString().startsWith("ekoolab.com.show.beans")){
					JSONObject memJsonObj = jsonObj.optJSONObject(jsonField);
					if(memJsonObj != null){
						Object memObj = parseObject(Class.forName(field.getType().getName()), memJsonObj);
						field.set(obj, memObj);
					}
				}
			}
		} catch (IllegalAccessException | IllegalArgumentException | InstantiationException | ClassNotFoundException | ParseException | NullPointerException e) {
			error = e.toString();
		}
		return obj;
	}
	
	public interface ParserListener{
		void onParseSuccess(Object obj);
		void onParseError(String err);
	}
	
}
