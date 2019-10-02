package alix.lucene.search;

import java.text.CollationKey;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefHash;
import org.apache.lucene.util.UnicodeUtil;

import alix.lucene.analysis.CharsAtt;

/**
 * An iterator for sorted list of terms
 * 
 * @author fred
 *
 */
public class TopTerms
{
  /** Dictionary of the terms */
  private final BytesRefHash hashDic;
  /** Count of terms */
  private final int size;
  /** An optional field by termId used to sort by score */
  private double[] scores;
  /** An optional field by termId, total count of words */
  private long[] lengths;
  /** Total count of documents in the collection. */
  protected int docsAll;
  /** An optional field by termId, total of relevant docs b */
  protected int[] docs;
  /** An optional field by termId, relevant  docs b */
  protected int[] hits;
  /** Total count of occurrences in the collection. */
  protected long occsAll;
  /** An optional field by termId, count of matching occurences */
  protected int[] occs;
  /** An optional field by termId, docid used as a cover for a term like a title or an author */
  protected int[] covers;
  /** An optional field by termId, index of term in a series, like a sorted query */
  protected int[] nos;
  /** Current term id to get infos on */
  private int termId;
  /** Cursor, to iterate in the sorter */
  private int cursor = -1;
  /** An array for sorting */
  private Entry[] sorter;
  /** Bytes to copy current term */
  private final BytesRef ref = new BytesRef();

  /** Super constructor, needs a dictionary */
  public TopTerms(final BytesRefHash hashDic)
  {
    this.hashDic = hashDic;
    this.size = hashDic.size();
  }
  
  /**
   * Get size of the dictionary.
   * @return
   */
  public int size()
  {
    return size;
  }

  /**
   * Set global stats.
   * @param occsAll Total count of occurrences in the collection.
   * @param docsAll Total count of documents in the collection.
   */
  public void setAll(final long occsAll, final int docsAll) 
  {
    this.occsAll = occsAll;
    this.docsAll = docsAll;
  }

  /**
   * Prepare sorting
   */
  private Entry[] sorter() 
  {
    Entry[] sorter = this.sorter; // localize
    // new array
    if (sorter == null) {
      sorter = new Entry[size];
      for (int i = 0, max = size; i < max; i++) {
        sorter[i] = new Entry(i);
      }
    }
    // resort
    else {
      Arrays.sort(sorter,  new Comparator<Entry>() {
          @Override
          public int compare(Entry arg0, Entry arg1)
          {
            return Integer.compare(arg0.termId, arg1.termId);
          }
        }
      );
    }
    return sorter;
  }
  
  /**
   * An entry used in a sorter array.
   */
  class Entry
  {
    final int termId;
    double rank;
    CollationKey key;

    public Entry(final int termId)
    {
      this.termId = termId;
    }

    /*
    Double.compare(((EntryScore) o).score, score);
    key.compareTo(((EntryString) o).key);
     */
    @Override
    public String toString()
    {
      BytesRef ref = new BytesRef();
      hashDic.get(termId, ref);
      return termId + ". " + ref.utf8ToString() + " (" + rank + ")";
    }
  }

  /**
   * Sort the terms by value
   */
  public void sort()
  {
    Entry[] sorter = sorter();
    Collator collator = Collator.getInstance(Locale.FRANCE);
    collator.setStrength(Collator.TERTIARY);
    collator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
    for (int i = 0, max = size; i < max; i++) {
      hashDic.get(i, ref);
      sorter[i].key = collator.getCollationKey(ref.utf8ToString());
    }
    Arrays.sort(sorter,  new Comparator<Entry>() {
        @Override
        public int compare(Entry arg0, Entry arg1)
        {
          return arg0.key.compareTo(arg1.key);
        }
      }
    );
    this.sorter = sorter;
    cursor = -1;
  }

  /**
   * Set a long value to each term.
   * @param scores
   */
  public void setScores(final double[] scores)
  {
    this.scores = scores;
  }
  public double[] getScores()
  {
    return scores;
  }
  /**
   * Sort the terms according to a vector of scores by termId.
   * @param scores An array in termId order.
   */
  public void sort(final double[] doubles)
  {
    Entry[] sorter = sorter();
    for (int i = 0, max = size; i < max; i++) {
      sorter[i].rank = doubles[i];
    }
    sort(sorter);
  }
  
  private void sort(Entry[] sorter)
  {
    Arrays.sort(sorter, new Comparator<Entry>() {
        @Override
        public int compare(Entry arg0, Entry arg1)
        {
          return Double.compare(arg1.rank, arg0.rank);
        }
      }
    );
    this.sorter = sorter;
    cursor = -1;
  }

