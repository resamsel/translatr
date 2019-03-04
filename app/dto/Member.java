package dto;

import models.ProjectUser;

import java.util.UUID;

public class Member {
    public Long id;

    public UUID userId;

    public String userName;

    public String userUsername;

    public String userEmail;

    public MemberRole role;

    public Member(ProjectUser member) {
        this.id = member.id;
        this.userId = member.user.id;
        this.userName = member.user.name;
        this.userUsername = member.user.username;
        this.userEmail = member.user.email;
        this.role = MemberRole.from(member.role);
    }

    public static Member from(models.ProjectUser member) {
        return new Member(member);
    }
}
