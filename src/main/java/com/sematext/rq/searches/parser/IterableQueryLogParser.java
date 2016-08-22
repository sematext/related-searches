package com.sematext.rq.searches.parser;

import com.sematext.rq.searches.common.Settings;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;

/**
 * Abstract base class for {@link QueryLogParser} implementations.
 * 
 * @author sematext, http://www.sematext.com/
 */
public abstract class IterableQueryLogParser implements QueryLogParser {
  protected BufferedReader reader;
  protected DateFormat dateFormat;

  /**
   * Parses next available log line.
   * 
   * @param input
   *          Stream of log data.
   * @return List of log entries.
   * @throws IOException
   *           thrown when fatal error occurs
   */
  protected abstract QueryLogEntry parseLogLine() throws IOException;

  /**
   * Constructor.
   * 
   * @param filepath
   *          path to query log file
   * @throws FileNotFoundException
   *           thrown when given file is not found
   * @throws IOException
   *           thrown when I/O error occurs
   */
  public IterableQueryLogParser(String filepath) throws FileNotFoundException, IOException {
    InputStream is = new FileInputStream(filepath);
    reader = new BufferedReader(new InputStreamReader(is, Settings.DEFAULT_CHARSET));
    dateFormat = new SimpleDateFormat(Settings.DEFAULT_DATE_FORMAT);
  }

  /**
   * (non-Javadoc)
   * 
   * @see com.sematext.rq.searches.parser.didyoumean.QueryLogParser#sortedQueryLogIterator()
   */
  @Override
  public Iterator<QueryLogEntry> sortedQueryLogIterator() {
    return new Iterator<QueryLogEntry>() {
      private QueryLogEntry cached = null;

      /**
       * (non-Javadoc)
       * 
       * @see java.util.Iterator#remove()
       */
      @Override
      public void remove() {
        throw new RuntimeException("Not implemented");
      }

      /**
       * (non-Javadoc)
       * 
       * @see java.util.Iterator#next()
       */
      @Override
      public QueryLogEntry next() {
        if (cached != null) {
          QueryLogEntry entry = cached;
          cached = null;
          return entry;
        } else {
          try {
            return parseLogLine();
          } catch (IOException e) {
            try {
              reader.close();
            } catch (IOException ioe) {
            }
            return null;
          }
        }
      }

      /**
       * (non-Javadoc)
       * 
       * @see java.util.Iterator#hasNext()
       */
      @Override
      public boolean hasNext() {
        if (cached != null) {
          return true;
        } else {
          try {
            cached = parseLogLine();
          } catch (IOException e) {
          }
          if (cached != null) {
            return true;
          } else {
            try {
              reader.close();
            } catch (IOException ioe) {
            }
            return false;
          }
        }
      }
    };
  }
}
