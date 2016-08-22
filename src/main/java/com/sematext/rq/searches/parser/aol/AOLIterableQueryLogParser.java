/*
 *    Copyright (c) Sematext International
 *    All Rights Reserved
 *
 *    THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Sematext International
 *    The copyright notice above does not evidence any
 *    actual or intended publication of such source code.
 */
package com.sematext.rq.searches.parser.aol;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import com.sematext.rq.searches.common.Settings;
import com.sematext.rq.searches.parser.IterableQueryLogParser;
import com.sematext.rq.searches.parser.QueryLogEntry;
import com.sematext.rq.searches.parser.QueryLogParser;

/**
 * {@link QueryLogParser} implementation for AOL query log like files. The file may contain the following lines: <code>
 * sessionId query date time 
 * </code> OR <code>
 * sessionId query date time count url
 * </code>.
 * 
 * All the parameters apart from date and time should be delimited with tab character. The delimiter between date and
 * time should be a space character.
 * 
 * @author sematext, http://www.sematext.com/
 */
public class AOLIterableQueryLogParser extends IterableQueryLogParser {
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
  public AOLIterableQueryLogParser(String filepath) throws FileNotFoundException, IOException {
    super(filepath);
  }

  /**
   * (non-Javadoc)
   * 
   * @see com.sematext.rq.searches.parser.didyoumean.eval.IterableQueryLogParser#parseLogLine()
   */
  @Override
  protected QueryLogEntry parseLogLine() throws IOException {
    String line = null;
    // lines that only contain - character will be ignored
    // line that are not split correctly will be ignored
    do {
      line = reader.readLine();
      if ("-".equals(line) || (line != null && line.split(Settings.AOL_LOG_FORMAT_SPLIT_CHARACTER).length < 3)) {
        continue;
      } else {
        break;
      }
    } while (true);
    return getLogEntry(line);
  }

  /**
   * Parses given line.
   * 
   * @param line
   *          line to parse
   * @return {@link QueryLogEntry} or <code>null</code>
   */
  protected QueryLogEntry getLogEntry(String line) {
    QueryLogEntry entry = null;
    if (line != null) {
      // split on tab characters
      String[] splits = line.split(Settings.AOL_LOG_FORMAT_SPLIT_CHARACTER);
      if (splits.length >= 3) {
        String query = splits[1];
        String sessionId = splits[0];

        Date timestamp;
        try {
          timestamp = dateFormat.parse(splits[2]);
        } catch (ParseException pe) {
          // use 0L date for lines that have date parsing exceptions
          timestamp = new Date(0L);
        }

        if (splits.length == 5) {
          // line with click information
          entry = new QueryLogEntry(query, splits[4], sessionId, getCount(splits[3]) > 0 ? true : false, timestamp);
        } else if (splits.length == 4) {
          // line with number of results
          long count = getCount(splits[3]);
          entry = new QueryLogEntry(query, null, sessionId, count > 0 ? true : false, timestamp, count);
        } else if (splits.length == 3) {
          // line without click information
          entry = new QueryLogEntry(query, null, sessionId, false, timestamp);
        }
      }
    }
    return entry;
  }

  private long getCount(String countPart) {
    Long count = 0l;
    try {
      count = Long.parseLong(countPart);
    } catch (Exception ex) {
      // do nothing
    }
    return count;
  }
}
