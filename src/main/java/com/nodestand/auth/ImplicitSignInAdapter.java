/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nodestand.auth;

import com.nodestand.service.NodeUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.web.SignInAdapter;
import org.springframework.web.context.request.NativeWebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class ImplicitSignInAdapter implements SignInAdapter {

    private final RequestCache requestCache;

    @Autowired
    public ImplicitSignInAdapter(RequestCache requestCache) {
        this.requestCache = requestCache;
    }

    @Autowired
    private NodeUserDetailsService nodeUserDetailsService;

    @Override
    public String signIn(String localUserId, Connection<?> connection, NativeWebRequest request) {
        String providerUserId = connection.getKey().getProviderUserId();
        NodeUserDetails userDetails = null;
        try {
            userDetails = nodeUserDetailsService.loadUserByUsername(providerUserId);
            nodeUserDetailsService.setCurrentUser(userDetails.getUser());
        } catch (UsernameNotFoundException e) {
            userDetails = nodeUserDetailsService.register(providerUserId, connection.getDisplayName());
            // TODO: go to registration page so they can change their display name.
        }
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

        return extractOriginalUrl(request);
    }

    private String extractOriginalUrl(NativeWebRequest request) {
        HttpServletRequest nativeReq = request.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse nativeRes = request.getNativeResponse(HttpServletResponse.class);
        SavedRequest saved = requestCache.getRequest(nativeReq, nativeRes);
        if (saved == null) {
            return null;
        }
        requestCache.removeRequest(nativeReq, nativeRes);
        removeAutheticationAttributes(nativeReq.getSession(false));
        return saved.getRedirectUrl();
    }

    private void removeAutheticationAttributes(HttpSession session) {
        if (session == null) {
            return;
        }
        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }

}