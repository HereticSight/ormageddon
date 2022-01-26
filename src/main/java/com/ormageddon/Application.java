package com.ormageddon;

import java.util.LinkedList;

import com.ormageddon.model.Home;
import com.ormageddon.model.User;
import com.ormageddon.util.Configuration;

public class Application {

	public static void main(String[] args) {
		Configuration config = new Configuration(false);

		Home house = new Home("123 fake street", new LinkedList<User>());
		Home house2 = new Home("114 cake street", new LinkedList<User>());
		User testUser = new User("mosizlak", "123abc", house);
		User testUser2 = new User("janeschmoe", "123abc", house);
		User testUser3 = new User("mody", "pass", house2);
		User testUser4 = new User("brody", "pass", house2);
		User testUser5 = new User("cody", "pass", null);
		
		house.setOwned(true);
		house.getInhabitants().add(testUser);
		house.getInhabitants().add(testUser2);
		house2.getInhabitants().add(testUser3);
		house2.getInhabitants().add(testUser4);
		
		
		config.addObjectToDB(testUser);
		config.addObjectToDB(testUser2);
		config.createSavePoint("new");
		config.addObjectToDB(testUser3);
		config.rollBackToSavePoint("new");
		config.addObjectToDB(testUser4);
		config.releaseSavePoint("new");
		config.addObjectToDB(house);
		config.addObjectToDB(house2);
		config.commitTransaction();
		
		testUser4.setId(3);
		testUser4.setPassword("meep");
		
		config.updateObjectInDB(testUser4, "pwd");
		config.commitTransaction();
		config.addObjectToDB(testUser5);
		config.rollBackTransaction();
		
		
		
		
		
		config.addAllFromDbToCache(User.class);
		System.out.println(config.getListObjectFromDB(User.class));
		System.out.println(config.getListObjectFromDB(Home.class));
		System.out.println(config.getListObjectFromDB(testUser4.getClass(), "username", "brody"));
		System.out.println(config.getListObjectFromDB(User.class, "username", "mody"));
		
		
		String word = "hello";
	}
}
