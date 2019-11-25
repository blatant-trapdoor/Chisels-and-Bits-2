package nl.dgoossens.chiselsandbits2.client.render.models;

import java.util.*;

public enum CacheType {
    DEFAULT,
    MODEL,
    ROUTINE(175), //Every 175ms
    ;

    private final List<CacheClearable> clearable = new ArrayList<>();

    CacheType() {}
    CacheType(long repeating) {
        //Call this cache every repeating milliseconds.
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                call();
            }
        }, repeating, repeating);
    }

    public void register(CacheClearable c) {
        clearable.add(c);
    }

    public void call() {
        for (CacheClearable c : clearable)
            c.clearCache();
    }
}
