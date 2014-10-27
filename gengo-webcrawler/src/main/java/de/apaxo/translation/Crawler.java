package de.apaxo.translation;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.cache.Cache;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.gengo.client.GengoClient;
import com.gengo.client.enums.Tier;
import com.gengo.client.exceptions.GengoException;
import com.gengo.client.payloads.TranslationJob;

/**
 * An EJB bean that can crawl an URL. It takes the start url, create a crawl job
 * and then fire events for all the other urls found on this site.
 * 
 * The crawl job gets a unique id and the status can be asked for.
 * 
 * @author manuel
 *
 */
@Stateless
public class Crawler {

	private static final Logger log = Logger.getLogger(Crawler.class.getName());

	@Inject
	Cache<String, CrawlJob> crawlJobs;

	@Inject
	Event<CrawlWorkItem> grabUrl;

	Properties properties;

	/**
	 * Load a properties file with the credentials.
	 */
	@PostConstruct
	public void loadGengoCredentials() {

		properties = new Properties();
		try {
			properties.load(Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("gengo.properties"));
		} catch (Exception e) {
			log.log(Level.SEVERE, "Could not load gengo.properties", e);
		}
	}

	/**
	 * Start with the given url.
	 * 
	 * @param url
	 *            the url to start on
	 * @return the CrawlJob that was created
	 * @throws IOException
	 */
	public CrawlJob startWithUrl(String url) throws IOException {
		return startWithUrl(url, null);
	}

	public CrawlJob startWithUrl(String url, String selector)
			throws IOException {
		CrawlJob crawlJob = new CrawlJob();
		if (selector != null && !selector.equals("")) {
			crawlJob.setSelector(selector);
		}
		crawlJob.setStartUrl(url);
		crawlJobs.put(crawlJob.getId(), crawlJob);
		// set timeout to 5 seconds
		Document doc = Jsoup.connect(url).timeout(5000).get();
		Elements links = doc.select("a[href]");
		for (Element link : links) {
			String urlString = link.attr("abs:href");
			try {
				log.fine("Adding url string: " + urlString);
				URL foundUrl = new URL(urlString);
				grabUrl.fire(new CrawlWorkItem(crawlJob, foundUrl));
				crawlJob.getTotalUrls().incrementAndGet();
			} catch (MalformedURLException ex) {
				log.log(Level.WARNING, "Could not parse url", ex);
				crawlJob.getThrowables().add(ex);
			}
		}
		return crawlJob;
	}

	public CrawlJob get(String id) {
		return crawlJobs.get(id);
	}

	@Asynchronous
	public void processUrl(@Observes CrawlWorkItem crawlWorkItem) {
		URL url = crawlWorkItem.getUrl();
		CrawlJob crawlJob = crawlWorkItem.getCrawlJob();
		String selector = crawlJob.getSelector();
		try {
			Document doc = Jsoup.connect(url.toString()).timeout(5000).get();
			String text;
			if (selector != null) {
				try {
					text = getPlainText(doc.select(selector).select("*"));
				} catch (Throwable t) {
					text = "Problem with css selector: " + selector;
					log.log(Level.WARNING, text + " on url " + url.toString(),
							t);
					crawlJob.getThrowables().add(t);
				}
			} else {
				text = getPlainText(doc);
			}
			URLCrawlResult urlCrawlResult = new URLCrawlResult(url, text);
			crawlJobs.get(crawlJob.getId()).getUrlCrawlResults()
					.add(urlCrawlResult);
			crawlJob.getCompletedUrls().incrementAndGet();
		} catch (IOException ex) {
			log.log(Level.WARNING, "Could not process: " + url.toString(), ex);
			crawlJob.getThrowables().add(ex);
			crawlJob.getCompletedUrls().incrementAndGet();
		}
	}

	/**
	 * Gets the plain text from the elements.
	 * 
	 * @param elements
	 * @return
	 */
	public static String getPlainText(Elements elements) {

		String noHTMLString = "";
		StringBuilder output = new StringBuilder();
		try {

			for (Element el : elements) {
				if (el != null && el.ownText() != null
						&& !el.ownText().equals("")) {
					if (el.tagName().equals("h1")) {
						output.append("= " + el.ownText() + " =\n");
					} else if (el.tagName().equals("h2")) {
						output.append("== " + el.ownText() + " ==\n");
					} else if (el.tagName().equals("h3")) {
						output.append("=== " + el.ownText() + " ===\n");
					} else if (el.tagName().equals("li")
							&& el.parent().tagName().equals("ul")) {
						output.append(" * "
								+ el.ownText().replaceAll("[\r\n]+", "") + "\n");
					} else if (el.tagName().equals("li")
							&& el.parent().tagName().equals("ol")) {
						output.append(" # "
								+ el.ownText().replaceAll("[\r\n]+", "") + "\n");
					} else {
						output.append(el.ownText() + "\n");
					}
				}
			}
		} catch (Exception ex) {
			log.log(Level.WARNING,
					"An error occurred while converting html mime content  to text/plain output: ",
					ex);
		}

		noHTMLString = output.toString();
		if (noHTMLString != null) {
			noHTMLString = noHTMLString.replaceAll("[\r\n]+", "\n");
		}
		return noHTMLString;

	}

	/**
	 * Gets the plain text from the doc.
	 * 
	 * @param doc
	 * @return
	 */
	public static String getPlainText(Document doc) {
		return getPlainText(doc.select("*"));
	}

	/**
	 * Send these entries to gengo for translation.
	 * 
	 * @param selectedEntries
	 * @return
	 * @throws GengoException
	 */
	public String submit(List<URLCrawlResult> selectedEntries)
			throws GengoException {
		List<TranslationJob> jobList = new ArrayList<TranslationJob>();
		for (URLCrawlResult urlCrawlResult : selectedEntries) {

			jobList.add(new TranslationJob("Translate: "
					+ urlCrawlResult.getUrl(), urlCrawlResult.getText(),
					urlCrawlResult.getSourceLanguage(), urlCrawlResult
							.getTargetLanguage(), Tier.valueOf(urlCrawlResult
							.getTier())));
		}
		String sandboxProperty = properties
				.getProperty("gengo.credentials.sandbox");
		boolean sandbox = sandboxProperty == null
				|| sandboxProperty.equals("true");
		GengoClient Gengo = new GengoClient(
				properties.getProperty("gengo.credentials.public_key"),
				properties.getProperty("gengo.credentials.private_key"),
				sandbox);
		JSONObject response = Gengo.postTranslationJobs(jobList, true);
		return response.toString();
	}
}
