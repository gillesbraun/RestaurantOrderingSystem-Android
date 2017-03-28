package lu.btsi.bragi.ros.rosandroid;

import android.content.Context;
import android.os.Build;

import java.util.Locale;

import lu.btsi.bragi.ros.models.pojos.Language;
import lu.btsi.bragi.ros.models.pojos.Location;
import lu.btsi.bragi.ros.models.pojos.Waiter;

/**
 * Created by Gilles Braun on 14.03.2017.
 */

public class Config {
    private Language language = new Language("en", "English", null, null);
    private Waiter waiter;

    private static final Config ourInstance = new Config();
    private Location location;

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

    public Locale getLocale(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.getResources().getConfiguration().getLocales().get(0);
        } else {
            return context.getResources().getConfiguration().locale;
        }
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }
}
