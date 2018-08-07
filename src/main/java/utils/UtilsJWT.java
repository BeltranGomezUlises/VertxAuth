/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Ulises Beltrán Gómez --- beltrangomezulises@gmail.com
 */
public class UtilsJWT {

    private static final String PUBLIC_KEY = "k$5*t;ht^L$_g76k'H6LSas\"n`6xrE=)?)+g!~0r198(\"D^|Hl'~+SvuMm'P_([";
    private static final String PRIVATE_KEY = "5]yM#;jbI)=s&!:Lh.:LPwv+~W]GH&_a8J[e*xY}0i8YywNz6<`J'+)hGs'2Z[U46w'wK2+i`!CaXOW#]TGquiF:HS:^M}>~b6xuF_s53N~i#B=VHJO+kBznBdkuDF9FBCCA13757B338279EDE56D1DF3EDCCB23BE6748729257D9F791DCD6A6554B361EBC99B";

    /**
     * Generates a jwt for access a system
     *
     * @param employeeId id of the user requesting token
     * @return Access JWT as string
     */
    public static String generateAccessToken(final int employeeId) {
        JwtBuilder builder = Jwts.builder();
        builder.setSubject(String.valueOf(employeeId));
        builder.setIssuer("auth system");
        builder.setIssuedAt(new Date());
        builder.setExpiration(new Date(System.currentTimeMillis() + (1000 * 60 * 60)));
        return builder.signWith(SignatureAlgorithm.HS512, PUBLIC_KEY).compact();
    }

    /**
     * Generates a jwt for refresh a access token
     *
     * @param employeeId id of the user requesting token
     * @return Refresh JWT as string
     */
    public static String generateRefreshToken(final int employeeId) {
        JwtBuilder builder = Jwts.builder();
        builder.setSubject(String.valueOf(employeeId));
        builder.setIssuer("auth system");

        Map<String, Object> claims = new HashMap<>();
        claims.put("day", Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
        claims.put("employeeId", employeeId);
        builder.setClaims(claims);

        return builder.signWith(SignatureAlgorithm.HS512, PRIVATE_KEY).compact();
    }

    /**
     * Obtains the employee id from the access token
     *
     * @param accessToken JWT where the employee is stored
     * @return id of the user
     */
    public static int getEmployeeIdFrom(final String accessToken) {
        return Integer.valueOf(Jwts.parser()
                .setSigningKey(PUBLIC_KEY)
                .parseClaimsJws(accessToken)
                .getBody()
                .getSubject());
    }

    /**
     * Checks if the accesoToken is valid
     *
     * @param accessToken string token to validate
     * @return true if the accessToken is valid, false otherwise
     */
    public static boolean isAccessTokenValid(String accessToken) {
        try {
            Jwts.parser().setSigningKey(PUBLIC_KEY).parseClaimsJws(accessToken);
            return true;
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException
                | UnsupportedJwtException | IllegalArgumentException | NullPointerException e) {
            return false;
        }
    }

    /**
     * Generates a new accessToken refreshing the client session
     *
     * @param refreshToken refresh token provided by the login
     * @param accessToken access token provided by the login
     * @return the new access token
     * @throws Exception any value is no valid for the refreshtoken
     */
    public static String refreshToken(final String refreshToken, final String accessToken) throws Exception {
        try {
            Jwts.parser().setSigningKey(PUBLIC_KEY).parseClaimsJws(accessToken);
            throw new Exception("Access token is still valid");
        } catch (ExpiredJwtException e) {
            long timeDiference = System.currentTimeMillis() - e.getClaims().getExpiration().getTime();
            if (timeDiference > (1000 * 60 * 60)) { //is grater than 1 hour
                throw new Exception("Can't refresh accessToken");
            } else {
                Claims claims = Jwts.parser().setSigningKey(PRIVATE_KEY).parseClaimsJws(refreshToken).getBody();
                int employeeId = Integer.parseInt(claims.get("employeeId").toString());
                int tokenDay = Integer.parseInt(claims.get("day").toString());

                int actualEmployeeId = Integer.valueOf(e.getClaims().getSubject());
                int actualDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
                if (employeeId == actualEmployeeId
                        && actualDay == tokenDay) {
                    return generateAccessToken(employeeId);
                } else {
                    throw new Exception("Can't refresh accessToken");
                }
            }
        }
    }
    
}
