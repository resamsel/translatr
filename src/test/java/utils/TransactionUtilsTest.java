package utils;

import com.translatr.util.EmailUtils;
import com.translatr.util.MessageUtils;
import com.translatr.util.UuidUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sanity-check test — verifies that the Quarkus utility singletons can be instantiated.
 * Replaces the trivial Play-era TransactionUtils instantiation test.
 */
class TransactionUtilsTest {

    @Test
    void utilitiesAreInstantiable() {
        assertThat(new EmailUtils()).isNotNull();
        assertThat(new UuidUtils()).isNotNull();
        assertThat(new MessageUtils()).isNotNull();
    }
}
