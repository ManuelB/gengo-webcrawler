package de.apaxo.translation;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
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
		Map<String, Boolean> alreadyFound = new HashMap<>();
		for (Element link : links) {
			String urlString = link.attr("abs:href");
			if (urlString != null) {
				// replace everything that is behind a hash symbol
				// e.g. http://www.example.com/#mytest ->
				// http://www.example.com/
				urlString = urlString.replaceAll("#.*$", "");
			}
			try {
				// filter duplicates
				if (!alreadyFound.containsKey(urlString)) {
					log.fine("Adding url string: " + urlString);
					URL foundUrl = new URL(urlString);
					grabUrl.fire(new CrawlWorkItem(crawlJob, foundUrl));
					crawlJob.getTotalUrls().incrementAndGet();
					alreadyFound.put(urlString, true);
				}
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
					text = getPlainText(doc.select(selector).select("> *"));
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
	 * Write function that takes Elements as input and concerts it to a new
	 * array list.
	 * 
	 * @param elements
	 * @return
	 */
	public static String getPlainText(Elements elements) {
		return getPlainText(new ArrayList<>(elements));
	}

	/**
	 * Gets the plain text from the elements.
	 * 
	 * @param elements
	 * @return
	 */
	public static String getPlainText(List<Node> nodes) {

		String noHTMLString = "";
		StringBuilder output = new StringBuilder();
		try {

			for (Node node : nodes) {
				if (node != null) {
					if (node instanceof Element) {
						Element el = (Element) node;
						if (el.tagName().equals("h1")) {
							output.append("\n# "
									+ getPlainText(el.childNodes()));
						} else if (el.tagName().equals("h2")) {
							output.append("\n## "
									+ getPlainText(el.childNodes()));
						} else if (el.tagName().equals("h3")) {
							output.append("\n### "
									+ getPlainText(el.childNodes()));
						} else if (el.tagName().equals("h4")) {
							output.append("\n#### "
									+ getPlainText(el.childNodes()));
						} else if (el.tagName().equals("h5")) {
							output.append("\n##### "
									+ getPlainText(el.childNodes()));
						} else if (el.tagName().equals("h6")) {
							output.append("\n###### "
									+ getPlainText(el.childNodes()));
						} else if (el.tagName().equals("strong")) {
							output.append("**" + getPlainText(el.childNodes())
									+ "**");
						} else if (el.tagName().equals("b")) {
							output.append("**" + getPlainText(el.childNodes())
									+ "**");
						} else if (el.tagName().equals("img")) {
							output.append("\n![" + el.attr("src") + "]");
							if (el.attr("alt") != null
									&& !el.attr("alt").equals("")) {
								output.append("(" + el.attr("alt") + ")");
							}
						} else if (el.tagName().equals("a")) {
							output.append("\n[" + getPlainText(el.childNodes())
									+ "](" + el.attr("href") + ")");
						} else if (el.tagName().equals("li")
								&& el.parent().tagName().equals("ul")) {
							String text = getPlainText(el.childNodes());
							if (text != null && !text.equals("")) {
								output.append("\n * " + text);
							}
						} else if (el.tagName().equals("li")
								&& el.parent().tagName().equals("ol")) {
							String text = getPlainText(el.childNodes());
							if (text != null && !text.equals("")) {
								output.append("\n 1. " + text);
							}
						} else if (el.tagName().equals("br")) {
							output.append("\n");
						} else if (el.tagName().equals("p")) {
							output.append("\n" + getPlainText(el.childNodes()));
						} else {
							output.append(getPlainText(el.childNodes()));
						}
					} else if (node instanceof TextNode) {
						TextNode text = (TextNode) node;

						if (text != null && !text.isBlank()) {
							output.append(text);
						}

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
		return getPlainText(doc.select("> *"));
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
			TranslationJob translationJob = new TranslationJob("Translate: "
					+ urlCrawlResult.getPath(), urlCrawlResult.getText(),
					urlCrawlResult.getSourceLanguage(),
					urlCrawlResult.getTargetLanguage(),
					Tier.valueOf(urlCrawlResult.getTier()));

			translationJob
					.setComment("This job was automatically generated from the URL: "
							+ urlCrawlResult.getUrl()
							+ " by https://github.com/ManuelB/gengo-webcrawler. Please report bugs as comments or github issues.");

			translationJob.setUsePreferredTranslators(urlCrawlResult.getPreferredTranslator());
			
			jobList.add(translationJob);
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
