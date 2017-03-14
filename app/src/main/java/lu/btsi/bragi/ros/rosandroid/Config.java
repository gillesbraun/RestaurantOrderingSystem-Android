package lu.btsi.bragi.ros.rosandroid;

import lu.btsi.bragi.ros.models.pojos.Language;
import lu.btsi.bragi.ros.models.pojos.Table;
import lu.btsi.bragi.ros.models.pojos.Waiter;

/**
 * Created by Gilles Braun on 14.03.2017.
 */

public class Config {
    private Language language = new Language("en", "English", null, null);
    private Table table;
    private Waiter waiter;

    private static final Config ourInstance = new Config();

    public static Config getInstance() {
        return ourInstance;
    }

    private Config() {
    }

    public Language getLanguage() {
        return language;
    }

    public Waiter getWaiter() {
        return waiter;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public void setWaiter(Waiter waiter) {
        this.waiter = waiter;
    }
}
