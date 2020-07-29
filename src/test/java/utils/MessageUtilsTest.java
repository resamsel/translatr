package utils;

import static org.assertj.core.api.Assertions.assertThat;
import static utils.MessageUtils.wordCount;

import models.Message;
import org.junit.Test;

/**
 * @author resamsel
 * @version 6 Jun 2017
 */
public class MessageUtilsTest {
  @Test
  public void testWordCount() {
    assertThat(wordCount((String) null)).isZero();
    assertThat(wordCount((Message) null)).isZero();
    assertThat(wordCount("a")).isEqualTo(1);
    assertThat(wordCount("a ")).isEqualTo(1);
    assertThat(wordCount(" a")).isEqualTo(1);
    assertThat(wordCount("a b c")).isEqualTo(3);
    assertThat(wordCount("<span class=\"abc\">a b c</span>")).isEqualTo(3);
    assertThat(wordCount("<span class=\"abc\">a b c")).isEqualTo(3);
    assertThat(wordCount("a, b, c")).isEqualTo(3);
    assertThat(wordCount("a,b.c")).isEqualTo(3);
    assertThat(wordCount("a:b-c!")).isEqualTo(2);
    assertThat(wordCount("a\nb;c?")).isEqualTo(3);
    assertThat(wordCount("a\tb'c")).isEqualTo(2);
    assertThat(wordCount("a—c")).isEqualTo(2);
    assertThat(wordCount("a…c")).isEqualTo(2);
    assertThat(wordCount(
        "\"Oh, no,\" she's saying, \"our $400 blender can't handle something this hard!\""))
            .isEqualTo(12);
  }
}
