package com.ormageddon.model;

import java.util.List;
import java.util.Objects;

import com.ormageddon.annotations.Column;
import com.ormageddon.annotations.Entity;
import com.ormageddon.annotations.Id;

@Entity
public class Home {
	
	@Id
	private int id;

	@Column(name="address", notNull=true)
	private String address;

	@Column(name="owned")
	private boolean owned;

	private List<User> inhabitants;
	
	public Home() {
		
	}
	
	public Home(int id, String address, List<User> inhabitants) {
		super();
		this.id = id;
		this.address = address;
		this.inhabitants = inhabitants;
	}
	
	
	public Home(String address, List<User> inhabitants) {
		super();
		this.address = address;
		this.inhabitants = inhabitants;
	}
	
	public Home(int id, String address) {
		super();
		this.id = id;
		this.address = address;
	}
	
	public Home(String address) {
		super();
		this.address = address;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public List<User> getInhabitants() {
		return inhabitants;
	}

	public void setInhabitants(List<User> inhabitants) {
		this.inhabitants = inhabitants;
	}

	@Override
	public int hashCode() {
		return Objects.hash(address, id, inhabitants);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Home other = (Home) obj;
		return Objects.equals(address, other.address) && id == other.id
				&& Objects.equals(inhabitants, other.inhabitants);
	}

	@Override
	public String toString() {
		return "Home [id=" + id + ", address=" + address + ", inhabitants=" + inhabitants + "]";
	}
	
	
	
}
