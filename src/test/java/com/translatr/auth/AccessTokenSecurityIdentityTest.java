package com.translatr.auth;

import com.translatr.model.AccessToken;
import com.translatr.model.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccessTokenSecurityIdentityTest {

    @Test
    void exposesKeyIdAndUserAttributes() {
        User u = new User();
        u.username = "jane";
        AccessToken t = new AccessToken();
        t.id = 4242L;
        t.key = "secret-key-value";
        t.user = u;

        AccessTokenSecurityIdentity identity = new AccessTokenSecurityIdentity(t);

        assertThat(identity.<String>getAttribute(AccessTokenSecurityIdentity.KEY_ID_ATTRIBUTE))
                .isEqualTo("4242");
        assertThat(identity.<User>getAttribute(AccessTokenSecurityIdentity.USER_ATTRIBUTE))
                .isSameAs(u);
        assertThat(identity.getPrincipal().getName()).isEqualTo("jane");
        assertThat(identity.isAnonymous()).isFalse();
    }
}
