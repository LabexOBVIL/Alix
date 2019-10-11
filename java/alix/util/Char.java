/*
 * Copyright 2008 Pierre DITTGEN <pierre@dittgen.org> 
 *                Frédéric Glorieux <frederic.glorieux@fictif.org>
 * Copyright 2016 Frédéric Glorieux <frederic.glorieux@fictif.org>
 *
 * Alix, A Lucene Indexer for XML documents
 * Alix is a tool to index XML text documents
 * in Lucene https://lucene.apache.org/core/
 * including linguistic expertise for French.
 * Project has been started in 2008 under the javacrim project (sf.net)
 * for a java course at Inalco  http://www.er-tim.fr/
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
package alix.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * Efficient character categorizer, about 500x faster than Character.is*(),
 * optimized for tokenizer in latin scripts. Taken from
 * http://www.tomshut.de/java/index.html
 * 
 * Idea is to populate a big array of properties for the code points. Memory
 * optimization is possible for alphabetic scripts (… or ’ are in 2000-206F
 * block) Chinese is not relevant for such classes but will need a test
 * 
 * For latin script language, apos and dashes are considered as word characters
 * Separation on these chars is lexical specific
 * 
 * @author glorieux-f
 */
public class Char
{
  /** The 2 bytes unicode */
  private static final int SIZE = 65535;
  /** Properties of chars by index */
  private static final short[] CHARS = new short[SIZE + 1];
  /** Is a letter (Unicode property) */
  public static final short LETTER = 0x0001;
  /** Is a space (Unicode property) */
  public static final short SPACE = 0x0002;
  /** Is token, specific, with '-' and ‘'’ */
  public static final short TOKEN = 0x0004;
  /** Punctuation, according to Unicode */
  public static final short PUNCTUATION = 0x0008;
  public static final short PUNCTUATION_OR_SPACE = SPACE | PUNCTUATION;
  public static final short LOWERCASE = 0x0010;
  public static final short UPPERCASE = 0x0020;
  public static final short VOWEL = 0x0040;
  public static final short CONSONNANT = 0x0080;
  public static final short DIGIT = 0x0100;
  public static final short PUNsent = 0x0200;
  public static final short PUNcl = 0x0400;
  public static final short MATH = 0x0800;
  public static final short HIGHSUR = 0x4000;
  public static final short LOWSUR = 0x2000;
  public static final HashMap<String, Character> HTMLENT = new HashMap<String, Character>();
  static {
    BufferedReader buf = new BufferedReader(
        new InputStreamReader(Char.class.getResourceAsStream("htmlent.csv"), StandardCharsets.UTF_8));
    String l;
    String ent;
    Character c;
    int pos;
    try {
      while ((l = buf.readLine()) != null) {
        l = l.trim();
        if (l.charAt(0) == '#') continue;
        pos = l.indexOf(',');
        if (pos < 3) continue;
        if (pos + 1 >= l.length()) continue;
        ent = l.substring(0, pos);
        c = l.charAt(pos + 1);
        HTMLENT.put(ent, c);
      }
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }
  public static final HashMap<Character, String> FILENAME = new HashMap<Character, String>();
  static {
    FILENAME.put('a', "a");
    FILENAME.put('á', "a");
    FILENAME.put('à', "a");
    FILENAME.put('ä', "a");
    FILENAME.put('â', "a");
    FILENAME.put('A', "a");
    FILENAME.put('Á', "a");
    FILENAME.put('À', "a");
    FILENAME.put('Â', "a");
    FILENAME.put('Ä', "a");
    FILENAME.put('æ', "ae");
    FILENAME.put('Æ', "ae");
    FILENAME.put('b', "b");
    FILENAME.put('B', "b");
    FILENAME.put('c', "c");
    FILENAME.put('ç', "c");
    FILENAME.put('C', "c");
    FILENAME.put('Ç', "c");
    FILENAME.put('d', "d");
    FILENAME.put('D', "d");
    FILENAME.put('e', "e");
    FILENAME.put('é', "e");
    FILENAME.put('è', "e");
    FILENAME.put('ê', "e");
    FILENAME.put('ë', "e");
    FILENAME.put('E', "e");
    FILENAME.put('É', "e");
    FILENAME.put('È', "e");
    FILENAME.put('Ë', "e");
    FILENAME.put('f', "f");
    FILENAME.put('F', "f");
    FILENAME.put('g', "g");
    FILENAME.put('G', "g");
    FILENAME.put('h', "h");
    FILENAME.put('H', "h");
    FILENAME.put('i', "i");
    FILENAME.put('í', "i");
    FILENAME.put('ì', "i");
    FILENAME.put('î', "i");
    FILENAME.put('ï', "i");
    FILENAME.put('I', "i");
    FILENAME.put('Í', "i");
    FILENAME.put('Ì', "i");
    FILENAME.put('Î', "i");
    FILENAME.put('Ï', "i");
    FILENAME.put('j', "j");
    FILENAME.put('J', "j");
    FILENAME.put('k', "k");
    FILENAME.put('K', "k");
    FILENAME.put('l', "l");
    FILENAME.put('L', "l");
    FILENAME.put('m', "m");
    FILENAME.put('M', "m");
    FILENAME.put('n', "n");
    FILENAME.put('ñ', "n");
    FILENAME.put('N', "n");
    FILENAME.put('Ñ', "n");
    FILENAME.put('o', "o");
    FILENAME.put('ó', "o");
    FILENAME.put('ó', "o");
    FILENAME.put('ò', "o");
    FILENAME.put('ô', "o");
    FILENAME.put('ö', "o");
    FILENAME.put('õ', "o");
    FILENAME.put('O', "o");
    FILENAME.put('Ó', "o");
    FILENAME.put('Ò', "o");
    FILENAME.put('Ô', "o");
    FILENAME.put('Ö', "o");
    FILENAME.put('Õ', "o");
    FILENAME.put('Ø', "o");
    FILENAME.put('œ', "oe");
    FILENAME.put('Œ', "oe");
    FILENAME.put('p', "p");
    FILENAME.put('P', "p");
    FILENAME.put('q', "q");
    FILENAME.put('Q', "q");
    FILENAME.put('r', "r");
    FILENAME.put('R', "r");
    FILENAME.put('s', "s");
    FILENAME.put('š', "s");
    FILENAME.put('S', "s");
    FILENAME.put('Š', "s");
    FILENAME.put('t', "t");
    FILENAME.put('T', "t");
    FILENAME.put('u', "u");
    FILENAME.put('ú', "u");
    FILENAME.put('ù', "u");
    FILENAME.put('û', "u");
    FILENAME.put('ü', "u");
    FILENAME.put('U', "u");
    FILENAME.put('Ú', "u");
    FILENAME.put('Ù', "u");
    FILENAME.put('Û', "u");
    FILENAME.put('Ü', "u");
    FILENAME.put('v', "v");
    FILENAME.put('V', "v");
    FILENAME.put('w', "w");
    FILENAME.put('W', "w");
    FILENAME.put('x', "x");
    FILENAME.put('X', "x");
    FILENAME.put('y', "y");
    FILENAME.put('ý', "y");
    FILENAME.put('Y', "y");
    FILENAME.put('Ý', "y");
    FILENAME.put('z', "z");
    FILENAME.put('ž', "z");
    FILENAME.put('Z', "z");
    FILENAME.put('-', "-");
    FILENAME.put('_', "_");
    FILENAME.put('\'', "-");
    FILENAME.put('’', "-");
    FILENAME.put('\u200c', "");
    FILENAME.put('\u200d', "");
    FILENAME.put('0', "0");
    FILENAME.put('1', "1");
    FILENAME.put('2', "2");
    FILENAME.put('3', "3");
    FILENAME.put('4', "4");
    FILENAME.put('5', "5");
    FILENAME.put('6', "6");
    FILENAME.put('7', "7");
    FILENAME.put('8', "8");
    FILENAME.put('9', "9");
    FILENAME.put('°', "");
    FILENAME.put('.', ",");
    FILENAME.put('?', "");
  }
  static {
    int type;
    // infinite loop when size = 65536, a char restart to 0
    for (char c = 0; c < SIZE; c++) {
      if (Character.isHighSurrogate(c)) {
        CHARS[c] = HIGHSUR;
        continue;
      }
      if (Character.isLowSurrogate(c)) {
        CHARS[c] = LOWSUR;
        continue;
      }
      type = Character.getType(c);
      short properties = 0x0;
      // DO NOT modify '<>' values

      // inside a word ?
      if (Character.isISOControl(c)) {
        properties |= SPACE; // \n, \r, \t…
      }
      else if (Character.isLetter(c)) {
        properties |= LETTER | TOKEN;
        if (Character.isUpperCase(c)) {
          properties |= UPPERCASE;
        }
        if (Character.isLowerCase(c)) {
          properties |= LOWERCASE;
        }
      }
      else if (Character.isDigit(c)) {
        properties |= DIGIT | TOKEN;
      }
      else if (type == Character.MATH_SYMBOL) {
        properties |= MATH;
      }
      else if (Character.isSpaceChar(c)) {
        properties |= SPACE; // Unicode classes, with unbreakable
      }
      else if (Character.isWhitespace(c)) {
        properties |= SPACE; // \n, \r, \t…
      }
      else {
        type = Character.getType(c);
        // & is considered as Po OTHER_PUNCTUATION by unicode
        if (c == '&') {
          properties |= LETTER | TOKEN;
        }
        else if (type == Character.CONNECTOR_PUNCTUATION || type == Character.DASH_PUNCTUATION
            || type == Character.END_PUNCTUATION || type == Character.FINAL_QUOTE_PUNCTUATION
            || type == Character.INITIAL_QUOTE_PUNCTUATION || type == Character.OTHER_PUNCTUATION
            || type == Character.START_PUNCTUATION)
        {
          properties |= PUNCTUATION;
        }
        // hacky, hyphen maybe part of compound word, or start of a separator like ---
        if (c == '-' || c == 0xAD || c == '\'' || c == '’' || c == '_') {
          properties |= TOKEN;
        }
        if (c == '�') properties |= LETTER | TOKEN;
        if ('.' == c || '…' == c || '?' == c || '!' == c) properties |= PUNsent;
        else if (',' == c || ';' == c || ':' == c || '(' == c || ')' == c || '—' == c || '–' == c) properties |= PUNcl;
      }
      CHARS[c] = properties;
    }

  }

  public static char htmlent(final String ent)
  {
    Character c = HTMLENT.get(ent);
    if (c == null) return '�';
    return c;
  }

  public static char htmlent(final Chain ent)
  {
    @SuppressWarnings("unlikely-arg-type")
    Character c = HTMLENT.get(ent);
    if (c == null) return '�';
    return c;
  }

  public static String unTag(String xml)
  {
    StringBuilder dest = new StringBuilder();
    int start = 0;
    int end = xml.length();
    boolean lt = false, first = true;
    for (int i = start; i < end; i++) {
      char c = xml.charAt(i);
      switch (c) {
        case '<':
          first = false; // no broken tag at start
          lt = true;
          break;
        case '>':
          lt = false;
          // a broken tag at start, erase what was appended
          if (first) {
            dest.setLength(dest.length() - (i - start));
            first = false;
            break;
          }
          break;
        default:
          if (lt) break;
          else dest.append(c);
      }
    }
    return dest.toString();
  }

  /**
   * Get the internal properties for a char
   * 
   * @param c
   * @return
   */
  public static short props(final char c)
  {
    return CHARS[c];
  }

  /**
   * Is a word character (letter, but also, '’-_)
   * 
   * @see Character#isLetter(char)
   */
  public static boolean isToken(final char c)
  {
    return (CHARS[c] & TOKEN) > 0;
  }

  /**
   * Is a letter
   * 
   * @see Character#isLetter(char)
   */
  public static boolean isLetter(final char c)
  {
    return (CHARS[c] & LETTER) > 0;
  }

  /**
   * Is a Mathematic symbol
   */
  public static boolean isMath(final char c)
  {
    return (CHARS[c] & MATH) > 0;
  }

  /**
   * Is Numeric
   * 
   * @see Character#isDigit(char)
   */
  public static boolean isDigit(final char c)
  {
    return (CHARS[c] & DIGIT) > 0;
  }

  /**
   * Is a lower case letter
   * 
   * @see Character#isLowerCase(char)
   */
  public static boolean isLowerCase(final char c)
  {
    return (CHARS[c] & LOWERCASE) > 0;
  }

  /**
   * Is an upper case letter
   * 
   * @see Character#isUpperCase(char)
   */
  public static boolean isUpperCase(final char c)
  {
    return (CHARS[c] & UPPERCASE) > 0;
  }

  /**
   * Is a punctuation mark between words
   * 
   * @param ch
   * @return
   */
  public static boolean isPunctuation(final char c)
  {
    return (CHARS[c] & PUNCTUATION) > 0;
  }

  /**
   * Is a punctuation mark of sentence break level
   * 
   * @param ch
   * @return
   */
  public static boolean isPUNsent(final char c)
  {
    return (CHARS[c] & PUNsent) > 0;
  }

  /**
   * Is a punctuation mark of clause level (insisde a sentence)
   * 
   * @param ch
   * @return
   */
  public static boolean isPUNcl(final char c)
  {
    return (CHARS[c] & PUNcl) > 0;
  }

  /**
   * Is a "whitespace" according to ISO (space, tabs, new lines) and also for
   * Unicode (non breakable spoaces)
   * 
   * @see Character#isSpaceChar(char)
   * @see Character#isWhiteSpace(char)
   */
  public static boolean isSpace(final char c)
  {
    return (CHARS[c] & SPACE) > 0;
  }

  /**
   * Convenient method
   * 
   * @param ch
   * @return
   */
  public static boolean isPunctuationOrSpace(final char c)
  {
    return (CHARS[c] & PUNCTUATION_OR_SPACE) > 0;
  }

  /**
   * Is it first UTF-16 char of a supplemental unicode type like emoji ?
   * 
   * @param c
   * @return
   */
  public static boolean isHighSurrogate(final char c)
  {
    return (CHARS[c] & HIGHSUR) > 0;
  }

  /**
   * Is it second UTF-16 char of a supplemental unicode type like emoji ?
   * 
   * @param c
   * @return
   */
  public static boolean isLowSurrogate(final char c)
  {
    return (CHARS[c] & LOWSUR) > 0;
  }

  public static char toLower(char c)
  {
    if (!Char.isUpperCase(c)) return c;
    return Character.toLowerCase(c);
  }

  public static char toUpper(char c)
  {
    if (!Char.isLowerCase(c)) return c;
    return Character.toUpperCase(c);
  }

  /**
   * Lower casing on a string builder
   */
  public static StringBuilder toLower(StringBuilder s)
  {
    int max = s.length();
    char c;
    for (int i = 0; i < max; i++) {
      c = s.charAt(i);
      if (!Char.isUpperCase(c)) continue;
      // 1003 chars in multiple blocks accept a lower case
      // a hash of chars is not faster
      s.setCharAt(i, Character.toLowerCase(c));
    }
    // x6 new StringBuilder( s.toString().toLowerCase() )
    return s;
  }

  private Char()
  {
    // Don't
  }

  static public String toString(char c)
  {
    short props = CHARS[c];
    StringBuilder sb = new StringBuilder();
    sb.append(c).append("\t").append(Character.getName(c).toLowerCase()).append("\t");
    if ((props & TOKEN) > 0) sb.append("TOKEN ");
    if ((props & LETTER) > 0) sb.append("LETTER ");
    if ((props & SPACE) > 0) sb.append("SPACE ");
    if ((props & PUNCTUATION) > 0) sb.append("PUNCTUATION ");
    if ((props & PUNsent) > 0) sb.append("PUNsent ");
    if ((props & PUNcl) > 0) sb.append("PUNcl ");
    if ((props & DIGIT) > 0) sb.append("DIGIT ");
    if ((props & LOWERCASE) > 0) sb.append("LOWERCASE ");
    if ((props & UPPERCASE) > 0) sb.append("UPPERCASE ");
    if ((props & MATH) > 0) sb.append("MATH ");
    if ((props & HIGHSUR) > 0) sb.append("HIGHSUR ");
    if ((props & LOWSUR) > 0) sb.append("LOWSUR ");
    return sb.toString();
  }

}
