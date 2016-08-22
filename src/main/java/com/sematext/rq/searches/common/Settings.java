/*
 *    Copyright (c) Sematext International
 *    All Rights Reserved
 *
 *    THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Sematext International
 *    The copyright notice above does not evidence any
 *    actual or intended publication of such source code.
 */
package com.sematext.rq.searches.common;

/**
 * Interface for project wide settings.
 *
 * @author sematext, http://www.sematext.com/
 */
public interface Settings {
  /** Default character set. */
  String DEFAULT_CHARSET = "UTF-8";
  /** Path element separator. */
  String PATH_ELEMENT_SEPARATOR = "/";
  /** Decimal format. */
  String DECIMAL_FORMAT = "#.###";
  /** Elasticsearch type name. */
  String ES_TYPE_NAME = "query";
  /** Original query. */
  String ES_QUERY_SECTION = "query";
  /** Related queries suggestions. */
  String ES_RELATED_QUERIES_SECTION = "suggestions";
  /** Related query. */
  String ES_RELATED_QUERY = "related";
  /** Related query score. */
  String ES_RELATED_QUERY_SCORE = "score";
  /** Default date format. */
  String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
  /** Split character. */
  String AOL_LOG_FORMAT_SPLIT_CHARACTER = "\t";
  /** Elasticsearch enabled flag. */
  String ES_OUTPUT_ENABLED = "useElasticSearch";
  /** Redis prefix. */
  String REDIS_PREFIX = "q_";
  /** String distance prefix. */
  public static final String STRING_DISTANCE_PREFIX = "sdq_";
  /** Time click prefix. */
  public static final String TIME_CLICK_PREFIX = "tcq_";
}
