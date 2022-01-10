package com.ormageddon;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

import com.ormageddon.data.CrudOperations;
import com.ormageddon.data.OrmRepository;
import com.ormageddon.metamodel.MetaModel;
import com.ormageddon.model.Home;
import com.ormageddon.model.User;
import com.ormageddon.query.QueryBuilder;
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
		config.addObjectToDB(testUser5);
		
		
		

		System.out.println(config.getListObjectFromDB(User.class, "username", "mody"));
		System.out.println(config.getListObjectFromDB(User.class));
	}
}
