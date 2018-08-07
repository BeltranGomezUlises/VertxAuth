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
import io.vertx.core.json.JsonObject;
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
    private static final String RECOVER_PRIVATE_KEY = "5]yM#;jbI)=s&!:Lh.:LPwv+~W]GH&_a8J[e*xY}0i8YywNz6<`J'+)hGs'2Z[U46w'wK2+i`!CaXOW#]TGquiF:HS:^M}>~b6xuF_s53N~i#B=VHJO+kBznBdkuDF9FBCCA13757B338279EDE56D1DF3EDCCB23BE6748729257D9F791DCD6A6554B361EBC99B";

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
     * Generates a jws for the process of recover a employee password
     *
     * @param revocerCode generated recover code that the user has to use
     * @return string with the jws
     */
    public static String generateRecoverPasswordToken(final String revocerCode, final String employeeEmail) {
        JwtBuilder builder = Jwts.builder();
        builder.setSubject(new JsonObject()
                .put("recover_code", revocerCode)
                .put("employee_email", employeeEmail).toString()
        );
        builder.setIssuer("auth system");
        return builder.signWith(SignatureAlgorithm.HS512, RECOVER_PRIVATE_KEY).compact();
    }

    /**
     * Checks if the recover code is the same to the code inside recoverToken
     *
     * @param recoverToken generated recover token
     * @param recoverCode generated recover code
     * @return true if the recover code and the recover token matches, false otherwise
     */
    public static RecoverValidation isRecoverTokenMatching(final String recoverToken, final String recoverCode) {
        try {
            String object = Jwts.parser().setSigningKey(RECOVER_PRIVATE_KEY).parseClaimsJws(recoverToken).getBody().getSubject();
            JsonObject body = new JsonObject(object);
            if (body.getString("recover_code").equals(recoverCode)){
                return new RecoverValidation(true, body.getString("employee_email"));
            }            
        } catch (Exception e) {            
        }
        return new RecoverValidation(false, null);
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

    public static class RecoverValidation {

        private boolean valid;
        private String employeeMail;

        public RecoverValidation(boolean valid, String employeeMail) {
            this.valid = valid;
            this.employeeMail = employeeMail;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public String getEmployeeMail() {
            return employeeMail;
        }

        public void setEmployeeMail(String employeeMail) {
            this.employeeMail = employeeMail;
        }

    }

}
