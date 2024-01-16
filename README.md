# Project: Parallel Web Crawler

![UdaciSearch Logo](UdaciSearch.png)

"Welcome! This is your first week at the startup, **UdaciSearch.** You've been hired on as an Engineer, and you're really excited to make a big splash. UdaciSearch is interested in figuring out popular search terms on the internet in order to improve the [SEO](https://en.wikipedia.org/wiki/Search_engine_optimization) of their clients. Everyone wants to pop up at the top of a potential user's search! 

You are given the source code for your company's legacy web crawler, which is single-threaded. You notice it's a bit slow, and you quickly realize a way to improve its performance and impress your new manager. You can upgrade the code to take advantage of multi-core architectures to increase crawler throughput. Furthermore, you will measure the performance of your crawler to prove that, given the same amount of time, the multi-threaded implementation can visit more web pages than the legacy implementation. It might just be your first week, but you're set to impress!"

Above the introduction to the project.

## Getting Started

### Dependencies

  * Java JDK Version 17
  * Maven 3.6.3 or higher
  * IntelliJ IDEA

### Installation and some usage explanations

  * Download the [JDK 17](https://www.oracle.com/java/technologies/downloads). 
    I recommend JDK 17 since it is the latest long-term support Java version. Accept the license agreements and run the downloaded installer.
  * Follow the official [installation](https://maven.apache.org/install.html) and run `mvn -version` in a terminal to make sure you have at least version 3.6.3 installed.
  * Download the Community Edition of [IntelliJ IDEA](https://www.jetbrains.com/idea/download/). Run the downloaded installer.



Looking closely at the code repo that was shared with you, it's clear that the web crawler app reads in a JSON file to determine how it should run. Let's look an example of such a file:

#### JSON Configuration Example

```
{
  "startPages": ["http://example.com", "http://example.com/foo"],
  "ignoredUrls": ["http://example\\.com/.*"],
  "ignoredWords": ["^.{1,3}$"],
  "parallelism": 4,
  "implementationOverride": "com.udacity.webcrawler.SequentialWebCrawler",
  "maxDepth": 10,
  "timeoutSeconds": 7,
  "popularWordCount": 3,
  "profileOutputPath": "profileData.txt"
  "resultPath": "crawlResults.json"
}
```
  * `startPages` - These URLs are the starting point of the web crawl.
  
  * `ignoredUrls` - A list of regular expressions defining which, if any, URLs should not be followed by the web crawler. In this example, the second starting page will be ignored.
  
  * `ignoredWords` - A list of regular expressions defining which words, if any, should not be counted toward the popular word count. In this example, words with 3 or fewer characters are ignored.
  
  * `parallelism` - The desired parallelism that should be used for the web crawl. If set to 1, the legacy crawler should be used. If less than 1, parallelism should default to the number of cores on the system.
  
  * `implementationOverride` - An explicit override for which web crawler implementation should be used for this crawl. In this example, the legacy crawler will always be used, regardless of the value of the "parallelism" option.

  If this option is empty or unset, the "parallelism" option will be used (instead of the "implementationOverride" option) to determine which crawler to use. If this option is set to a non-empty string that is not the fully-qualified name of a class that implements the `WebCrawler` interface, the crawler will immediately fail.
  
  * `maxDepth` - The max depth of the crawl. The "depth" of a crawl is the maximum number of links the crawler is allowed to follow from the starting pages before it must stop. This option can be used to limit how far the crawler drifts from the starting URLs, or can be set to a very high number if that doesn't matter.
  
       *Example*: Suppose your starting page "A", links to the following web pages, and you want to run with a depth of 2.
       
![Page Traversal](PageTraversal.png)

In This Example, Your Crawler Would Only Visit Pages A, B, C, and D

* `timeoutSeconds` - The max amount of time the crawler is allowed to run, in seconds. Once this amount of time has been reached, the crawler will finish processing any HTML it has already downloaded, but it is not allowed to download any more HTML or follow any more hyperlinks.
  
* `popularWordCount` - The number of popular words to record in the output. In this example, the 3 most frequent words will be recorded. If there is a tie in the top 3, word length is used as a tiebreaker, with longer words taking preference. If the words are the same length, words that come first alphabetically get ranked higher.
  
* `profileOutputPath` - Path to the output file where performance data for this web crawl should be 
. If there is already a file at that path, the new data should be appended. If this option is empty or unset, the profile data should be printed to standard output.
  
* `resultPath` - Path where the web crawl result JSON should be written. If a file already exists at that path, it should be overwritten. If this option is empty or unset, the result should be printed to standard output.


  
###  Run the Parallel Crawler!

To run entire parallel web crawler, complete with performance profiling! You should now be able to run it with the following commands:

```
mvn package
java -classpath target/udacity-webcrawler-1.0.jar \
    com.udacity.webcrawler.main.WebCrawlerMain \
    src/main/config/sample_config.json
```
                  
## Built With


A brief moment to appreciate all the third-party Java libraries used. 

* [jsoup](https://jsoup.org/) - An open-source Java library for working with HTML.
  * License: [MIT License](https://jsoup.org/license)
* [Jackson Project](https://github.com/FasterXML/jackson) - Affectionately known as "the best JSON parser for Java".
  * License: [Apache 2.0](https://github.com/FasterXML/jackson-core/blob/master/src/main/resources/META-INF/LICENSE)
* [Guice](https://github.com/google/guice/) - An open-source dependency injection framework for Java.
  * License: [Apache 2.0](https://github.com/google/guice/blob/master/COPYING)
* [Maven](https://maven.apache.org/) - Used to build and manage the project dependencies.
  * License: [Apache 2.0 ](http://maven.apache.org/ref/3.0/license.html)
* [JUnit 5](https://junit.org/junit5/) - An open-source unit testing framework for Java.
  * License: [Eclipse Public License 2.0](https://github.com/junit-team/junit5/blob/main/LICENSE.md)
* [Truth](https://github.com/google/truth) - An open-source assertion framework used in Java unit tests.
  * License: [Apache 2.0](https://github.com/google/truth/blob/master/LICENSE)


