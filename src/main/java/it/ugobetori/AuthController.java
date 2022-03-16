package it.ugobetori;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.ugobetori.security.JwtUtils;
import it.ugobetori.security.service.UserDetailsImpl;
import it.ugobetori.payload.request.*;
import it.ugobetori.payload.response.*;
import it.ugobetori.repositories.RoleRepository;
import it.ugobetori.repositories.UserRepository;

@CrossOrigin(origins="*")
@RestController
@RequestMapping("/api")
public class AuthController {
	
	@Autowired
	AuthenticationManager authenticationManager;	
	@Autowired
	UserRepository userRepository;
	@Autowired
	RoleRepository roleRepository;
	
	@Autowired
	JwtUtils jwtUtils;
	
	@PostMapping("/login")
	public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
		
		// Usa l'AuthenticationManager per autenticare i parametri della request
		Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
		// Ottiene i privilegi dell'utente
		authentication.getAuthorities();
		
		// Ottiene il SecurityContext
		SecurityContextHolder.getContext().setAuthentication(authentication);
		// Genera il token
		String jwt = jwtUtils.generateJwtToken(authentication);
		
		// Ottiene i dati dell'utente 
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		
		// Ottiene i ruoli dell'utente
		List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority()).collect(Collectors.toList());
		
		// Restituisce la response con status 200, token e dati dell'utente
		return ResponseEntity.ok(new LoginResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roles, userDetails.getExpirationTime()));
	}
	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest) {
		
		// Verifica l'esistenza di Username e Email già registrate
		if (userRepository.existsByUsername(signUpRequest.getUsername())) {
			return ResponseEntity.badRequest().body(new SignupResponse("Errore: Username già in uso!"));
		}
		if (userRepository.existsByEmail(signUpRequest.getEmail())) {
			return ResponseEntity.badRequest().body(new SignupResponse("Errore: Email già in uso!"));
		}
		
		// Crea un nuovo user codificando la password
		User user = new User(null, signUpRequest.getUsername(), signUpRequest.getEmail(), signUpRequest.getPassword(), signUpRequest.getNome(), signUpRequest.getCognome());
		Set<String> strRoles = signUpRequest.getRole();
		Set<Role> roles = new HashSet<>();
		// Verifica eventuali Role presenti nella request e li assegna allo user. In caso nullo assegna il ROLE_USER
				if (strRoles == null) {
					Role userRole = roleRepository.findByRoleType(RoleType.ROLE_USER).orElseThrow(() -> new RuntimeException("Errore: Role non trovato!"));
					roles.add(userRole);
				} else {
					strRoles.forEach(role -> {
						switch (role) {
						case "admin":
							Role adminRole = roleRepository.findByRoleType(RoleType.ROLE_ADMIN).orElseThrow(() -> new RuntimeException("Errore: Role non trovato!"));
							roles.add(adminRole);
							break;
						default:
							Role userRole = roleRepository.findByRoleType(RoleType.ROLE_USER).orElseThrow(() -> new RuntimeException("Errore: Role non trovato!"));
							roles.add(userRole);
						}
					});
				}
				user.setRoles(roles);
				userRepository.save(user);
				return ResponseEntity.ok(new SignupResponse("User registrato con successo!"));
			}

}
