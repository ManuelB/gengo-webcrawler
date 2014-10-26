gengo-webcrawler
================

First working version of gengo crawler that can crawl a german website and afterwards submit jobs to the gengo website. It is doing the following:
   
  # Receiving the given page
  # Create a crawl work item for all found links
  # Download all found links and extract the text by the given css selector. All html tags are removed
  # Offer an interface for a human to select the pages including word counts
  # Send the pages to gengo
