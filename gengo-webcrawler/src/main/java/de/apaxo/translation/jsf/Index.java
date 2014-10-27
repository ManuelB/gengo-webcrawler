package de.apaxo.translation.jsf;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Model;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import de.apaxo.translation.CrawlJob;
import de.apaxo.translation.Crawler;
import de.apaxo.translation.URLCrawlResult;

@Named
@ViewScoped
public class Index implements Serializable {

	@Inject
	Crawler crawler;

	/**
	 * 
	 */
	private static final long serialVersionUID = -5987653448993231010L;
	private String url;
	private String selector;
	private String id;
	private Map<String, Boolean> selected = new HashMap<>();
	private String result;

	private CrawlJob crawlJob;

	@PostConstruct
	public void init() {
		setId(FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("id"));
		if (getId() != null) {
			setCrawlJob(crawler.get(getId()));
		}

	}

	/**
	 * Start crawling and set the id of this view to the given id.
	 */
	public String crawl() {
		try {
			setId(crawler.startWithUrl(getUrl(), getSelector()).getId());
			return "index?id=" + getId()
					+ "faces-redirect=true&includeViewParams=true";
		} catch (Exception ex) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(ex.getLocalizedMessage()));
		}
		return null;
	}

	/**
	 * Submit the crawled URLs to gengo.
	 * 
	 * @return
	 */
	public String submit() {
		try {
			setResult(crawler.submit(getSelectedEntries()));
		} catch (Exception ex) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(ex.getLocalizedMessage()));
		}
		FacesContext.getCurrentInstance().getExternalContext().getFlash()
				.put("index", this);
		return "submit";
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getSelector() {
		return selector;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public CrawlJob get() {
		return getId() == null ? null : crawler.get(getId());
	}

	public CrawlJob getCrawlJob() {
		return crawlJob;
	}

	public void setCrawlJob(CrawlJob crawlJob) {
		this.crawlJob = crawlJob;
		// Convert crawl results to normal array list
		// prevent java.lang.UnsupportedOperationException
		// at
		// java.util.concurrent.CopyOnWriteArrayList$COWIterator.set(CopyOnWriteArrayList.java:1185)
		// for sorting
		this.crawlJob.setUrlCrawlResults(new ArrayList<>(this.crawlJob
				.getUrlCrawlResults()));
	}

	public Map<String, Boolean> getSelected() {
		return selected;
	}

	public void setSelected(Map<String, Boolean> selected) {
		this.selected = selected;
	}

	public List<URLCrawlResult> getSelectedEntries() {
		List<URLCrawlResult> entries = new ArrayList<>();
		for (URLCrawlResult entry : crawlJob.getUrlCrawlResults()) {
			URL url = entry.getUrl();
			Boolean b = selected.get(url);
			if (b != null && b.booleanValue()) {
				entries.add(entry);
			}
		}
		return entries;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

}