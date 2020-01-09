package nl.dgoossens.chiselsandbits2.api.cache;

/**
 * Represents an object caching data, the cache will be
 * cleared before each rendering cycle starts.
 */
public interface CacheClearable {
    void clearCache();
}
