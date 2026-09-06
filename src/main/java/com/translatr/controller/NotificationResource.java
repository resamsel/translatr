package com.translatr.controller;

import com.translatr.dto.PagedNotificationList;
import com.translatr.generated.api.NotificationsApi;
import jakarta.annotation.security.PermitAll;

import java.util.Collections;

/**
 * Stub implementation of the notifications endpoint.
 * The original Play application used getstream.io which is not part of the
 * Quarkus migration. Returns an empty list until a replacement is wired in.
 */
public class NotificationResource implements NotificationsApi {

    @Override
    @PermitAll
    public PagedNotificationList findNotifications(Integer offset, Integer limit) {
        return new PagedNotificationList(0, offset, limit, false, offset != null && offset > 0, Collections.emptyList());
    }
}
