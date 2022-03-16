package it.ugobetori;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import lombok.Data;

/*
Una classe può essere annotata con @EqualsAndHashCode per consentire a lombok di generare implementazioni 
dei metodi equals(Object other) e hashCode().
@Data, genera getter per tutti i campi, un utile metodo toString e implementazioni hashCode e equals che controllano 
tutti i campi non transent (cioè che non dovrebbero far parte della serializzazione). 
Genererà anche setter per tutti i campi non finali, oltre a un costruttore.
Equivale a @Getter @Setter @RequiredArgsConstructor @ToString @EqualsAndHashCode.
*/
//notazione di lombok
@Data
@Entity
@Table(name = "users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String username;
	private String nome;
	private String cognome;
	private String email;
	private String password;
	private Boolean active = true;

	@ManyToMany
	//@JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	@JoinTable(name = "user_roles")
	private Set<Role> roles = new HashSet<>();
	public User() { }
	/*
	// Costruttore default 
	
	 
	public User( String name, String mySurname, int age) { 
		this.name = name;
		this.mySurname = mySurname; 
		this.age = age; 
	}
	*/
	public User(Long id, String username,  String email, String password, String nome, String cognome) {
		super();
		this.id = id;
		this.username = username;
		this.email = email;
		this.password = password;
		this.nome = nome;
		this.cognome = cognome;
	}

	@Override
	public String toString() {
		return String.format("User: myId=%d, myName=%s, mySurname=%s", id, nome, username);
	}

}
