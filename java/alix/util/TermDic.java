package alix.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import alix.fr.Lexik;

/**
 * A specialized table for a dictionary of terms, with an int code, an int counter, and an int tag. 
 * Conceived for performances, for linguistic usage, but the object do not requires lexical informations.
 * The int code is a way to optimize complex pattern storage (ex: wordgrams).
 * Access possible by a String term (internal HashMap), or by an int code (internal array, for optimized vector).
 * There are useful methods to get term list in inverse frequency order.
 * 
 * Each term added will create an entry or increment a counter if already
 * exists. Int code, generated by an internal autoincrement pointer.
 * It’s a grow only object, entries can’t be removed.
 * There’s no method to put a term with a free code, terms are never replaced and
 * keep their code. Codes are kept consistent during all life of Object.
 * 
 * 
 * — TODO implement saving on SQL backend
 * — TODO dictionary merges
 * — TODO optimize double access ?
 * 
 * code->String->[code, counter, tag], access by code need another jump to get the tag.
 * 
 * A String can keep an int tag, set at creation of the entry but is not modified.
 * This could be used to filter dictionary outputs
 * (ex: just substantives, only nouns) with no more lexical informations about the Strings stored.
 * But be careful, this tag is not used as a component for the key.
 * This dictionary do not distinguish homographs by gramcat (same String key, different int tag).
 * The experience shows that a tag is usually not sufficient to distinguish linguistic homography
 * (ex: suis<VERB,être>, suis<VERB,suivre>.). 
 * An int field is not enough to ensure disambiguation between homographs, 
 * better approach is to forge String keys outside from this dictionary (ex: suis,être ; suis,suivre)
 * 
 * @author glorieux-f
 *
 */
public class TermDic
{
  /** 
   * HashMap to find by String. String is the best object for key (most common, not mutable).
   * A custom object (like Term) will not match against String.
   * The int array contains an int key and a count  
   */
  private HashMap<String, DicEntry> byTerm = new HashMap<String, DicEntry>();
  /** List of terms, kept in index order, to get a term by int code */
  private DicEntry[] byCode = new DicEntry[32];
  /** Pointer in the array of terms, only growing when terms are added, used as code */
  private int pointer;
  /** Count of all occurrences for this dico */
  private long occs;
  /** The separator for CSV export and import */
  private static char SEP = ';';

  /**
   * A term record in the dictionary, obtained by String or by code
   * @author glorieux-f
   */
  public class DicEntry implements Comparable<DicEntry>
  {
    /** The String form of Term */ 
    private final String label;
    /** Internal code for the term */ 
    private final int code;
    /** A tag ex gram cat */
    private final int tag;
    /** A counter */
    private int count;
    /** A secondary counter (for comparisons) */
    private int count2;
    private DicEntry( String label, int code, int tag, int count, int count2 )
    {
      this.label = label;
      this.code = code;
      this.tag = tag;
      this.count = count;
      this.count2 = count2;
    }
    public String label() { return label; }
    public int code() { return code; }
    public int tag() { return tag; }
    public int count() { return count; }
    public int count2() { return count2; }
    /**
     * Is used for debug, is not a save method
     */
    @Override
    public String toString()
    {
      StringBuffer sb = new StringBuffer();
      sb
        .append( label )
        .append( SEP )
        .append( count )
        .append( SEP )
        .append( code )
        .append( SEP )
        .append( tag )
        .append( SEP )
        .append( count2 )
      ;
      return sb.toString();
    }
    @Override
    /**
     * Default comparator for term informations, 
     */
    public int compareTo( DicEntry o )
    {
      return ( o.count + o.count2) - ( count + count2 );
    }    
  }
  /**
   * Constructor
   */
  public TermDic()
  {
  }
  /**
   * Open a dictionary with a list of terms
   * @param terms
   */
  public TermDic( final String[] terms )
  {
    for (String term: terms) add( term, 0, 1, 0 );
  }
  /**
   * For object reuse, and not too much memory reallocation, reset counters (objects are kept)
   */
  public void reset()
  {
    occs=0;
    pointer = 0;
    byTerm.clear();
  }
  
  /**
   * Get a term by code
   * 
   * @param index
   * @return the term
   */
  public String term( final int code )
  {
    if (code < 1) return null;
    if (code > pointer) return null;
    return byCode[code].label;
  }

