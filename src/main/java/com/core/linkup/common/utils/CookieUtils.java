package com.core.linkup.common.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CookieUtils {
    public static Optional<Cookie> getCookie(HttpServletRequest request,
                                             String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return Optional.of(cookie);
                }
            }
        }
        return Optional.empty();
    }

    public static void addCookie(HttpServletResponse response,
                                 String name,
                                 String value,
                                 int cookieMaxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(cookieMaxAge);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public static void removeCookie(HttpServletRequest request,
                                    HttpServletResponse response,
                                    String name) {

        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    cookie.setValue("");
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                }
            }
        }
    }
}