  /**
   * Sort the terms according to a vector of scores by termId.
   * @param scores An array in termId order.
   */
  public void sort(final long[] longs)
  {
    Entry[] sorter = sorter();
    for (int i = 0, max = size; i < max; i++) {
      sorter[i].rank = longs[i];
    }
    sort(sorter);
  }

  /**
   * Sort the terms according to a vector of scores by termId.
   * @param scores An array in termId order.
   */
  public void sort(final int[] ints)
  {
    Entry[] sorter = sorter();
    for (int i = 0, max = size; i < max; i++) {
      sorter[i].rank = ints[i];
    }
    sort(sorter);
  }

  /**
   * Test if a term is in the dictionary.
   * If exists, identifier will be kept to get infos on it.
   * @param term
   * @return
   */
  public boolean contains(String term)
  {
    BytesRef bytes = new BytesRef(term);
    int termId = hashDic.find(bytes);
    if (termId < 0) return false;
    this.termId = termId;
    return true;
  }
  
  /**
   * Reset the internal cursor when iterating in the sorter.
   */
  public void reset()
  {
    cursor = -1;
  }

  /**
   * Test if there is one term more in
   * @return
   */
  public boolean hasNext()
  {
    return (cursor < size - 1);
  }

  public void next()
  {
    // if too far, let's array cry
    cursor++;
    termId = sorter[cursor].termId;
  }

  /**
   * Populate the term with reusable bytes.
   * 
   * @param ref
   */
  public void term(BytesRef bytes)
  {
    hashDic.get(termId, bytes);
  }

  /**
   * Get current term, with reusable chars.
   * 
   * @param term
   * @return
   */
  public CharsAtt term(CharsAtt term)
  {
    hashDic.get(termId, ref);
    // ensure size of the char array
    int length = ref.length;
    char[] chars = term.resizeBuffer(length);
    final int len = UnicodeUtil.UTF8toUTF16(ref.bytes, ref.offset, length, chars);
    term.setLength(len);
    return term;
  }

  /**
   * Get the current term as String.
   * 
   * @return
   */
  public String term()
  {
    hashDic.get(termId, ref);
    return ref.utf8ToString();
  }

  /**
   * Set an optional array of values (in termId order).
   * 
   * @param docs
   */
  public void setDocs(final int[] docs)
  {
    this.docs = docs;
  }
  public int[] getDocs()
  {
    return docs;
  }

  /**
   * Get the total count of documents relevant for thi term.
   * @return
   */
  public int docs()
  {
    return docs[termId];
  }

  /**
   * Set an optional array of values (in termId order).
   * @param hits
   */
  public void setHits(final int[] hits)
  {
    this.hits = hits;
  }

  /**
   * Get the count of matched documents for the current term.
   * @return
   */
  public int hits()
  {
    return hits[termId];
  }

  /**
   * Set an optional array of values (in termId order).
   * 
   * @param weights
   */
  public void setNos(final int[] nos)
  {
    this.nos = nos;
  }

  /**
   * Get the no of the current term.
   * @return
   */
  public int n()
  {
    return nos[termId];
  }

  /**
   * Set an optional array of values (in termId order).
   * 
   * @param lengths
   */
  public void setLengths(final long[] lengths)
  {
    this.lengths = lengths;
  }

  /**
   * Current term, global count of items relevant for this term.
   * 
   * @return
   */
  public long length()
  {
    return lengths[termId];
  }

  /**
   * Set an optional array of values (in termId order), occurrences.
   * 
   * @param occs
   */
  public void setOccs(final int[] occs)
  {
    this.occs = occs;
  }

  public int[] getOccs()
  {
    return occs;
  }

  /**
   * Current term, number of occurrences.
   * 
   * @return
   */
  public long occs()
  {
    return occs[termId];
  }

  /**
   * Set an optional int array of docids in termId order.
   * @param weights
   */
  public void setCovers(final int[] covers)
  {
    this.covers = covers;
  }

  /**
   * Current term, the cover docid.
   * @return
   */
  public int cover()
  {
    return covers[termId];
  }

  /**
   * Get the current score.
   * 
   * @return
   */
  public double score()
  {
    return scores[termId];
  }

  /**
   * Get the current value used for sorting.
   * 
   * @return
   */
  public double rank()
  {
    return sorter[cursor].rank;
  }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    BytesRef ref = new BytesRef();
    int max = Math.min(200, sorter.length);
    for (int i = 0; i < max; i++) {
      int facetId = sorter[i].termId;
      // System.out.println(facetId);
      hashDic.get(facetId, ref);
      sb.append(""+facetId+". "+ref.utf8ToString());
      if (lengths != null) sb.append(":" + lengths[facetId]);
      if (scores != null) sb.append(" (" + scores[facetId] + ")");
      sb.append("\n");
    }
    return sb.toString();
  }
}
