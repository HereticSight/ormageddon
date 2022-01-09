package com.ormageddon;

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
		Configuration config = new Configuration();
		CrudOperations crudRepo = new CrudOperations(config);
		QueryBuilder queryBuilder = new QueryBuilder();
		
		Home house = new Home("123 fake street", new LinkedList<User>());
		User testUser = new User(2, "mosizlak", "123abc", house);
		User testUser2 = new User(3, "janeschmoe", "123abc", house);
		
		house.getInhabitants().add(testUser);
		house.getInhabitants().add(testUser2);
		
//		MetaModel<Class<?>> metaModel = MetaModel.of(User.class);
//		System.out.println(metaModel.getColumn("pwd"));
		
//		System.out.println(MetaModel.of(User.class).getColumnNameList());
		System.out.println(queryBuilder.createTableIfNotExists(MetaModel.of(User.class)));
//		System.out.println(queryBuilder.createTableIfNotExists(MetaModel.of(Home.class)));
//		System.out.println(queryBuilder.saveQuery(MetaModel.of(User.class)));
//		System.out.println(queryBuilder.updateQuery(MetaModel.of(User.class)));
//		System.out.println(queryBuilder.deleteQuery(MetaModel.of(User.class)));
		
//		crudRepo.update(testUser,"username      ,     pwd");
//		System.out.println(crudRepo.findAll(User.class));
//		System.out.println(crudRepo.remove(testUser2));
		
		System.out.println(crudRepo.findBy(User.class,"id,username","2,mosizlak"));
		
		
		
	}
}
