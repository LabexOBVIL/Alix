/*
 * Alix, A Lucene Indexer for XML documents.
 * 
 * Copyright 2009 Pierre DITTGEN <pierre@dittgen.org> 
 *                Frédéric Glorieux <frederic.glorieux@fictif.org>
 * Copyright 2016 Frédéric Glorieux <frederic.glorieux@fictif.org>
 *
 * Alix is a java library to index and search XML text documents
 * with Lucene https://lucene.apache.org/core/
 * including linguistic expertness for French,
 * available under Apache licence.
 * 
 * Alix has been started in 2009 under the javacrim project
 * https://sf.net/projects/javacrim/
 * for a java course at Inalco  http://www.er-tim.fr/
 * Alix continues the concepts of SDX under another licence
 * «Système de Documentation XML»
 * 2000-2010  Ministère de la culture et de la communication (France), AJLSM.
 * http://savannah.nongnu.org/projects/sdx/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package alix.lucene.search;

/**
 * Interface for a scorer, similar to tf-idf.
 * @author fred
 *
 */
public abstract class Scorer
{
  /** Total count of occurrences in the collection. */
  protected long occsAll;
  /** Total count of documents in the collection. */
  protected int docsAll;
  /** Average occ length */
  protected double docAvg;
  

  /**
   * Set colletion stats, not modified by queries.
   * @param occsAll Total count of occurrences in the collection.
   * @param docsAll Total count of documents in the collection.
   */
  public void setAll(final long occsAll, final int docsAll) {
    this.occsAll = occsAll;
    this.docsAll = docsAll;
    this.docAvg = (double) occsAll / docsAll;
  }
  
  public long occsAll() {
    return occsAll;
  }
  public int docsAll() {
    return docsAll;
  }
  /**
   * Set collection level variables for a query.
   * like the idf (independant document frequency) in the tf-idf
   * 
   * @param occsMatch Count of occurrences in a corpus containing a term.
   * @param docsMatch Count of documents in a corpus containing a term.
   */
  abstract public void weight(final long occsMatch, final int docsMatch);

  /**
   * Get a score for an item in a collection (ex: a document in a corpus)
   * like the tf (Term Frequency) in the tf-idf
   * 
   * @param occsDoc Count of matching occurrences in this document.
   * @param docLen Total count of occurrences for this document.
   * @return
   */
  abstract public double score(final int occsDoc, final long docLen);

}