  /**
   * Get the code of a term, -1 if not found
   * 
   * @param term
   * @return the key
   */
  public int code( Term term )
  {
    DicEntry vals = byTerm.get( term );
    if (vals == null) return -1;
    return vals.code;
  }
  /**
   * Get the code of a term, -1 if not found
   * 
   * @param term
   * @return the key
   */
  public int code( String term )
  {
    DicEntry line = byTerm.get( term );
    if ( line == null ) return -1;
    return line.code;
  }
  /**
   * Get the tag of a term, 0 if not found
   * 
   * @param term
   * @return the key
   */
  public int tag( Term term )
  {
    DicEntry line = byTerm.get( term );
    if ( line == null ) return 0;
    return line.tag;
  }
  /**
   * Get the code of a term, 0 if not found
   * 
   * @param term
   * @return the key
   */
  public int tag( String term )
  {
    DicEntry line = byTerm.get( term );
    if ( line == null ) return 0;
    return line.tag;
  }
  /**
   * Get the tag of a term by code, 0 if not found
   * 
   * @param code
   *          a term index
   * @return the state of counter after increments
   */
  public int tag( int code )
  {
    DicEntry line =  byCode[code];
    if ( line == null ) return 0;
    return line.tag;
  }

  /**
   * Get the count for a term by String, -1 if not found
   * 
   * @param term
   * @return the count of occurrences
   */
  public int count( String term )
  {
    DicEntry line = byTerm.get( term );
    if ( line == null) return -1;
    return line.count;
  }
  
  /**
   * Get the count for a term by Term (a mutable String), -1 if not found
   * 
   * @param term
   * @return the count of occurrences
   */
  public int count( Term term )
  {
    DicEntry line = byTerm.get( term );
    if ( line == null ) return -1;
    return line.count;
  }


  /**
   * Get the count of a term by code, -1 if not found
   * 
   * @param code
   *          a term index
   * @return the state of counter after increments
   */
  public int count( int code )
  {
    DicEntry line = byTerm.get( byCode[code] );
    if ( line  == null ) return -1;
    return line.count;
  }
  /**
   * Create a term with no tag, set its count at 1, or increment if exists
   * 
   * @param term a word
   * @return the code of term
   */
  public int add( final String term )
  {
    return add( term, 0, 1, 0 );
  }

  /**
   * Create a term with no tag, set its count at 1, or increment if exists
   * 
   * @param term a word
   * @return the code of term
   */
  public int inc( final String term )
  {
    return add( term, 0, 1, 0 );
  }
  /**
   * Create a term with no tag (by a Term object), set its count at 1, or increment if exists
   * 
   * @param term a word
   * @return the code of term
   */
  public int add( final Term term )
  {
    return add( term, 0, 1, 0 );
  }
  /**
   * Create a term with no tag (by a Term object), set its count at 1, or increment if exists
   * 
   * @param term a word
   * @return the code of term
   */
  public int inc( final Term term )
  {
    return add( term, 0, 1, 0 );
  }
  /**
   * Increment second counter for a term (by a String object), or create it if not exists
   * 
   * @param term a word
   * @return the code of term
   */
  public int inc2( final String term )
  {
    return add( term, 0, 0, 1);
  }

  /**
   * Increment second counter for a term (by a Term object), or create it if not exists
   * 
   * @param term a word
   * @return the code of term
   */
  public int inc2( final Term term )
  {
    return add( term, 0, 0, 1);
  }

  /**
   * Create a term with a tag, set its count at 1, or increment if exists.
   * 
   * @param term a word
   * @param tag a category code
   * @return the code of term
   */
  public int inc( final String term, final int tag )
  {
    return add( term, tag, 1, 0 );
  }
  /**
   * Create a term with no tag (by a Term object), set its count at 1, or increment if exists.
   * 
   * @param term a word
   * @param tag a category code
   * @return the code of term
   */
  public int inc( final Term term, final int tag )
  {
    return add( term, tag, 1, 0 );
  }
  /**
   * Increment second counter for a term (by a String object), or create it if not exists
   * 
   * @param term a word
   * @return the code of term
   */
  public int inc2( final String term, final int tag )
  {
    return add( term, tag, 0, 1);
  }

  /**
   * Increment second counter for a term (by a Term object), or create it if not exists
   * 
   * @param term a word
   * @return the code of term
   */
  public int inc2( final Term term, final int tag )
  {
    return add( term, tag, 0, 1);
  }

