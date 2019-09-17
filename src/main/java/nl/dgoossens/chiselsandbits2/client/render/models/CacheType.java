package nl.dgoossens.chiselsandbits2.client.render.models;

import java.util.ArrayList;
import java.util.List;

public enum CacheType {
    DEFAULT,
    MODEL,
    ;

    private final List<CacheClearable> clearable = new ArrayList<>();

    public void register(CacheClearable c) {
        clearable.add(c);
    }

    public void call() {
        for (CacheClearable c : clearable)
            c.clearCache();
    }
}
