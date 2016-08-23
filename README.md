# Related Searches

Given a user query Related Searches returns a list of, you guessed it - related searches, much like related searches you see on Google search results pages.

The related searches are derived from query logs structured like the infamous AOL search data leak - [AOL search data leak](https://en.wikipedia.org/wiki/). See sample further below.

Relatedness is based on query similarity, as well as click data stored in the query logs.

## Getting Started

To run Related Searches you will need to clone the repository and 

### Requirements

You need to have a recent version of Java installed, Apache Maven to build the jar files and [Redis](http://redis.io) to help with data processing.  

#### Redis role ####

Redis is used during calculation as a fast key-value store. 

#### Elasticsearch role ####

Elasticsearch is one of data stores related searches can be written to.

### Building from Source

Related Searches uses [Maven](https://maven.apache.org) for its build system. You'll need to have a modern version of Maven installed, any version from the 3.x branch should work.

To create a distribution, simply run `maven clean install` command in the cloned directory. The runnable jar files will be created in the @target@ directory and will be called: `BasicRelatedSearches.jar` and `TimeAndClickRelatedSearches.jar`.

### Running 

To run Related Searches you need to have Redis running. Once you build Related Searches you have two options:

#### Basic Related Searches

The most basic related searches calculation, which doesn't take click count into consideration. To run it and generate file output one has to run the following command:

`java -jar BasicRelatedSearches.jar REDIS_HOST REDIS_PORT TIME_BETWEEN_QUERIES_IN_SECONDS SUGGESTION_THRESHOLD QUERY_SIMILARITY_ACCEPTANCE_THRESHOLD QUERY_SIMILARITY_MORE_SIMILAR QUERIES_FILE OUTPUT_FILE $INDEX_NAME > /dev/null 2>&1`

To run basic related searches calculation and get the result in Elasticsearch index, one should run the following command:

`java -jar -DuseElasticSearch=true BasicRelatedSearches.jar REDIS_HOST REDIS_PORT TIME_BETWEEN_QUERIES_IN_SECONDS SUGGESTION_THRESHOLD QUERY_SIMILARITY_ACCEPTANCE_THRESHOLD QUERY_SIMILARITY_MORE_SIMILAR QUERIES_FILE ES_HOST INDEX_NAME > /dev/null 2>&1`

And the parameters are:
- REDIS_HOST - Redis host, e.g., _localhost_
- REDIS_PORT - Redis port, e.g., _6379_
- TIME_BETWEEN_QUERIES_IN_SECONDS - maximum time between queries in seconds, e.g., _20_
- SUGGESTION_THRESHOLD - suggestion threshold, e.g., _0.2_
- QUERY_SIMILARITY_ACCEPTANCE_THRESHOLD - query similarity threshold, e.g., _0.9_
- QUERY_SIMILARITY_MORE_SIMILAR - boolean, set to _true_ will result in suggesting similar queries according to the given _QUERY_SIMILARITY_ACCEPTANCE_THRESHOLD_
- QUERIES_FILE - queries log file
- OUTPUT_FILE - output file with results
- ES_HOST - Elasticsearch address
- INDEX_NAME - Elasticsearch index name

#### Time and Click-based Related Searches

To run related searches calculation that takes into consideration time and click information from the query log and output the results to file, one has to run the following command:

`java -jar TimeAndClickRelatedSearches.jar REDIS_HOST REDIS_PORT TIME_BETWEEN_QUERIES_IN_SECONDS SUGGESTION_THRESHOLD QUERY_SIMILARITY_ACCEPTANCE_THRESHOLD QUERY_SIMILARITY_MORE_SIMILAR TEXT_SIMILARITY_BOOST TIME_AND_CLICK_BOOST SUGGEST_NON_ZERO_HITS_ONLY QUERIES_FILE OUTPUT_FILE > /dev/null 2>&1`

To run time and click using related searches calculation and get the result in Elasticsearch index, one should run the following command:

`java -jar -DuseElasticSearch=true TimeAndClickRelatedSearches.jar REDIS_HOST REDIS_PORT TIME_BETWEEN_QUERIES_IN_SECONDS SUGGESTION_THRESHOLD QUERY_SIMILARITY_ACCEPTANCE_THRESHOLD QUERY_SIMILARITY_MORE_SIMILAR TEXT_SIMILARITY_BOOST TIME_AND_CLICK_BOOST SUGGEST_NON_ZERO_HITS_ONLY QUERIES_FILE ES_HOST ES_INDEX_NAME > /dev/null 2>&1`

And the parameters are:
- REDIS_HOST - Redis host, e.g., _localhost_
- REDIS_PORT - Redis port, e.g., _6379_
- TIME_BETWEEN_QUERIES_IN_SECONDS - maximum time between queries in seconds, e.g., _20_ 
- SUGGESTION_THRESHOLD - suggestion threshold, e.g., _0.2_
- QUERY_SIMILARITY_ACCEPTANCE_THRESHOLD - query similarity threshold, e.g., _0.9_
- QUERY_SIMILARITY_MORE_SIMILAR - boolean, set to _true_ will result in suggesting similar queries according to the given _QUERY_SIMILARITY_ACCEPTANCE_THRESHOLD_
- TEXT_SIMILARITY_BOOST - boost related to text similarity, e.g., _0.2_
- TIME_AND_CLICK_BOOST - boost related to clicks, e.g., _2.0_
- SUGGEST_NON_ZERO_HITS_ONLY - boolean value, when set to _true_ only non-zero hits queries will be suggested
- QUERIES_FILE - queries log file
- OUTPUT_FILE - output file with results
- ES_HOST - Elasticsearch address
- ES_INDEX_NAME - Elasticsearch index name

#### Query Log Format

Related Searches expects a given log format out of the box, the popular [AOL](https://en.wikipedia.org/wiki/AOL_search_data_leak) log file format. Each query log line contained tabulate separated entries:
- session identifier
- query 
- query time
- click information (optional)
- was search successful (optional)

For example:
```
12345	sematext training	Fri, 19 Aug 2016 13:30:00 GMT
12345	sematext solr training	Fri, 19 Aug 2016 13:31:00 GMT
56789	sematext elasticsearch	Fri, 19 Aug 2016 13:35:00 GMT	1	true	
56789	sematext training	Fri, 19 Aug 2016 13:35:20 GMT	0	false
```

# License

```
This software is licensed under the Apache License, version 2 ("ALv2"), quoted below.

Copyright 2009-2016 Sematext Group, Inc <http://sematext.com>

Licensed under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy of
the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations under
the License.
```
