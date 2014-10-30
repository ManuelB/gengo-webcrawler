package de.apaxo.translation;

import java.io.Serializable;
import java.net.URL;

public class URLCrawlResult implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3926553986575172923L;
	private URL url;
	private String text;
	private String sourceLanguage = "de";
	private String targetLanguage = "en";
	private String tier = "STANDARD";

	public URLCrawlResult() {
		
	}
	
	public URLCrawlResult(URL url, String text) {
		super();
		this.url = url;
		this.text = text;
	}

	public String getSourceLanguage() {
		return sourceLanguage;
	}

	public void setSourceLanguage(String sourceLanguage) {
		this.sourceLanguage = sourceLanguage;
	}

	public String getTargetLanguage() {
		return targetLanguage;
	}

	public void setTargetLanguage(String targetLanguage) {
		this.targetLanguage = targetLanguage;
	}


	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Integer getWordCount() {
		String trim = getText().trim();
		if (trim.isEmpty())
			return 0;
		return trim.split("\\s+").length; // separate string around spaces

	}

	public String getTier() {
		return tier;
	}

	public void setTier(String tier) {
		this.tier = tier;
	}
}
