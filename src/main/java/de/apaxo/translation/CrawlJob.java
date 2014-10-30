package de.apaxo.translation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class CrawlJob implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8648345773716585728L;
	private String id = UUID.randomUUID().toString();
	private AtomicInteger completedUrls = new AtomicInteger();
	private AtomicInteger totalUrls = new AtomicInteger();
	private List<URLCrawlResult> urlCrawlResults = new CopyOnWriteArrayList<URLCrawlResult>();
	private List<Throwable> throwables = new ArrayList<>();
	private String selector;
	private String startUrl;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public AtomicInteger getCompletedUrls() {
		return completedUrls;
	}

	public AtomicInteger getTotalUrls() {
		return totalUrls;
	}

	public List<URLCrawlResult> getUrlCrawlResults() {
		return urlCrawlResults;
	}

	public void setUrlCrawlResults(List<URLCrawlResult> urlCrawlResults) {
		this.urlCrawlResults = urlCrawlResults;
	}

	public List<Throwable> getThrowables() {
		return throwables;
	}

	public void setThrowables(List<Throwable> throwables) {
		this.throwables = throwables;
	}

	public String getSelector() {
		return selector;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}

	public String getStartUrl() {
		return startUrl;
	}

	public void setStartUrl(String startUrl) {
		this.startUrl = startUrl;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((completedUrls == null) ? 0 : completedUrls.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((selector == null) ? 0 : selector.hashCode());
		result = prime * result
				+ ((startUrl == null) ? 0 : startUrl.hashCode());
		result = prime * result
				+ ((throwables == null) ? 0 : throwables.hashCode());
		result = prime * result
				+ ((totalUrls == null) ? 0 : totalUrls.hashCode());
		result = prime * result
				+ ((urlCrawlResults == null) ? 0 : urlCrawlResults.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CrawlJob other = (CrawlJob) obj;
		if (completedUrls == null) {
			if (other.completedUrls != null)
				return false;
		} else if (!completedUrls.equals(other.completedUrls))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (selector == null) {
			if (other.selector != null)
				return false;
		} else if (!selector.equals(other.selector))
			return false;
		if (startUrl == null) {
			if (other.startUrl != null)
				return false;
		} else if (!startUrl.equals(other.startUrl))
			return false;
		if (throwables == null) {
			if (other.throwables != null)
				return false;
		} else if (!throwables.equals(other.throwables))
			return false;
		if (totalUrls == null) {
			if (other.totalUrls != null)
				return false;
		} else if (!totalUrls.equals(other.totalUrls))
			return false;
		if (urlCrawlResults == null) {
			if (other.urlCrawlResults != null)
				return false;
		} else if (!urlCrawlResults.equals(other.urlCrawlResults))
			return false;
		return true;
	}

}
