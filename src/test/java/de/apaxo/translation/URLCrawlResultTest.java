package de.apaxo.translation;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

public class URLCrawlResultTest {

	@Test
	public void testGetPath() throws MalformedURLException {
		URLCrawlResult urlCrawlResult = new URLCrawlResult();
		assertNull(urlCrawlResult.getPath());
		urlCrawlResult.setUrl(new URL("http://www.example.com"));
		assertEquals("", urlCrawlResult.getPath());
		urlCrawlResult.setUrl(new URL("http://www.example.com/"));
		assertEquals("/",urlCrawlResult.getPath());
		urlCrawlResult.setUrl(new URL("http://www.example.com/my-cool-file.php"));
		assertEquals("/my-cool-file.php",urlCrawlResult.getPath());
		urlCrawlResult.setUrl(new URL("http://www.example.com/my-cool-file.php?param1=yeah"));
		assertEquals("/my-cool-file.php",urlCrawlResult.getPath());
	}

}
