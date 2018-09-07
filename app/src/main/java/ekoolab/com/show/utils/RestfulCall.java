package ekoolab.com.show.utils;


public class RestfulCall {

    private static RestfulCall instance;

    public static RestfulCall getInstance(){
        if(instance == null){
            synchronized (RestfulCall.class) {
                if(instance == null){
                    instance = new RestfulCall();
                }
            }
        }
        return instance;
    }


}




















