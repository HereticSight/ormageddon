package com.ormageddon.util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.ormageddon.annotations.Entity;
import com.ormageddon.data.CrudOperations;
import com.ormageddon.metamodel.ForeignKeyField;
import com.ormageddon.metamodel.MetaModel;
import com.ormageddon.query.QueryBuilder;
import com.ormageddon.query.Transactions;

public class Configuration {
	
	private Transactions tcl = new Transactions();
	private static Logger logger = Logger.getLogger(Configuration.class);
	private static HashMap<Class<?>, HashSet<Object>> cache;
	private static QueryBuilder qBuilder = new QueryBuilder();
	private static CrudOperations crudOps;
	private static ConnectionPool cPool = new ConnectionPool();
	private static ClassFinder cFinder = new ClassFinder();
	private static Connection conn;
	private static List<MetaModel<Class<?>>> metaModelList;
	private static String PACKAGE_NAME;
	private static Properties props = new Properties();
	private boolean autoCreateTables = true;
	private boolean autoCommit = true;
	
	static {
		try {
			props.load(new FileReader("src\\main\\resources\\application.properties"));
			PACKAGE_NAME = props.getProperty("packageName");
		} catch (FileNotFoundException e) {
			logger.error("Could not find File");
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("There was an IO Error");
			e.printStackTrace();
		}
		
	}
	
	public Configuration() {
		cache = new HashMap<Class<?>, HashSet<Object>>();
		addAllMetaModels();
		try {
			conn = getConnection();
			crudOps = new CrudOperations(conn, autoCommit);
			this.autoCommit = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		persistMetaModels();
		
	}	
	public Configuration(boolean autoCommit) {
		cache = new HashMap<Class<?>, HashSet<Object>>();
		addAllMetaModels();
		try {
			conn = getConnection();
			crudOps = new CrudOperations(conn, autoCommit);
			setAutoCommit(autoCommit);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		persistMetaModels();
		
	}	
	
	public boolean setAutoCommit(boolean autoCommit) {
		crudOps.setAutoCommit(autoCommit);
		if (!crudOps.getAutoCommit()) {
			crudOps.beginTransaction();
		}
		this.autoCommit = autoCommit;
		return this.autoCommit;
	}
	
	public boolean disableAutoCommit() {
		crudOps.setAutoCommit(false);
		crudOps.beginTransaction();
		this.autoCommit = false;
		return this.autoCommit;
	}
	
	public boolean getAutoCommit() {
		return this.autoCommit;
	}
	
	public Connection getConnection() throws SQLException {
		DataSource dSource = cPool.setUpPool();
		
		cPool.printDbStatus();
		System.out.println("======Connecting to the DB=======");
		
		return dSource.getConnection();
	}
	
	public Configuration addAnnotatedClass(Class<?> annotatedClass) {
		metaModelList.add(MetaModel.of(annotatedClass));
		return this;
	}
	
	public List<MetaModel<Class<?>>> getMetaModels() {
		
		return metaModelList;
		
	}
	
	public MetaModel<Class<?>> getMetaModel(Class<?> desired) {
		Optional<MetaModel<Class<?>>> metaModel;
		
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
			crudOps.persistMetaModel(m);
		}
	
	}
	
	public HashMap<Class<?>, HashSet<Object>> getCache() {
		return cache;
	}

	public boolean updateObjectInDB(final Object obj,final String update_columns) {
		
		return crudOps.update(obj, update_columns);
	}
	
	public boolean removeObjectFromDB(final Object obj) {
		return crudOps.remove(obj);
	}
	
	public boolean addObjectToDB(final Object obj) {
		return crudOps.save(obj);
	}
	
	public Optional<List<Object>> getListObjectFromDB(final Class<?> clazz) {
		if (cache.get(clazz) != null && !cache.get(clazz).isEmpty()) {
			logger.info("Retrieved " + clazz.getSimpleName() + " list from cache");
			return Optional.of(cache.get(clazz).stream().collect(Collectors.toList()));
		} else {
			logger.info("Retrieved " + clazz.getSimpleName() + " list from database");
			return crudOps.findAll(clazz);
		}
	}
	
	public Optional<List<Object>> getListObjectFromDB(final Class<?> clazz, String columns, String conditions) {
//		if (!cache.get(clazz).isEmpty() && cache.get(clazz) != null) {
//			MetaModel<Class<?>> metaModel = getMetaModel(clazz);
//			List<String> cols = Arrays.asList(columns.split("\\s*,\\s*"));
//			List<String> cons = Arrays.asList(conditions.split("\\s*,\\s*"));
//			Set<Object> allObjects = cache.get(clazz);
//			
//			for (int i = 0; i < cols.size(); i++) {
//				
//			}
//			
//			List<Object> requested;
//			
//			
//			return Optional.of(null);
//		}
		return crudOps.findBy(clazz, columns, conditions);
	}
	
	public void addAllFromDbToCache(Class<?> clazz) {
		List<Object> list = getListObjectFromDB(clazz).isPresent() ? getListObjectFromDB(clazz).get() : null;
		cache.put(clazz, (HashSet<Object>) list.stream().collect(Collectors.toSet()));
	}
	
	public void rollBackTransaction() {
		if (!autoCommit) {
			crudOps.rollback();
			crudOps.beginTransaction();
		} else {
			logger.warn("Cannot perform Rollback if AutoCommit is set to true.");
		}
	}
	
	public void rollBackToSavePoint(String savePoint) {
		if (!autoCommit) {
			crudOps.rollback(savePoint);
		} else {
			logger.warn("Cannot perform Rollback if AutoCommit is set to true.");
		}
	}
	
	public void releaseSavePoint(String savePoint) {
		if (!autoCommit) {
			crudOps.releaseSavePoint(savePoint);
		} else {
			logger.warn("There are no SavePoints to release if AutoCommit is set to true.");
		}
	}
	
	public void createSavePoint(String savePoint) {
		if (!autoCommit) {
			crudOps.setSavePoint(savePoint);
		} else {
			logger.warn("You cannot set Save Points when AutoCommit is set to true.");
		}
	}
	
	public void commitTransaction() {
		if (!autoCommit) {
			crudOps.sendCommit();
			crudOps.beginTransaction();
		} else {
			logger.warn("AutoCommit is set to true. Transactions are automatically committed as you perform database actions");
		}
	}
	
}
