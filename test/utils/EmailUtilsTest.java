package utils;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EmailUtilsTest {

  @Test
  public void maskEmailAddressBlank() {
    // given
    String email = "";

    // when
    String actual = EmailUtils.maskEmail(email);

    // then
    assertThat(actual).isEqualTo("");
  }

  @Test
  public void maskEmailAddressShortest() {
    // given
    String email = "a@b.com";

    // when
    String actual = EmailUtils.maskEmail(email);

    // then
    assertThat(actual).isEqualTo("*@b.com");
  }

  @Test
  public void maskEmailAddressShorter() {
    // given
    String email = "ab@b.com";

    // when
    String actual = EmailUtils.maskEmail(email);

    // then
    assertThat(actual).isEqualTo("**@b.com");
  }

  @Test
  public void maskEmailAddressShort() {
    // given
    String email = "abc@b.com";

    // when
    String actual = EmailUtils.maskEmail(email);

    // then
    assertThat(actual).isEqualTo("***@b.com");
  }

  @Test
  public void maskEmailAddressVisibleStartAndEnd() {
    // given
    String email = "abcd@b.com";

    // when
    String actual = EmailUtils.maskEmail(email);

    // then
    assertThat(actual).isEqualTo("a**d@b.com");
  }

  @Test
  public void maskEmailAddressVisibleStartAndEndLonger() {
    // given
    String email = "abcde@b.com";

    // when
    String actual = EmailUtils.maskEmail(email);

    // then
    assertThat(actual).isEqualTo("a***e@b.com");
  }
}
