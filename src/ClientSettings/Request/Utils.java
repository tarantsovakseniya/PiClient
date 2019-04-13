package ClientSettings.Request;

public class Utils {

    public static Request getRequest(String data) {
        if (data.equals("stop")||data.equals("finish")) return Request.STOP;
        if (data.contains("dwn")) return Request.DOWNLOAD;
        if (data.contains("upload")) return Request.UPLOAD;
        return Request.ELSE;
    }
}
