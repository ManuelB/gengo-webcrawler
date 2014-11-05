package de.apaxo.translation;

import static org.junit.Assert.assertEquals;
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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
	public void testGetPlainText() {
		Document doc = Jsoup
				.parse("<html><head><title>Hallo Welt</title></head><body><h1>Headline 1</h1><p>li bla blub</p><h2>More</h2><p>even more <br /> after br </p><ul><li>unordered</li><li>unorderd</li></ul><ol><li>one</li><li>two</li></ol></body></html>");
		String plainText = Crawler.getPlainText(doc);
		String expected = "Hallo Welt\n" + "# Headline 1\n"
				+ "li bla blub\n" + "## More\n" + "even more \n after br \n"
				+ " * unordered\n" + " * unorderd\n" + " 1. one\n" + " 1. two"
				+ "";
		assertEquals(expected, plainText);
		doc = Jsoup
				.parse("<html><head><title>Hallo Welt</title></head><body><h1>Headline 1</h1><p>li bla blub</p><h2>More</h2><p>even more <br /> after br </p><ul><li><li>unordered</li><li>unorderd</li></ul><ol><li>one</li><li>two</li><li></li></ol></body></html>");
		plainText = Crawler.getPlainText(doc);
		assertEquals(expected, plainText);
		
		doc = Jsoup
				.parse("<html><head><title>Hallo Welt</title></head><body><p>This <strong>strong</strong> men. Is one of the <br /> <p>better</p></p><p>and <a href=\"more.html\" target=\"_blank\">more</a> successful people<img src=\"image1.png\"/> <img src=\"image2.png\" alt=\"Watch him\"/></p></body></html>");
		plainText = Crawler.getPlainText(doc);
		assertEquals("Hallo Welt\n" + 
				"This **strong** men. Is one of the \n" + 
				"better\n" + 
				"and \n" + 
				"[more](more.html) successful people\n" + 
				"![](image1.png)\n" + 
				"![Watch him](image2.png)", plainText);
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
