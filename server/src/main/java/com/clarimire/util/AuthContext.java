package com.clarimire.util;

import io.jsonwebtoken.Claims;

import javax.servlet.http.HttpServletRequest;

public final class AuthContext {

    public static final String ATTR_USERNAME = "currentUsername";
    public static final String ATTR_ROLE = "currentRole";

    private AuthContext() {}

    public static String getUsername(HttpServletRequest request) {
        Object v = request.getAttribute(ATTR_USERNAME);
        return v != null ? v.toString() : null;
    }

    public static String getRole(HttpServletRequest request) {
        Object v = request.getAttribute(ATTR_ROLE);
        return v != null ? v.toString() : null;
    }

    public static void setFromClaims(HttpServletRequest request, Claims claims) {
        if (claims == null) {
            return;
        }
        request.setAttribute(ATTR_USERNAME, claims.get("username"));
        request.setAttribute(ATTR_ROLE, claims.get("role"));
    }
}
