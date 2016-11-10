package com.nodestand.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * https://github.com/Robbert1/boot-stateless-social
 *
 * https://github.com/szerhusenBC/jwt-spring-security-demo
 */
@Component
public class TokenHandler {


    private static final String CLAIM_KEY_USERNAME = "sub";
    private static final String CLAIM_KEY_CREATED = "created";

    private static final long TEN_DAYS = 1000 * 60 * 60 * 24 * 10;
    private static final String CLAIM_KEY_SOCIAL_PROVIDER = "socialProvider";
    private static final String CLAIM_KEY_SOCIAL_ID = "socialId";

    @Value("${jwt.secret}")
	private String secret;


	public String getUserStableIdFromToken(String token) {
        String stableId;
        try {
            final Claims claims = getClaimsFromToken(token);
            stableId = claims.getSubject();
        } catch (Exception e) {
            stableId = null;
        }
        return stableId;
    }

    public String getSocialIdFromToken(String token) {
        String socialId;
        try {
            final Claims claims = getClaimsFromToken(token);
            socialId = (String) claims.get(CLAIM_KEY_SOCIAL_ID);
        } catch (Exception e) {
            socialId = null;
        }
        return socialId;
    }

    public Date getExpirationDateFromToken(String token) {
        Date expiration;
        try {
            final Claims claims = getClaimsFromToken(token);
            expiration = claims.getExpiration();
        } catch (Exception e) {
            expiration = null;
        }
        return expiration;
    }

    public String generateToken(NodeUserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_USERNAME, userDetails.getUserId());
        claims.put(CLAIM_KEY_SOCIAL_PROVIDER, userDetails.getProviderId());
        claims.put(CLAIM_KEY_SOCIAL_ID, userDetails.getProviderUserId());
        claims.put(CLAIM_KEY_CREATED, new Date());
        return generateToken(claims);
    }

    private String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(generateExpirationDate())
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    private Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + TEN_DAYS);
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

	private Claims getClaimsFromToken(String token) {
		Claims claims;
		try {
			claims = Jwts.parser()
					.setSigningKey(secret)
					.parseClaimsJws(token)
					.getBody();
		} catch (Exception e) {
			claims = null;
		}
		return claims;
	}

    public Boolean validateToken(String token, NodeUserDetails userDetails) {
        final String stableId = getUserStableIdFromToken(token);
        final String socialId = getSocialIdFromToken(token);

        return stableId != null && stableId.equals(userDetails.getUserId())
                && socialId != null && socialId.equals(userDetails.getProviderUserId())
                && !isTokenExpired(token);
    }

}
