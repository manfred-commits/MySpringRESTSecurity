package it.ugobetori.security;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import it.ugobetori.security.service.UserDetailsImpl;

/* Incapsula le funzioni di gestione dei token JWT. I parametri precedentemente inseriti in configurazione 
vengono impiegati in questa classe per generare il token ed impostarne la data di scadenza
*/
@Component
public class JwtUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);	
	
	// Recupera chiave segreta e data di scadenza da application.properties
	@Value("${jwt.secret}")
	private String jwtSecret;	
	@Value("${jwt.expirationms}")
	private Long jwtExpirationMs;

	/* Genera il token con: dati dell'utente, data di scadenza, algoritmo di cifratura, chiave segreta
	Authentication: rappresenta il token per una richiesta di autenticazione o per un'entità autenticata 
	una volta che la richiesta è stata elaborata dal metodo AuthenticationManager.authenticate(Authentication)
	 */
	public String generateJwtToken(Authentication authentication) {
		// getPrincipal(): l'identità dell'entità da autenticare
		UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
		Date now = new Date();
		Date exp = new Date((now).getTime() + jwtExpirationMs);
		userPrincipal.setExpirationTime(exp);
		// Usa il builder di Jwts per generare il token
		return Jwts.builder()
				.setSubject((userPrincipal.getUsername())).setIssuedAt(now)
				.setExpiration(exp)
				.signWith(SignatureAlgorithm.HS512, jwtSecret).compact();
	}
	
	// Estrae il nome utente dal Token
	public String getUserNameFromJwtToken(String token) {
		return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
	}
	
	// Valida il token
	public boolean validateJwtToken(String authToken) {
		// Analizza la stringa JWS 
		try {
			// Usa la chiave di firma per verificare i dati (claims) del token
			Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
			return true;
		} catch (SignatureException e) {
			logger.error("Invalid JWT signature: {}", e.getMessage());
		} catch (MalformedJwtException e) {
			logger.error("Invalid JWT token: {}", e.getMessage());
		} catch (ExpiredJwtException e) {
			logger.error("JWT token is expired: {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			logger.error("JWT token is unsupported: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			logger.error("JWT claims string is empty: {}", e.getMessage());
		}
		return false;
	}
}
