import java.util.*;
import java.io.*;
import org.json.*;

public class translate {

    public static void main(String[] args) throws Exception {
        InputStream is = new FileInputStream("messages.json");
        byte[] buffer = new byte[4096];
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        int read = 0;
        while ((read = is.read(buffer)) > 0) {
            bs.write(buffer, 0, read);
        }
        is.close();
        JSONObject translations = new JSONObject(bs.toString());
        Properties properties = new Properties();
        for (String key : (Set<String>) translations.keySet()) {
            properties.setProperty(key, translations.getString(key));
        }
        OutputStream os = new FileOutputStream("messages.properties");
        properties.store(os, "Blockseditor Translations");
        os.close();
    }

}
