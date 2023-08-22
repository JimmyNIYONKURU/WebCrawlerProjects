package com.udacity.webcrawler.json;
import com.fasterxml.jackson.core.JsonGenerator;


import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Objects;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
/**
 * Utility class to write a {@link CrawlResult} to file.
 */
public final class CrawlResultWriter
{
  private final CrawlResult result;

  /**
   * Creates a new {@link CrawlResultWriter} that will write the given {@link CrawlResult}.
   */
  public CrawlResultWriter(CrawlResult result) {
    this.result = Objects.requireNonNull(result);
  }

  /**
   * Formats the {@link CrawlResult} as JSON and writes it to the given {@link Path}.
   *
   * <p>If a file already exists at the path, the existing file should not be deleted; new data
   * should be appended to it.
   *
   * @param path the file path where the crawl result data should be written.
   */
  public void write(Path path) throws JsonProcessingException
  {
    // This is here to get rid of the unused variable warning.
    Objects.requireNonNull(path);
    //
    ObjectMapper objectMapper = new ObjectMapper();
    String jsonResult = objectMapper.writeValueAsString(result);
    try
    {
      Files.write(path, jsonResult.getBytes(), Files.exists(path) ? java.nio.file.StandardOpenOption.APPEND : java.nio.file.StandardOpenOption.CREATE);
    }
    catch(java.io.IOException e)
    {
      e.printStackTrace();
    }
  }

  /**
   * Formats the {@link CrawlResult} as JSON and writes it to the given {@link Writer}.
   *
   * @param writer the destination where the crawl result data should be written.
   */
  public void write(Writer writer) throws IOException
  {
    // This is here to get rid of the unused variable warning.
    Objects.requireNonNull(writer);
    //
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);  // Disable auto-close for the writer
    try
    {
      objectMapper.writeValue(writer, result);
    }
    catch (JsonProcessingException e)
    {
      e.printStackTrace();
    }

  }
}
