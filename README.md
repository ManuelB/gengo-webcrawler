gengo-webcrawler
================

First working version of gengo crawler that can crawl a german website and afterwards submit jobs to the gengo website. It is doing the following:
   
  1. Receiving the given page
  2. Create a crawl work item for all found links
  3. Download all found links and extract the text by the given css selector. All html tags are removed
  4. Offer an interface for a human to select the pages including word counts
  5. Send the pages to gengo

Made gengo credentials configurable:

   mvn install -Dgengo.credentials.public_key=YOUR_PUBLIC_KEY -Dgengo.credentials.private_key=YOUR_PRIVATE_KEY -Dgengo.credentials.sandbox=true
