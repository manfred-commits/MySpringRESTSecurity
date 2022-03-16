package it.ugobetori.security.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.ugobetori.User;
import it.ugobetori.repositories.UserRepository;

/* UserDetailsServiceImpl 
Implementa l’interfaccia UserDetailsService fornita dal modulo Spring Security.
L'interfaccia UserDetailsService viene utilizzata per recuperare i dati relativi all'utente. 
Ha un metodo chiamato loadUserByUsername() che può essere sovrascritto per personalizzare il processo di ricerca dell'utente
UserDetailsService restituisce un'implementazione UserDetails che contiene le GrantedAuthorities
*/
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	UserRepository userRepository;

	@Override
	// Il metodo viene eseguito in una transazione DB
	@Transactional
	// Cerca l'utente nel DB e ritorna l'utente tramite l'implementazione di UserDetailsImpl o un'eccezione
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<User> user = userRepository.findByUsername(username);
		if (user.isPresent()) {
			return UserDetailsImpl.build(user.get());
		} else {
			throw new UsernameNotFoundException("Utente " + username + "non trovato!");
		}
	}
	public List<User> getAllUser(){
		return (List<User>) userRepository.findAll();
	}

}
