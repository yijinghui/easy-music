package com.easy.enumeration;

import lombok.Getter;

@Getter
public enum RoleEnum {


    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER"),
    ARTIST("ROLE_ARTIST");

    private final String role;

    RoleEnum(String role) {
        this.role = role;
    }

}
