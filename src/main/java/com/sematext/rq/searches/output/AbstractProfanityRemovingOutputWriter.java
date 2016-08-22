/*
 *    Copyright (c) Sematext International
 *    All Rights Reserved
 *
 *    THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Sematext International
 *    The copyright notice above does not evidence any
 *    actual or intended publication of such source code.
 */
package com.sematext.rq.searches.output;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

/**
 * Abstract implementation of {@link OutputWriter} that enables banned words removal. In order to pass in the directory
 * that should be used one should pass <code>-DprofanityDict=location</code> parameter.
 * 
 * @author sematext, http://www.sematext.com/
 */
public abstract class AbstractProfanityRemovingOutputWriter implements OutputWriter {
  private static Logger LOG = Logger.getLogger(AbstractProfanityRemovingOutputWriter.class);
  public static final String PROFANITY_DICT_PARAM = "profanityDict";
  public static final Version LUCENE_VERSION = Version.LUCENE_4_10_4;
  public static final String LUCENE_FIELD_NAME = "phrase";
  private final Analyzer analyzer = new StandardAnalyzer();
  private Set<String> bannedWords;
  private Directory directory;
  private DirectoryReader directoryReader = null;
  private IndexSearcher indexSearcher = null;

  /**
   * Default constructor.
   */
  protected AbstractProfanityRemovingOutputWriter() {
    this(System.getProperty(PROFANITY_DICT_PARAM));
  }

  /**
   * Constructor.
   * 
   * @param dictFilePath
   *          dictionary file path
   */
  protected AbstractProfanityRemovingOutputWriter(String dictFilePath) {
    directory = new RAMDirectory();
    initializeProfanitySet(dictFilePath);
    if (dictFilePath != null && bannedWords != null && !bannedWords.isEmpty()) {
      try {
        directoryReader = DirectoryReader.open(directory);
        indexSearcher = new IndexSearcher(directoryReader);
      } catch (Exception ex) {
        LOG.error("Error creating directory reader or searcher", ex);
      }
    }
  }

  /**
   * Initializes profanity set.
   * 
   * @param dictFilePath
   *          dictionary file path
   */
  private void initializeProfanitySet(String dictFilePath) {
    if (dictFilePath != null) {
      File file = new File(dictFilePath);
      if (file.exists() && file.isFile()) {
        try {
          IndexWriterConfig config = new IndexWriterConfig(LUCENE_VERSION, analyzer);
          IndexWriter indexWriter = new IndexWriter(directory, config);
          BufferedReader reader = new BufferedReader(new FileReader(file));
          Set<String> bannedWords = new HashSet<String>();
          String line = null;
          while ((line = reader.readLine()) != null) {
            bannedWords.add(line.trim());
            Document doc = new Document();
            doc.add(new StringField(LUCENE_FIELD_NAME, line, Store.NO));
            indexWriter.addDocument(doc);
          }
          this.bannedWords = bannedWords;
          indexWriter.close();
          reader.close();
        } catch (Exception ex) {
          LOG.error("Error reading file", ex);
        }
      }
    }
  }

  /**
   * Checks if the given phrase is banned.
   * 
   * @param phrase
   *          phrase to check
   * @return <code>true</code> if phrase is banned
   */
  public boolean isPhraseBanned(String phrase) {
    if (indexSearcher == null) {
      return false;
    }
    if (isExactPhraseBanned(phrase) || isFuzzyPhraseBanned(phrase)) {
      return true;
    }
    return false;
  }

  /**
   * Checks if the given phrase is banned with exact matching.
   * 
   * @param phrase
   *          phrase to check
   * @return <code>true</code> if phrase is banned
   */
  public boolean isExactPhraseBanned(String phrase) {
    if (phrase != null && bannedWords != null) {
      String[] parts = phrase.split(" ");
      if (parts != null) {
        for (String part : parts) {
          if (bannedWords.contains(part)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Checks if the given phrase is banned with fuzzy matching.
   * 
   * @param phrase
   *          phrase to check
   * @return <code>true</code> if phrase is banned
   */
  public boolean isFuzzyPhraseBanned(String phrase) {
    if (phrase != null) {
      String[] parts = phrase.split(" ");
      if (parts != null) {
        for (String part : parts) {
          FuzzyQuery query = new FuzzyQuery(new Term(LUCENE_FIELD_NAME, part), 2);
          try {
            ScoreDoc[] hits = indexSearcher.search(query, null, 1).scoreDocs;
            if (hits.length > 0) {
              return true;
            }
          } catch (Exception ex) {
            LOG.error("Error running query", ex);
          }
        }
      }
    }
    return false;
  }

  /**
   * (non-Javadoc)
   * 
   * @see java.lang.Object#finalize()
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    directoryReader.close();
    directory.close();
  }
}