  /**
   * Increment counter for a term (with a String object) of a specified amount, or create it if not exists
   * 
   * @param term a word
   * @param amount add to counter
   * @return the code of term
   */
  public int add( final String term, final int amount)
  {
    return add( term, 0, amount, 0 );
  }

  /**
   * Increment counter for a term (with a String object) of a specified amount, or create it if not exists
   * 
   * @param term a word
   * @param amount add to counter
   * @return the code of term
   */
  public int add( final Term term, final int amount)
  {
    return add( term, 0, amount, 0 );
  }

  /**
   * Increment counter for a term (with a String object) of a specified amount, or create it if not exists
   * 
   * @param term a word
   * @param amount add to counter
   * @return the code of term
   */
  public int add( final String term, final int tag, final int amount)
  {
    return add( term, tag, amount, 0 );
  }

  /**
   * Increment counter for a term (with a String object) of a specified amount, or create it if not exists
   * 
   * @param term a word
   * @param amount add to counter
   * @return the code of term
   */
  public int add( final Term term, final int tag, final int amount)
  {
    return add( term, tag, amount, 0 );
  }
  

  /**
   * Add a term occurrence, increment if exist or create entry
   * @param term
   * @param count1 for first counter
   * @param count2 for second counter
   * @return code (old or new)
   */
  public int add( final String term, final int tag, final int amount1, final int amount2 )
  {
    DicEntry line = byTerm.get( term );
    if ( line == null ) return put( term, tag, amount1, amount2);
    // repeated code with add(Term, …)
    occs += amount1;
    occs += amount2;
    line.count += amount1;
    line.count2 += amount2;
    return line.code;
  }
  
  /**
   * Add a term occurrence, increment if exist or create entry, 
   * does not create a String object for increment, 
   * except if the entry is created.
   * 
   * @param term
   * @param count1 first count
   * @param count2 second counter
   * @return code (old or new)
   */
  public int add( final Term term, final int tag, final int amount, final int amount2 )
  {
    DicEntry line = byTerm.get( term );
    if ( line == null ) return put( term.toString(), tag, amount, amount2);
    // repeated code with add(String, …)
    occs += amount;
    occs += amount2;
    line.count += amount;
    line.count2 += amount2;
    return line.code;
  }
  /**
   * Create a term if not exists, but do not modify counts.
   * @param term
   * @return index code
   */
  public int put( final String term )
  {
    return add( term, 0, 0, 0 );
  }
  /**
   * Create a term if not exists, but do not modify counts
   * @param term
   * @return index code
   */
  public int put( final Term term )
  {
    return add( term, 0, 0, 0 );
  }
  /**
   * Create a term in the different data structures 
   * @param term
   * @param tag An int tag for output filtering
   * @param count initial value for counter  
   * @param count2 initial value for secondary counter
   * @return 
   */
  private int put ( final String term, final int tag, final int amount, final int amount2 )
  {
    pointer++;
    // index is too short, extends it (not a big perf pb)
    if (pointer >= byCode.length) {
      final int oldLength = byCode.length;
      final DicEntry[] oldData = byCode;
      byCode = new DicEntry[Calcul.nextSquare( oldLength )];
      System.arraycopy( oldData, 0, byCode, 0, oldLength );
    }
    DicEntry line = new DicEntry( term, pointer, tag, amount, amount2 );
    // put the same line object by reference in HashMap and Array
    byTerm.put( term, line );
    byCode[pointer] = line;
    return pointer;
  }
  
  /**
   * Size of the dictionary
   */
  public int size()
  {
    return pointer;
  }

  /**
   * Sum of all counts
   */
  public long occs()
  {
    return occs;
  }
  /**
   * Increment occurrences count with not stored terms (useful for filtered dictionary)
   */
  public TermDic inc( )
  {
    occs ++;
    return this;
  }
  /**
   * Increment occurrences count with not stored terms (useful for filtered dictionary)
   */
  public TermDic inc( int i )
  {
    occs += i;
    return this;
  }

  /**
   * Return an iterable object to get freqlist
   * @return
   */
  public List<DicEntry> byCount() {
    List<DicEntry> list = new ArrayList<DicEntry>( byTerm.values() ); // will copy entries (?)
    Collections.sort( list );
    return list;
  }

  public Collection<DicEntry> entries() {
    return byTerm.values();
  }

  /**
   * To save the dictionary, with some index consistency but… will not works on
   * merge
   * 
   * @param file
   * @throws NumberFormatException
   * @throws IOException
   */
  public void csv( Path path ) throws IOException
  {
    BufferedWriter writer = Files.newBufferedWriter( path, Charset.forName( "UTF-8" ) );
    csv( writer );
  }

