package de.apaxo.translation;

import java.net.URL;

public class CrawlWorkItem {
	private CrawlJob job;
	private URL url;

	public CrawlWorkItem() {

	}

	public CrawlWorkItem(CrawlJob jobId, URL url) {
		super();
		this.job = jobId;
		this.url = url;
	}

	public CrawlJob getCrawlJob() {
		return job;
	}

	public void setCrawlJob(CrawlJob jobId) {
		this.job = jobId;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}
}
