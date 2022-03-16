package it.ugobetori.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import it.ugobetori.security.service.UserDetailsServiceImpl;

/* Spring Security
La sicurezza delle applicazioni si riduce a due problemi più o meno indipendenti: l'autenticazione (chi sei?) e l'autorizzazione (cosa puoi fare?).
Spring Security si concentra sulla fornitura di autenticazione e autorizzazione alle applicazioni Java.
Caratteristiche
- Supporto completo ed estensibile sia per l'autenticazione che per l'autorizzazione
- Protezione contro attacchi come session fixation, clickjacking, cross site request forgery, ecc.
- Integrazione API servlet
- Integrazione con Spring Web MVC
*/
/* WebSecurityConfig estende WebSecurityConfigurerAdapter che abilita la sicurezza HTTP in Spring
Tramite @Configuration e @EnableWebSecurity, abilita le funzioni di sicurezza e ne configura i parametri
- Istruisce il sistema sul service (nel nostro caso userDetailsService) da impiegare per manipolare i dati dello user 
- Registra il filter (nel nostro caso AuthTokenFilter) che ha il compito di estrarre e processare il token JWT dalle request
- Definisce l'algoritmo di encoding delle password
- Configura la sicurezza HTTP
*/

@SuppressWarnings("deprecation")
@Configuration
@EnableWebSecurity
// Abilita le annotazioni prepost (es. @PreAuthorize("hasRole('ADMIN') or hasRole('USER')"))
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
	UserDetailsServiceImpl userDetailsService;
	@Autowired
	private AuthEntryPointUnauthorizedJwt unauthorizedHandler;

	@Bean
	public AuthTokenFilter authenticationJwtTokenFilter() {
		return new AuthTokenFilter();
	}
		
	// Configura l'AuthenticationManagerBuilder che si occupa di costruire un gestore di autenticazione
	@Override
	public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
		// Aggiunge l'autenticazione utilizzando userDetailsService passato come argomento e il tipo di encoder scelto
		authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(this.passwordEncoder());
	}
	
	/* AuthenticationManager: Elabora una request di autenticazione
	L'interfaccia principale della strategia per l'autenticazione in Spring
	Un AuthenticationManager può fare una delle 3 cose nel suo metodo authenticate():
	- Restituire un'autenticazione (normalmente con authenticated=true) se può verificare che l'input rappresenti un'entità valida
	- Generare un'eccezione AuthenticationException se ritiene che l'input rappresenti un'entità non valida
	- Restituire null se non può decidere
	*/
	// Espone come Bean l'AuthenticationManager in modo che sia disponibile come autowired nel controller
	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	// Definisce il sistema di encoding delle password
	@Bean
	public PasswordEncoder passwordEncoder() {
		// 1. Implementazione deprecata (non sicura)
		return NoOpPasswordEncoder.getInstance();
		/* 2. Implementa la funzione di hashing utilizzando l'encoder corrispondente all’algoritmo scelto (in questo caso Bcrypt) */
		//return new BCryptPasswordEncoder();
	}
	
	// Il metodo configure(HttpSecurity) definisce i criteri di protezione HTTP
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// Imposta il filtro CORS
		http.cors().and()
			// In questo esempio disabilita il filtro CSRF
			.csrf().disable()
			// Definisce la classe che gestisce gli accessi non autorizzati
			.exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
			// Imposta la policy delle Session
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
			// Definisce quali percorsi URL sono autorizzati (in questo caso tutti i percorsi e relative directory)
			.authorizeRequests().antMatchers("/**").permitAll()
			// Ogni request deve essere autenticata
			.anyRequest().authenticated();
		// Aggiunge il filtro di autenticazione
		http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
	}
}
