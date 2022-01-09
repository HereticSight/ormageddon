package com.ormageddon.util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import com.ormageddon.annotations.Entity;
import com.ormageddon.data.CrudOperations;
import com.ormageddon.metamodel.ForeignKeyField;
import com.ormageddon.metamodel.MetaModel;
import com.ormageddon.query.QueryBuilder;

public class Configuration {
	
	private HashMap<Class<?>, HashSet<Object>> cache;
	private QueryBuilder qBuilder = new QueryBuilder();
	private CrudOperations crudOps;
	private ConnectionPool cPool = new ConnectionPool();
	private ClassFinder cFinder = new ClassFinder();
	private Connection conn = null;
	private List<MetaModel<Class<?>>> metaModelList;
	private static String PACKAGE_NAME;
	private static Properties props = new Properties();
	private boolean autoCreateTables = true;
	
	static {
		try {
			props.load(new FileReader("src\\main\\resources\\application.properties"));
			PACKAGE_NAME = props.getProperty("packageName");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public Configuration() {
		addAllMetaModels();
		cache = new HashMap<Class<?>, HashSet<Object>>();
//		persistMetaModels();
	}
	
	
	public Connection getConnection() throws SQLException {
		DataSource dSource = cPool.setUpPool();
		
		cPool.printDbStatus();
		System.out.println("======Connecting to the DB=======");
		
		return dSource.getConnection();
	}
	
	public Configuration addAnnotatedClass(Class annotatedClass) {
		
		metaModelList.add(MetaModel.of(annotatedClass));
		
		return this;
	}
	
	public List<MetaModel<Class<?>>> getMetaModels() {
		
		return metaModelList;
		
	}
	
	public MetaModel<Class<?>> getMetaModel(Class desired) {
		Optional<MetaModel<Class<?>>> metaModel = null;
		
		metaModel = metaModelList.stream().filter( m -> m.getSimpleClassName().equals(desired.getSimpleName()))
										  .findFirst();
		
		return metaModel.isPresent() ? metaModel.get() : null;
	}
	
	public List<MetaModel<Class<?>>> addAllMetaModels() {
		
		if (metaModelList == null) {
			metaModelList = new LinkedList<MetaModel<Class<?>>>();
		}
		
		Set<Class<?>> packageClasses = cFinder.findAllClasses(PACKAGE_NAME);
		
		packageClasses.stream()
						.filter(c -> c.getAnnotation(Entity.class) != null)
						.forEach(c -> addAnnotatedClass(c));
		 return metaModelList;
	}
	
	public void persistMetaModels() {
		
		for (MetaModel<Class<?>> m : metaModelList) {
			
			persistMetaModel(m);
		}
	
	}
	
	public boolean persistMetaModel(MetaModel<Class<?>> clazz) {
		if (!clazz.getForeignKeys().isEmpty()) {
			for (ForeignKeyField fk : clazz.getForeignKeys()) {
				persistMetaModel(fk.getReference());
			}
		}
		qBuilder.createTableIfNotExists(clazz);
		
		return false;
	}
	
	public HashMap<Class<?>, HashSet<Object>> getCache() {
		return cache;
	}

	public boolean UpdateObjectInDB(final Object obj,final String update_columns) {
		
		return false;
	}
	
	public boolean removeObjectFromDB(final Object obj) {
		return false;
	}
	
	public boolean addObjectToDB(final Object obj) {
		return false;
	}
	
	public Optional<List<Object>> getListObjectFromDB(final Class<?> clazz) {
		if (!cache.get(clazz).isEmpty() && cache.get(clazz) != null) {
			return Optional.of(cache.get(clazz).stream().collect(Collectors.toList()));
		}
		return crudOps.findAll(clazz);
	}
	
	public Optional<List<Object>> getListObjectFromDB(final Class<?> clazz, String columns, String conditions) {
		if (!cache.get(clazz).isEmpty() && cache.get(clazz) != null) {
			MetaModel<Class<?>> metaModel = getMetaModel(clazz);
			List<String> cols = Arrays.asList(columns.split("\\s*,\\s*"));
			List<String> cons = Arrays.asList(conditions.split("\\s*,\\s*"));
			Set<Object> allObjects = cache.get(clazz);
			
			for (int i = 0; i < cols.size(); i++) {
				
			}
			
			List<Object> requested;
			
			
			
			
			
			return Optional.of(null);
		}
		
		return crudOps.findBy(clazz, columns, conditions);
	}
	
	
	
	public void addAllFromDbToCache(Class<?> clazz) {
		List<Object> list = !getListObjectFromDB(clazz).isPresent() ? getListObjectFromDB(clazz).get() : null;
		cache.put(clazz, (HashSet<Object>) list.stream().collect(Collectors.toSet()));
	}

}
