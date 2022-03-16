package it.ugobetori.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import it.ugobetori.security.service.UserDetailsServiceImpl;

/* Viene invocata dal framework ogni volta che il client effettua una request.
Ha il compito di estrarre eventuali dati relativi al token JWT ed elaborarli per permettere il processo di autenticazione

Implementa il filtro OncePerRequestFilter
Spring Security a livello Web si basa sui filtri servlet.
Normalmente si usa una Servlet per controllare, pre-elaborare e/o postelaborare richieste specifiche. 
Ma se occorre filtrare/modificare richieste comuni e/o risposte in base a condizioni specifiche, allora un Filtro è molto di più adatto.
I filtri utilizzano FilterChain per richiamare il filtro successivo nella catena o la risorsa alla fine della catena.
Un filtro consente di decidere se un codice specifico debba essere eseguito appena prima o dopo l'esecuzione del servlet:
code1   ===>   esecuzione della servlet (usando chain.doFilter())   ===>    code2
Se, però c'è qualche altra richiesta a un altro servlet che ha lo stesso filtro, il filtro verrà eseguito di nuovo. 
Ogni volta che si effettua una richiesta internamente a qualche altra API nel progetto, la stessa autenticazione si ripeterebbe
poiché tutte le API hanno lo stesso filtro di sicurezza. OncePerRequestFilter impedisce questo comportamento.
*/
public class AuthTokenFilter extends OncePerRequestFilter {
	
	@Autowired
	private JwtUtils jwtUtils;
	@Autowired
	private UserDetailsServiceImpl userDetailsService;
	
	@Override
	// Request e Response della servlet, FilterChain richiama il filtro successivo nella catena o la risorsa finale
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			// Verifica l'eventuale token presente nella request
			String jwt = parseJwt(request);
			if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
				// Se il token è valido estrae il nome utente dal token
				String username = jwtUtils.getUserNameFromJwtToken(jwt);
				// Passa il nome al metodo di userDetailsService per recuperarlo dal db
				UserDetails userDetails = userDetailsService.loadUserByUsername(username);
				// Crea autenticazione tramite i dettagli dello user 
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
				// Crea un WebAuthenticationDetails contenente informazioni sulla request in corso
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				// Aggiunge l'oggetto Authentication nel SecurityContext in modo che sia utilizzabile dalle successive chiamate HTTP
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		} catch (Exception e) {
			logger.error("Non posso creare l'autenticazione {}", e);
		}
		filterChain.doFilter(request, response);
	}
	
	// Estrae il token dall'eventuale header 'Authorization' dalla request
	private String parseJwt(HttpServletRequest request) {
		String headerAuth = request.getHeader("Authorization");
		if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
			return headerAuth.substring(7, headerAuth.length());
		}
		return null;
	}
}
