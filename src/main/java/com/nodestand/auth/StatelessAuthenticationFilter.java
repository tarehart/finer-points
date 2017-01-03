package com.nodestand.auth;

import com.nodestand.controllers.ResourceNotFoundException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.repository.UserRepository;
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
import java.util.Optional;

@Component
public class StatelessAuthenticationFilter extends GenericFilterBean {

    private static final String TOKEN_HEADER_NAME = "Authorization";

    private final TokenHandler tokenHandler;

    private final UserService userService;

    private final UserRepository userRepository;

    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
    public StatelessAuthenticationFilter(TokenHandler tokenHandler, UserService userService, UserRepository userRepository) {
        this.tokenHandler = tokenHandler;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        //jwtFilter(httpRequest);
        userAgentTestFilter(httpRequest);

		chain.doFilter(request, response);
	}

	private void userAgentTestFilter(HttpServletRequest httpRequest) {

        String userAgent = httpRequest.getHeader("User-Agent");

        String providerId = "userAgent";
        String providerUserId = userAgent;

        Optional<NodeUserDetails> userDetails = userService.loadUserBySocialProvider(providerId, providerUserId);

        NodeUserDetails concreteUser;

        // Step 3b. Create a new user account or return an existing one.
        if (userDetails.isPresent()) {
            concreteUser = userDetails.get();
        } else {

            //add new users to the db with its default roles for later use in SocialAuthenticationSuccessHandler
            final User user = new User(
                    providerId,
                    providerUserId,
                    User.Roles.ROLE_USER);

            String[] userAgentParts = userAgent.split(" ");

            // Start with three aliases
            user.addNewAlias(userAgentParts[userAgentParts.length - 1]);

            userRepository.save(user);

            concreteUser = new NodeUserDetails(user);
        }

        setUserInContext(httpRequest, concreteUser);
    }

    private void setUserInContext(HttpServletRequest httpRequest, NodeUserDetails concreteUser) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(concreteUser, null, concreteUser.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void jwtFilter(HttpServletRequest httpRequest) {
        String authToken = getToken(httpRequest);
        String stableId = tokenHandler.getUserStableIdFromToken(authToken);

        if (stableId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            try {

                NodeUserDetails userDetails = this.userService.loadUserByUserId(stableId);

                if (tokenHandler.validateToken(authToken, userDetails)) {
                    setUserInContext(httpRequest, userDetails);
                }
            } catch (ResourceNotFoundException e) {
                logger.error("Failed attempt to log in because matching user not found in database.", e);
            }
        }
    }

    private String getToken(HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader(TOKEN_HEADER_NAME);
        // We are expecting something like "Bearer XYZ". Strip off the Bearer prefix.
        return authHeader != null ? authHeader.split(" ")[1] : null;
    }

}