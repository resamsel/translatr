package utils;

import com.translatr.model.Message;
import com.translatr.util.MessageUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static com.translatr.util.MessageUtils.wordCount;

class MessageUtilsTest {

    @Test
    void testWordCount() {
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
        assertThat(wordCount("a\u2014c")).isEqualTo(2);   // em dash —
        assertThat(wordCount("a\u2026c")).isEqualTo(2);   // ellipsis …
        assertThat(wordCount(
                "\"Oh, no,\" she's saying, \"our $400 blender can't handle something this hard!\""))
                .isEqualTo(12);
    }
}
