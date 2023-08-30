package com.udacity.webcrawler;

import com.udacity.webcrawler.json.CrawlResult;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.concurrent.RecursiveAction;
import java.util.HashMap;
import java.util.HashSet;



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

  @Inject
  ParallelWebCrawler(
      Clock clock,
      @Timeout Duration timeout,
      @PopularWordCount int popularWordCount,
      @TargetParallelism int threadCount) {
    this.clock = clock;
    this.timeout = timeout;
    this.popularWordCount = popularWordCount;
    this.pool = new ForkJoinPool(Math.min(threadCount, getMaxParallelism()));
  }

  @Override
  public CrawlResult crawl(List<String> startingUrls)
  {
    Instant deadline = clock.instant().plus(timeout);
    Map<String, Integer> counts = new HashMap<>();
    Set<String> visitedUrls = new HashSet<>();

    List<RecursiveAction> tasks = new ArrayList<>();
    for(String url : startingUrls)
    {
      tasks.add(new CrawlTask(url, deadline, maxDepth, counts, visitedUrls));
    }
    pool.invokeAll(tasks);
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

  private class CrawlTask extends RecursiveAction {
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
