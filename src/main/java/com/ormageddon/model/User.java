package com.ormageddon.model;

import java.util.Objects;

import com.ormageddon.annotations.Column;
import com.ormageddon.annotations.Entity;
import com.ormageddon.annotations.Id;
import com.ormageddon.annotations.JoinColumn;

@Entity
public class User {
	
	@Id
	private int id;
	
	@Column(name="username", notNull=true, unique=true)
	private String username;
	
	@Column(name="pwd", notNull=true)
	private String password;
	
	@JoinColumn(name="home_id", references=Home.class)
	private Home home;
	
	public User() {
		
	}
	
	public User(int id, String username, String password, Home home) {
		super();
		this.id = id;
		this.username = username;
		this.password = password;
		this.home = home;
	}

	public User(String username, String password, Home home) {
		super();
		this.username = username;
		this.password = password;
		this.home = home;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Home getHome() {
		return home;
	}

	public void setHome(Home home) {
		this.home = home;
	}

	@Override
	public int hashCode() {
		return Objects.hash(home, id, password, username);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		return Objects.equals(home, other.home) && id == other.id && Objects.equals(password, other.password)
				&& Objects.equals(username, other.username);
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", username=" + username + ", password=" + password + ", home=" + home + "]";
	}
	
	
}
