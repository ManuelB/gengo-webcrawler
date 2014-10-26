package de.apaxo.translation;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.LogManager;

import javax.cache.Cache;
import javax.enterprise.event.Event;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class CrawlerTest {

	@BeforeClass
	public static void setUpLogging() {
		try {
			// https://community.oracle.com/thread/1307033?start=0&tstart=0
			LogManager.getLogManager().readConfiguration(
					Crawler.class.getResourceAsStream("/logging.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Test
	@Ignore
	public void testStartWithUrl() throws IOException {
		final Crawler crawler = new Crawler();

		@SuppressWarnings("unchecked")
		Cache<String, CrawlJob> cache = mock(Cache.class);

		final Map<String, CrawlJob> map = new HashMap<>();

		doAnswer(new Answer<Void>() {

			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				map.put((String) invocation.getArguments()[0],
						(CrawlJob) invocation.getArguments()[1]);
				return null;

			}
		}).when(cache).put(anyString(), any(CrawlJob.class));
		
		when(cache.get(anyString())).thenAnswer(new Answer<CrawlJob>() {

			@Override
			public CrawlJob answer(InvocationOnMock invocation)
					throws Throwable {
				return map.get(invocation.getArguments()[0]);
			}
			
		});

		crawler.crawlJobs = cache;

		@SuppressWarnings("unchecked")
		Event<CrawlWorkItem> grabUrl = mock(Event.class);

		doAnswer(new Answer<Void>() {

			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				crawler.processUrl((CrawlWorkItem) invocation.getArguments()[0]);
				return null;
			}

		}).when(grabUrl).fire(any(CrawlWorkItem.class));

		crawler.grabUrl = grabUrl;

		crawler.startWithUrl("http://www.3chirurgen.de");
	}
}