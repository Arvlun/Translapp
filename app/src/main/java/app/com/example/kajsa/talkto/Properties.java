package app.com.example.kajsa.talkto;

/**
 *  Singleton containing textmode setting variable
 */
public class Properties {

    private static Properties inst = null;

    private Properties(){}

    public boolean textmode;

    public static synchronized Properties getInstance() {
        if (null == inst) {
            inst = new Properties();
        }
        return inst;
    }
}
