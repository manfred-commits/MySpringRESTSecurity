package it.ugobetori.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.ugobetori.User;
import it.ugobetori.security.service.UserDetailsServiceImpl;

//@CrossOrigin(origins="http://localhost:8080, http://pippo.com", maxAge=3600)
@CrossOrigin(origins="*")
@RestController
@RequestMapping("api/test")
public class TestController {
	
	@Autowired UserDetailsServiceImpl userDetailsServiceImpl; 
	@GetMapping("/all")
	public String allAccess() {
		
		return "Endpoint con accesso Pubblico";
	}
	@GetMapping("/allusers")
	public List<User> getAllUsers() {
		
		return userDetailsServiceImpl.getAllUser();
	}
	
	@GetMapping("/user")
	// Verifica l'autorizzazione prima di entrare nel metodo in base al ruolo
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public String userAccess() {
		return "Endopoint con accesso User e Admin.";
	}

	@GetMapping("/admin")
	@PreAuthorize("hasRole('ADMIN')")
	public String adminAccess() {
		return "Endpoint con accesso Admin";
	}

}
