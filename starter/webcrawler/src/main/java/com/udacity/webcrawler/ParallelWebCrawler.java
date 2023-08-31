package com.udacity.webcrawler;
import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;
import javax.inject.Inject;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;
/**
 * A concrete implementation of {@link WebCrawler} that runs multiple threads on a
 * {@link ForkJoinPool} to fetch and process multiple web pages in parallel.
 */
final class ParallelWebCrawler implements WebCrawler
{
  private final Clock clock;
  private final Duration timeout;
  private final int popularWordCount;
  private final ForkJoinPool pool;
  private final List<Pattern> ignoredUrls;
  private final int maxDepth;
  private final PageParserFactory parserFactory;
  @Inject
  ParallelWebCrawler(
          Clock clock,
          @Timeout Duration timeout,
          @PopularWordCount int popularWordCount,
          @TargetParallelism int threadCount,
          @IgnoredUrls List<Pattern> ignoredUrls,
          @MaxDepth int maxDepth,
          PageParserFactory parserFactory) {
    this.clock = clock;
    this.timeout = timeout;
    this.popularWordCount = popularWordCount;
    this.pool = new ForkJoinPool(Math.min(threadCount, getMaxParallelism()));
    this.ignoredUrls = ignoredUrls;
    this.maxDepth = maxDepth;
    this.parserFactory = parserFactory;
  }
  @Override
  public CrawlResult crawl(List<String> startingUrls)
  {
    Instant deadline = clock.instant().plus(timeout);
    Map<String, Integer> counts = new HashMap<>();
    Set<String> visitedUrls = new HashSet<>();
    for(String url : startingUrls)
    {
      pool.invoke(new CrawlTask(url, deadline, maxDepth, counts, visitedUrls));
    }
    return new CrawlResult.Builder()
            .setWordCounts(WordCounts.sort(counts, popularWordCount))
            .setUrlsVisited(visitedUrls.size())
            .build()
            ;
  }
  @Override
  public int getMaxParallelism()
  {
    return Runtime.getRuntime().availableProcessors();
  }
  private class CrawlTask extends RecursiveAction
  {
    private final String url;
    private final Instant deadline;
    private final int currentDepth;
    private final Map<String, Integer> counts;
    private final Set<String> visitedUrls;
    CrawlTask(
            String url,
            Instant deadline,
            int currentDepth,
            Map<String, Integer> counts,
            Set<String> visitedUrls) {
      this.url = url;
      this.deadline = deadline;
      this.currentDepth = currentDepth;
      this.counts = counts;
      this.visitedUrls = visitedUrls;
    }
    @Override
    protected void compute()
    {
      if (currentDepth <= 0 || clock.instant().isAfter(deadline))
      {
        return;
      }
      for (Pattern pattern : ignoredUrls)
      {
        if (pattern.matcher(url).matches())
        {
          return;
        }
      }
      if (!visitedUrls.contains(url))
      {
        visitedUrls.add(url);
        PageParser.Result result = parserFactory.get(url).parse();
        for (Map.Entry<String, Integer> entry : result.getWordCounts().entrySet())
        {
          counts.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }
        List<CrawlTask> subtasks = new ArrayList<>();
        for (String link : result.getLinks())
        {
          subtasks.add(
                  new CrawlTask(link, deadline, currentDepth - 1, counts, visitedUrls));
        }
        invokeAll(subtasks);
      }
    }
  }
}
