package utils;

import com.translatr.util.EmailUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailUtilsTest {

    @Test
    void maskEmailAddressBlank() {
        assertThat(EmailUtils.maskEmail("")).isEqualTo("");
    }

    @Test
    void maskEmailAddressShortest() {
        assertThat(EmailUtils.maskEmail("a@b.com")).isEqualTo("*@b.com");
    }

    @Test
    void maskEmailAddressShorter() {
        assertThat(EmailUtils.maskEmail("ab@b.com")).isEqualTo("**@b.com");
    }

    @Test
    void maskEmailAddressShort() {
        assertThat(EmailUtils.maskEmail("abc@b.com")).isEqualTo("***@b.com");
    }

    @Test
    void maskEmailAddressVisibleStartAndEnd() {
        assertThat(EmailUtils.maskEmail("abcd@b.com")).isEqualTo("a**d@b.com");
    }

    @Test
    void maskEmailAddressVisibleStartAndEndLonger() {
        assertThat(EmailUtils.maskEmail("abcde@b.com")).isEqualTo("a***e@b.com");
    }
}
