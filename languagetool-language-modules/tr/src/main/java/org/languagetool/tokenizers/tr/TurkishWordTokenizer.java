/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Andriy Rysin (http://www.languagetool.org)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.tokenizers.tr;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.languagetool.tokenizers.WordTokenizer;

/**
 * Turkish word tokenizer.
 * <p>
 * Turkish attaches suffixes to proper nouns via an apostrophe, e.g.
 * {@code Ali'nin} (Ali's), {@code Ankara'da} (in Ankara), {@code Türkiye'ye}
 * (to Turkey). The base {@link WordTokenizer} splits on the apostrophe, which
 * would break these into separate tokens. This subclass keeps the apostrophe
 * inside the token when it sits between two word (letter) characters.
 * <p>
 * Both the ASCII apostrophe {@code '} (U+0027) and the typographic right single
 * quotation mark {@code '} (U+2019), common in Turkish typography, are handled
 * identically. Apostrophes at word boundaries still split normally.
 */
public class TurkishWordTokenizer extends WordTokenizer {

  private static final char ASCII_APOSTROPHE = '\'';
  private static final char TYPOGRAPHIC_APOSTROPHE = '\u2019';

  private static boolean isApostrophe(String tok) {
    return tok.length() == 1
        && (tok.charAt(0) == ASCII_APOSTROPHE || tok.charAt(0) == TYPOGRAPHIC_APOSTROPHE);
  }

  private static boolean isWordToken(String tok) {
    if (tok.isEmpty()) {
      return false;
    }
    for (int i = 0; i < tok.length(); i++) {
      if (!Character.isLetter(tok.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Tokenize Turkish text, keeping apostrophes inside proper-noun + suffix
   * tokens such as {@code Ali'nin}.
   *
   * @param text String of words to tokenize.
   */
  @Override
  public List<String> tokenize(String text) {
    List<String> raw = new ArrayList<>();
    StringTokenizer st = new StringTokenizer(text, getTokenizingCharacters(), true);
    while (st.hasMoreElements()) {
      raw.add(st.nextToken());
    }
    List<String> joined = joinEMailsAndUrls(raw);
    return joinApostropheInsideWords(joined);
  }

  /**
   * Walk the raw token list and, wherever an apostrophe sits between two
   * letter tokens, merge the three into one token. Handles chained merges
   * (e.g. a token that just absorbed an apostrophe can absorb more on either
   * side is not required for Turkish, but we keep the loop stable).
   */
  private List<String> joinApostropheInsideWords(List<String> tokens) {
    List<String> out = new ArrayList<>();
    int i = 0;
    while (i < tokens.size()) {
      String cur = tokens.get(i);
      if (i + 2 < tokens.size() && isApostrophe(tokens.get(i + 1))
          && isWordToken(cur) && isWordToken(tokens.get(i + 2))) {
        // merge cur + apostrophe + next, then keep scanning from the merged token
        StringBuilder merged = new StringBuilder(cur);
        merged.append(tokens.get(i + 1)).append(tokens.get(i + 2));
        i += 3;
        out.add(merged.toString());
      } else {
        out.add(cur);
        i++;
      }
    }
    return out;
  }
}
