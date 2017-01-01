package com.nodestand.auth;

import com.nodestand.controllers.ResourceNotFoundException;
import com.nodestand.service.user.UserService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class StatelessAuthenticationFilter extends GenericFilterBean {

    private static final String TOKEN_HEADER_NAME = "Authorization";

    private final TokenHandler tokenHandler;

    private final UserService userService;

    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
    public StatelessAuthenticationFilter(TokenHandler tokenHandler, UserService userService) {
        this.tokenHandler = tokenHandler;
        this.userService = userService;
    }

    @Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String authToken = getToken(httpRequest);
        String stableId = tokenHandler.getUserStableIdFromToken(authToken);

        if (stableId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            try {

                NodeUserDetails userDetails = this.userService.loadUserByUserId(stableId);

                if (tokenHandler.validateToken(authToken, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (ResourceNotFoundException e) {
                logger.error("Failed attempt to log in because matching user not found in database.", e);
            }
        }

		chain.doFilter(request, response);
	}

    private String getToken(HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader(TOKEN_HEADER_NAME);
        // We are expecting something like "Bearer XYZ". Strip off the Bearer prefix.
        return authHeader != null ? authHeader.split(" ")[1] : null;
    }

}