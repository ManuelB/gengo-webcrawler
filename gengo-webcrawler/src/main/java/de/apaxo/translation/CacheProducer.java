package de.apaxo.translation;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.enterprise.inject.Produces;

public class CacheProducer {
	/**
	 * Get a global cache object that can map objects,
	 * to objects.
	 * Other types might trigger:
	 * Caused by: java.lang.ClassCastException: ISPN021011: Incompatible cache value types specified, expected class java.lang.String but class java.lang.Object was specified
	 * @return
	 */
	@Produces
	public Cache<String, CrawlJob> getCache() {
		// Retrieve the system wide cache manager
		CacheManager cacheManager = Caching.getCachingProvider()
				.getCacheManager();
		// Define a named cache with default JCache configuration
		Cache<?, ?> cache = cacheManager.getCache("global",
				Object.class, Object.class);
		if (cache == null) {
			cache = cacheManager.createCache("global",
					new MutableConfiguration<Object, Object>()
							.setStoreByValue(false));
		}
		return (Cache<String, CrawlJob>) cache;
	}
}
