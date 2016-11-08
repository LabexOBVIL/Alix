package alix.fr;

import alix.util.Term;

/**
 * A Token in a text flow with different properties.
 * A token should allow to write a nice concordance.
 * @author glorieux
 *
 */
public class Occ
{
  /** Graphical form like encountered, caps/min, ellisions, could be used for a correct concordancer */
  public Term graph = new Term();
  /** Orthographic form, normalized graphical form */
  public Term orth = new Term();
  /** Grammatical category */
  public Tag tag = new Tag();
  /** Lemma form */
  public Term lem = new Term();
  /** Char index in the source file of the first char of the token */
  public int start = 0;
  /** Char index in the source file  */
  public int end = 0;
  /**
   * Empty constructor
   */
  public Occ()
  {
    
  }
  /**
   * Copy an occurrence
   */
  public Occ( Occ occ )
  {
    replace( occ );
  }
  /**
   * Constructor
   */
  public Occ( final CharSequence graph, final CharSequence orth, final Tag tag, final CharSequence lem)
  {
    graph( graph );
    orth( orth );
    tag( tag );
    lem( lem );
  }
  /**
   * Constructor
   */
  public Occ( final Term graph, final Term orth, final short tag, final Term lem)
  {
    graph( graph );
    orth( orth );
    tag( tag );
    lem( lem );
  }
  /**
   * Replace occurrence values by another
   * @param occ
   * @return a handle on the Occurrence object for chaining
   */
  public Occ replace( Occ occ )
  {
    graph.replace( occ.graph );
    orth.replace( occ.orth );
    tag.code( occ.tag );
    lem.replace( occ.lem );
    start = occ.start;
    end = occ.end;
    return this;
  }
  /**
   * Append an occurrence to make a compound word
   * @return
   */
  public Occ apend( Occ occ )
  {
    char c;
    if ( !graph.isEmpty() ) {
      c = graph.last();
      if ( c != '\'' && c != '’' && c != '-' && occ.graph.first() != '-')
        graph.append( ' ' );
    }
    if ( !orth.isEmpty() ) {
      c = orth.last();
      if ( c != '\'' && c != '’' && c != '-' && occ.orth.first() != '-')
        orth.append( ' ' );
    }
    if ( !lem.isEmpty() ) {
      c = lem.last();
      if ( c != '\'' && c != '’' && c != '-' && occ.orth.first() != '-')
        lem.append( ' ' );
    }
    graph.append( occ.graph );
    orth.append( occ.orth );
    lem.append( occ.lem );
    // no way to guess how cat will be changed
    if ( tag.equals( 0 ) ) tag( occ.tag );
    // if occurrence was empty, take the index value of new Occ
    if ( start < 0 ) start = occ.start;
    end = occ.end;
    return this;
  }
  /**
   * Clear Occurrence of all information
   * @return a handle on the Occurrence object for chaining
   */
  public Occ clear()
  {
    graph.reset();
    orth.reset();
    lem.reset();
    tag.code(0);
    start = -1;
    end = -1;
    return this;
  }
  /**
   * Is Occurrence with at least graph value set ?
   * @return
   */
  public boolean isEmpty()
  {
    return graph.isEmpty();
  }
  
  /**
   * Set graph value by a String (or a mutable String)
   * @param cs
   * @return a handle on the occurrence object
   */
  public Occ graph( CharSequence cs) 
  {
    graph.replace( cs );
    return this;
  }
  /**
   * Set graph value by a String (or a mutable String), with index
   * @param cs
   * @param from start index in the CharSequence
   * @param length number of chars from start
   * @return a handle on the occurrence
   */
  public Occ graph( CharSequence cs, int from, int length) 
  {
    graph.replace( cs, from, length );
    return this;
  }
  /**
   * Set graph value by copy of a term
   * @param t
   * @return a handle on the occurrence
   */
  public Occ graph( Term t) 
  {
    graph.replace( t );
    return this;
  }

  /**
   * Set orth value by a String (or a mutable String)
   * @param cs
   * @return a handle on the occurrence
   */
  public Occ orth( CharSequence cs) 
  {
    orth.replace( cs );
    return this;
  }
  /**
   * Set orth value by a String (or a mutable String), with index
   * @param cs
   * @param from start index in the CharSequence
   * @param length number of chars from start
   * @return a handle on the occurrence object
   */
  public Occ orth( CharSequence cs, int from, int length) 
  {
    orth.replace( cs, from, length );
    return this;
  }
  /**
   * Set orth value by copy of a term
   * @param t
   * @return a handle on the occurrence object
   */
  public Occ orth( Term t) 
  {
    orth.replace( t );
    return this;
  }
  /**
   * Set a grammar category code
   * @return a handle on the occurrence object for chaining
   */
  public Occ tag( final short code )
  {
    tag.code( code );
    return this;
  }
  /**
   * Set a grammar category code
   * @param tag
   * @return
   */
  public Occ tag( final Tag tag )
  {
    tag.code( tag );
    return this;
  }
  /**
   * Set lem value by copy of a term
   * @param t
   * @return a handle on the occurrence object
   */
  public Occ lem( final Term t ) 
  {
    lem.replace( t );
    return this;
  }
  public Occ lem( final CharSequence cs) 
  {
    lem.replace( cs );
    return this;
  }
  /**
   * Set a start pointer for the occurrence
   * @param i pointer, for example a char index in a String
   * @return
   */
  public Occ start(final int i)
  {
    this.start = i;
    return this;
  }
  /**
   * Set an end pointer for the occurrence (last char + 1)
   * @param i pointer, for example a char index in a String
   * @return
   */
  public Occ end(final int i)
  {
    this.end = i;
    return this;
  }
  /** 
   * Default String display 
   */
  public String toString()
  {
    return graph+"\t"+orth+"\t"+tag.label()+"\t"+lem+"\t"+start;
  }
}
