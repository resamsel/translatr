package com.translatr.controller;

import com.translatr.auth.CurrentUserResolver;
import com.translatr.criteria.UserCriteria;
import com.translatr.dto.PagedList;
import com.translatr.dto.PagedUserList;
import com.translatr.dto.UserDto;
import com.translatr.generated.api.UsersApi;
import com.translatr.model.User;
import com.translatr.service.UserService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;

import java.util.UUID;

public class UserResource implements UsersApi {

    private final UserService         userService;
    private final CurrentUserResolver currentUserResolver;

    @Inject
    public UserResource(UserService userService, CurrentUserResolver currentUserResolver) {
        this.userService         = userService;
        this.currentUserResolver = currentUserResolver;
    }

    @Override
    @PermitAll
    public PagedUserList findUsers(String search, Integer offset, Integer limit, String order, String fetch,
                                   String username, String email) {
        return toPagedDto(userService.find(toCriteria(search, offset, limit, order, fetch, username, email)));
    }

    @Override
    @PermitAll
    public UserDto getUser(UUID id) {
        return userService.get(id);
    }

    @Override
    @PermitAll
    public UserDto getUserByUsername(String username) {
        return userService.getByUsername(username);
    }

    // The UI calls this as ?fetch=features (translatr) and ?fetch=featureFlags (translatr-admin);
    // both read user.features. UserService.get() always folds the UserFeatureFlag rows into that
    // map, so the fetch param needs no handling here - it is intentionally a no-op.
    @Override
    @Authenticated
    public UserDto getCurrentUser() {
        User user = currentUserResolver.resolve();
        return userService.get(user.id);
    }

    @Override
    @PermitAll
    public UserDto getProfile() {
        return null;
    }

    @Override
    @Authenticated
    public UserDto updateUser(UserDto userDto) {
        return userService.update(userDto);
    }

    @Override
    @Authenticated
    public UserDto deleteUser(UUID id) {
        return userService.delete(id);
    }

    @Override
    @Authenticated
    public UserDto saveUserSettings(UUID id, UserDto userDto) {
        userDto.setId(id);
        return userService.update(userDto);
    }

    static UserCriteria toCriteria(String search, Integer offset, Integer limit, String order, String fetch,
                                   String username, String email) {
        UserCriteria c = new UserCriteria();
        c.search   = search;
        if (offset != null) c.offset = offset;
        if (limit  != null) c.limit  = limit;
        c.order    = order;
        c.fetch    = fetch;
        c.username = username;
        c.email    = email;
        return c;
    }

    private static PagedUserList toPagedDto(PagedList<UserDto> src) {
        return new PagedUserList(src.total, src.offset, src.limit, src.hasNext, src.hasPrev, src.list);
    }
}
