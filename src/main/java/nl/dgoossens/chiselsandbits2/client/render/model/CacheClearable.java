package nl.dgoossens.chiselsandbits2.client.render.model;

/**
 * Represents an object caching data, the cache will be
 * cleared before each rendering cycle starts.
 */
public interface CacheClearable {
    void clearCache();
}
