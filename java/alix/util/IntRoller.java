package alix.util;

/**
 * Efficient Object to handle a sliding window of ints.
 * 
 * 
 * @author glorieux-f
 *
 * @param <T>
 */
public class IntRoller extends Roller
{
  /** Data of the sliding window */
  private int[] data;

  /**
   * Constructor, init data
   */
  public IntRoller(final int left, final int right) {
    super(left, right);
    data = new int[size];
  }

  /**
   * Return a value for a position, positive or negative, relative to center
   * 
   * @param ord
   * @return the primary value
   */
  public int get(final int pos)
  {
    return data[pointer(pos)];
  }

  /**
   * Return first value
   * 
   * @return
   */
  public int first()
  {
    return data[pointer(right)];
  }

  /**
   * Return last value
   * 
   * @return
   */
  public int last()
  {
    return data[pointer(left)];
  }

  /**
   * Set value at position
   * 
   * @param ord
   * @return the primary value
   */
  public IntRoller set(final int pos, final int value)
  {
    int index = pointer(pos);
    // int old = data[ index ];
    data[index] = value;
    return this;
  }

  /**
   * Add a value at front
   */
  public IntRoller push(final int value)
  {
    // int ret = data[ pointer( -left ) ];
    center = pointer(+1);
    data[pointer(right)] = value;
    return this;
  }

  /**
   * Clear all value
   * 
   * @return
   */
  public IntRoller clear()
  {
    int length = data.length;
    for (int i = 0; i < length; i++) {
      data[i] = 0;
    }
    return this;
  }

  /**
   * Increment all values
   * 
   * @return
   */
  public IntRoller inc()
  {
    for (int i = 0; i < size; i++) {
      data[i]++;
    }
    return this;
  }

  /**
   * Decrement all values
   * 
   * @return
   */
  public IntRoller dec()
  {
    for (int i = 0; i < size; i++) {
      data[i]--;
    }
    return this;
  }

  @Override
  public int hashCode()
  {
    // no cache for hash, such roller change a lot
    int res = 17;
    for (int i = left; i <= right; i++) {
      res = 31 * res + get(i);
    }
    return res;
  }

  @Override
  public boolean equals(Object o)
  {
    if (o == null)
      return false;
    if (o == this)
      return true;
    if (o instanceof IntPair) {
      IntPair pair = (IntPair) o;
      if (size != 2)
        return false;
      if (get(0) != pair.x)
        return false;
      if (get(1) != pair.y)
        return false;
      return true;
    }
    if (o instanceof IntList) {
      IntList list = (IntList) o;
      if (list.size() != size)
        return false;
      int ilist = list.size() - 1;
      int i = right;
      do {
        if (get(i) != list.get(ilist))
          return false;
        i--;
        ilist--;
      }
      while (ilist >= 0);
      return true;
    }
    if (o instanceof IntRoller) {
      IntRoller roller = (IntRoller) o;
      if (roller.size != size)
        return false;
      int pos1 = left;
      int pos2 = roller.left;
      int max1 = right;
      while (pos1 <= max1) {
        if (get(pos1) != roller.get(pos2))
          return false;
        pos1++;
        pos2++;
      }
      return true;
    }
    return false;
  }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    for (int i = left; i <= right; i++) {
      if (i == 0)
        sb.append(" <");
      sb.append(get(i));
      if (i == 0)
        sb.append("> ");
      else if (i == right)
        ;
      else if (i == -1)
        ;
      else
        sb.append(" ");
    }
    return sb.toString();
  }

  public String toString(DicFreq words)
  {
    return toString(words, left, right);
  }

  public String toString(DicFreq words, int from, int to)
  {
    StringBuilder sb = new StringBuilder();
    for (int i = from; i <= to; i++) {
      int val = get(i);
      if (val != 0)
        sb.append(words.label(val)).append(" ");
    }
    return sb.toString();
  }

}