  /**
   * Send a CSV version of the dictionary
   * 
   * @return a CSV string
   */
  public String csv()
  {
    return csv( -1 );
  }

  /**
   * Send a CSV version of the dictionary
   * 
   * @return a CSV string
   */
  public String csv( int limit )
  {
    return csv( limit );
  }

  /**
   * Send a CSV version of the dictionary
   * 
   * @return a CSV string
   */
  /*
  public String csv( int size, Set<String> stoplist )
  {
    String ret = null;
    try {
      ret = csv( new StringWriter(), size, stoplist ).toString();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return ret;
  }
  */

  /**
   * Give a csv view of all dictionary
   * 
   * @throws IOException
   */
  public Writer csv( Writer writer ) throws IOException
  {
    return csv( writer, -1 );
  }

  /**
   * Save datas 
   * @param writer
   * @return
   */
  public TermDic save( Writer writer )
  {
    return this;
  }
  
  /**
   * Give a csv view of all dictionary
   * TODO a top filter
   * @throws IOException
   */
  public Writer csv( Writer writer, int limit ) throws IOException
  {
    // TODO total occs
    writer.write( "("+occs+")"+SEP+"COUNT"+SEP+"CODE"+SEP+"TAG"+SEP+"COUNT2"+SEP+"PPM"+"\n" );
    try {
      for (DicEntry entry: byCount()) {
        if (limit-- == 0)
          break;
        writer.write( entry.toString() );
        writer.write( SEP );
        writer.write( ""+(double)Math.round( 100000000.0*entry.count/occs )/100 );
        writer.write( "\n" );
      }
    } finally {
      writer.close();
    }
    return writer;
  }

  /**
   * DEPRECATED, use a CompDic nstead. 
   * Give a csv view of all dictionary
   * 
   * @throws IOException
   */
  /*
  public Writer csvcomp( Writer writer, int limit, Set<Term> stoplist ) throws IOException
  {
    String[] byCount = byCount();
    int length = byCount.length;
    int count1;
    int count2;
    try {
      for (int i = 0; i < length; i++) {
        if (stoplist != null && stoplist.contains( byCount[i] ))
          continue;
        if (limit-- == 0)
          break;
        Terminfos values = byTerm.get( byCount[i] );
        count1 = values.count;
        count2 = values.count2;
        writer.write( 
                byCount[i]
          +"\t"+ count1
          +"\t"+ count2
          +"\t"+ (count1+count2)
          +"\t"+NumberFormat.getInstance().format( 1.0*count1 / (count1+count2) )
          // +"\t"+index(byCount[i])
        +"\n" );
      }
    } finally {
      writer.close();
    }
    return writer;
  }
  */

  /**
   * Is used for debug, is not a save method
   */
  @Override
  public String toString()
  {
    return csv( 10 );
  }

  /**
   * Load a freqlist from csv TODO test it
   * 
   * @param file
   * @throws IOException
   * @throws NumberFormatException
   */
  public void load( Path path ) throws IOException
  {
    BufferedReader reader = Files.newBufferedReader( path, Charset.forName( "UTF-8" ) );
    String line = null;
    int value;
    try {
      // pass first line
      line = reader.readLine();
      while ((line = reader.readLine()) != null) {
        if (line.contains( "\t" )) {
          String[] strings = line.split( "\t" );
          try {
            value = Integer.parseInt( strings[1] );
          } catch (NumberFormatException e) {
            continue;
          }
          add( strings[0].trim(), value );
        }
        else {
          inc( line.trim() );
        }
      }
    } finally {
      reader.close();
    }
  }

  /**
   * Testing
   * 
   * @throws IOException
   */
  public static void main( String[] args ) throws IOException
  {
    BufferedReader buf = new BufferedReader(
      new InputStreamReader( Lexik.class.getResourceAsStream(  "dic/loc.csv" ), StandardCharsets.UTF_8 )
    );
    String l;
    TermDic dic = new TermDic();
    while ((l = buf.readLine()) != null) {
      if ( l.isEmpty() ) continue;
      for ( String s: l.split( "[ ;]+" ) ) {
        dic.inc( s );
      }
    }
    dic.csv( new PrintWriter(System.out) );
  }
}
/*
Ideas…
https://en.wikipedia.org/wiki/Bayes_estimator#Practical_example_of_Bayes_estimators
*/

