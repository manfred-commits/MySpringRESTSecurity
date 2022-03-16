package it.ugobetori.security.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import it.ugobetori.User;

/* UserDetailsImpl 
Implementa l’interfaccia UserDetails fornita dal modulo Spring Security.
UserDetails è un contenitore per le informazioni principali dell'utente. 
Memorizza le informazioni dell'utente che vengono successivamente incapsulate negli oggetti di autenticazione. 
Ciò consente di memorizzare in una posizione comoda le informazioni dell'utente non correlate alla sicurezza 
(come indirizzi e-mail, numeri di telefono, ecc.). 
*/
@Data
public class UserDetailsImpl implements UserDetails {
	
	private static final long serialVersionUID = -7130684040991684077L;
	
	private Long id;
	private String username;
	private String email;
	// Esclude dalla serializzazione
	@JsonIgnore
	private String password;
	
	
	// Proprietà dell'utente: abilitato, bloccato, scaduto, password scaduta
	// Locked: account sospeso automaticamente a causa di tentativi di accesso non validi
	// Expired: account disattivato amministrativamente in base a un timing (es. abbonamento scaduto)
	// CredentialsNonExpired: scadenza delle credenziali (password) 
	private boolean accountNonLocked = true;	
	private boolean accountNonExpired = false;
	private boolean credentialsNonExpired = true;
	private boolean enabled = true;
	private Date expirationTime;
	
	// GrantedAuthority, rappresenta un'autorizzazione concessa (letture, scrittura, ecc) 
	private Collection<? extends GrantedAuthority> authorities;
	
	// Costruttore
	public UserDetailsImpl(Long id, String username, String email, String password, boolean enabled, Collection<? extends GrantedAuthority> authorities) {
		super();
		this.id = id;
		this.username = username;
		this.email = email;
		this.password = password;		
		this.accountNonLocked = enabled;
		this.accountNonExpired = enabled;
		this.credentialsNonExpired = enabled;
		this.enabled = enabled;
		this.authorities = authorities;
	}
	
	

	public static UserDetailsImpl build(User user) {
		// SimpleGrantedAuthority, implementazione concreta di base di una GrantedAuthority
		List<GrantedAuthority> authorities = user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getRoleType().name())).collect(Collectors.toList());
		// Restituisce i dati dello user e la lista delle sue autorizzazioni
		return new UserDetailsImpl(user.getId(), user.getUsername(), user.getEmail(), user.getPassword(), user.getActive(), authorities);
	}
}
