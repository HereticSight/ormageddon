package com.ormageddon.data;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.ormageddon.annotations.Column;
import com.ormageddon.annotations.Id;
import com.ormageddon.annotations.JoinColumn;
import com.ormageddon.metamodel.ForeignKeyField;
import com.ormageddon.metamodel.MetaModel;
import com.ormageddon.query.QueryBuilder;

public class CrudOperations implements OrmRepository<Object> {
	
	private static Logger logger = Logger.getLogger(CrudOperations.class);
	private static LinkedList<String> transaction = new LinkedList<String>();
	private Connection conn;
	private static QueryBuilder qBuild = new QueryBuilder();
	private boolean autoCommit = true;

	public CrudOperations(Connection conn, boolean autoCommit) {
		this.conn = conn;
		this.autoCommit = autoCommit;
	}
	
	public boolean getAutoCommit() {
		return this.autoCommit;
	}
	
	public boolean setAutoCommit(boolean autoCommit) {
		return this.autoCommit = autoCommit;
	}

	public boolean persistMetaModel(MetaModel<Class<?>> metaModel) {
		if (metaModel != null) {			
			if (metaModel.getForeignKeys() != null && !metaModel.getForeignKeys().isEmpty()) {
				for (ForeignKeyField fk : metaModel.getForeignKeys()) {
					persistMetaModel(fk.getReference());
				}
			}
			String sql = qBuild.createTableIfNotExists(metaModel);
			try {
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(sql);
				logger.info("Created or (skipped creation if it already exists) of " + metaModel.getSimpleClassName() + " table.");
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error("SQL Exception thrown. Table creation failed.");
				return false;
			}
		}
		return false;

	}

	@Override
	public Optional<List<Object>> findAll(Class<?> clazz) {
		try {
			List<Object> objects = new LinkedList<Object>();
			List<Field> fields = Arrays.asList(clazz.getDeclaredFields());
			for (Field field : fields) {
				field.setAccessible(true);
			}
			List<Object> list = new ArrayList<>();
			Statement stmt = conn.createStatement();
			MetaModel<Class<?>> metaModel = MetaModel.of(clazz);
			String sql = qBuild.findAllQuery(metaModel);

			ResultSet rs;

			if ((rs = stmt.executeQuery(sql)) != null) {
				ResultSetMetaData rsmd = rs.getMetaData();
				int columnCount = rsmd.getColumnCount();

				while (rs.next()) {
					Object newObject = clazz.getConstructor().newInstance();
					for (Field field : fields) {
						Column col = field.getAnnotation(Column.class);
						Id id = field.getAnnotation(Id.class);
						JoinColumn fk = field.getAnnotation(JoinColumn.class);
						String name;

						if (id != null) {
							name = id.name();
							if (rs.getObject(name) != null) {
								Object value = rs.getObject(name);
								field.set(newObject, value);
							}
						}

						if (col != null) {
							name = col.name();
							if (rs.getObject(name) != null) {
								Object value = rs.getObject(name);
								field.set(newObject, value);
							}
						}

//						if (fk != null) {
//							name = fk.name();
//							if (rs.getObject(name) != null) {
//								Object value = rs.getObject(name);
//								field.set(newObject, value);
//							}
//						}
					}

					list.add(newObject);

				}

			}
			return Optional.of(list);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	@Override
	public Optional<List<Object>> findBy(Class<?> clazz, String columns, String conditions) {
		try {
			List<String> cols = Arrays.asList(columns.split("\\s*,\\s*"));
			List<String> cons = Arrays.asList(conditions.split("\\s*,\\s*"));
			List<Field> fields = Arrays.asList(clazz.getDeclaredFields());
			for (Field field : fields) {
				field.setAccessible(true);
			}
			List<Object> list = new ArrayList<>();
			Statement stmt = conn.createStatement();
			MetaModel<Class<?>> metaModel = MetaModel.of(clazz);
			String sql = qBuild.findByQuery(metaModel);
			List<String> query = new ArrayList<String>();
			for (int i = 0; i < cols.size(); i++) {
				if (cols.get(i) == "id") {
					query.add(cols.get(i) + " = " + cons.get(i));
				} else if (metaModel.getColumn(cols.get(i)) != null
						&& metaModel.getColumn(cols.get(i)).getType() == String.class) {
					query.add(cols.get(i) + " = '" + cons.get(i) + "'");
				} else {
					query.add(cols.get(i) + " = " + cons.get(i));
				}
			}
			sql += query.stream().collect(Collectors.joining(" AND ")) + (";");

			ResultSet rs;

			if ((rs = stmt.executeQuery(sql)) != null) {
				ResultSetMetaData rsmd = rs.getMetaData();
				int columnCount = rsmd.getColumnCount();

				while (rs.next()) {
					Object newObject = clazz.getConstructor().newInstance();
					for (Field field : fields) {
						Column col = field.getAnnotation(Column.class);
						Id id = field.getAnnotation(Id.class);
						JoinColumn fk = field.getAnnotation(JoinColumn.class);
						String name;

						if (id != null) {
							name = id.name();
							if (rs.getObject(name) != null) {
								Object value = rs.getObject(name);
								field.set(newObject, value);
							}
						}
						if (col != null) {
							name = col.name();
							if (rs.getObject(name) != null) {
								Object value = rs.getObject(name);
								field.set(newObject, value);
							}
						}
//						if (fk != null) {
//							name = fk.name();
//							if (rs.getObject(name) != null) {
//								Object value = rs.getObject(name);
//								field.set(newObject, value);
//							}
//						}
					}

					list.add(newObject);

				}

			}
			return Optional.of(list);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	@Override
	public boolean remove(Object model) {
		try {
			MetaModel<Class<?>> metaModel = MetaModel.of(model.getClass());
			String sql = qBuild.deleteQuery(metaModel);
			sql += metaModel.getPrimaryKey().getColumnName() + " = " + metaModel.getPrimaryKey().getValue(model) + ";";

			if (!autoCommit) {
				transaction.add(sql);
				logger.info("Delete Query for " + metaModel.getSimpleClassName() + " added to the transaction." );
				return true;
			} else {

				Statement stmt = conn.createStatement();
				stmt.executeUpdate(sql);
				return true;

			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return false;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return false;
		}

	}

	public boolean save(Object model) {
		MetaModel<Class<?>> metaModel = MetaModel.of(model.getClass());
		String sql = qBuild.saveQuery(metaModel);
		List<String> sqlUpdate = new ArrayList<String>();
		for (String string : metaModel.getColumnNameListNoFk()) {
			try {
				if (metaModel.getColumn(string) != null) {
					if (metaModel.getColumn(string) != null
							&& metaModel.getColumn(string).getType().equals(String.class)) {
						sqlUpdate.add("'" + metaModel.getColumn(string).getValue(model) + "'");
					} else {
						sqlUpdate.add((String) metaModel.getColumn(string).getValue(model));
					}
				}
				if (metaModel.getForeignKey(string) != null) {
					if (metaModel.getColumn(string) != null) {
						sqlUpdate.add((String) metaModel.getColumn(string).getValue(model));
					}
				}

			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				return false;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				return false;
			}
		}

		sql += sqlUpdate.stream().collect(Collectors.joining(" , ")) + ") ON CONFLICT DO NOTHING;";
		if (!autoCommit) {
			transaction.add(sql);
			logger.info("Insert Query for " + model.getClass().getSimpleName() + " added to the transaction");
			return true;
		} else {
			
			try {
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(sql);
				return true;
				
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
			
		}

	}

	public boolean update(Object model, String columns) {
		MetaModel<Class<?>> metaModel = MetaModel.of(model.getClass());
		String[] desiredColumns = columns.split("\\s*,\\s*");
		String sql = qBuild.updateQuery(metaModel);
		
			List<String> sqlUpdate = new ArrayList<String>();
			for (String string : desiredColumns) {
				try {
					if (metaModel.getColumn(string) != null) {
						if (metaModel.getColumn(string).getType().equals(String.class)) {
							sqlUpdate.add(string + " = '" + metaModel.getColumn(string).getValue(model) + "'");
						} else {
							sqlUpdate.add(string + " = " + metaModel.getColumn(string).getValue(model));
						}
					}
					if (metaModel.getForeignKey(string) != null) {
						if (metaModel.getColumn(string).getType().equals(String.class)) {
							sqlUpdate.add(string + " = " + metaModel.getColumn(string).getValue(model));
						}
					}
					
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					return false;
				} catch (IllegalAccessException e) {
					e.printStackTrace();
					return false;
				}
			}
			
			sql += sqlUpdate.stream().collect(Collectors.joining(",\r\n")) + " \r\n";
			
			try {
				sql += "WHERE " + metaModel.getPrimaryKey().getColumnName() + " = "
						+ metaModel.getPrimaryKey().getValue(model);
				sql += ";";
				System.out.println(sql);
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
				return false;
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
				return false;
			}
		if (!autoCommit){
			transaction.add(sql);
			logger.info("Update Query for " + model.getClass().getSimpleName() + " added to the transaction");
			return true;
		} else {
			
			try {
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(sql);
				return true;
				
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}


	}

	public void beginTransaction() {
		transaction = new LinkedList<String>();
		transaction.add("BEGIN;");
	}

	public void rollback() {
		transaction.clear();
	};

	public void rollback(String savePoint) {
		if (transaction.contains("SAVEPOINT " + savePoint + ";")) {
				while (!transaction.getLast().equals("SAVEPOINT " + savePoint + ";")) {
					System.out.println(transaction.getLast());
					transaction.removeLast();
				}
			logger.info("Rolled back to SavePoint: " + savePoint);
		}
	}

	public void setSavePoint(String savePoint) {
		if (!transaction.contains("SAVEPOINT " + savePoint + ";")) {
			transaction.add("SAVEPOINT " + savePoint + ";");
			logger.info("SavePoint has been set as " + savePoint);
		} else {
			logger.warn("SavePoint " + savePoint + " already exists. Please set a different one.");
		}
	}

	public void releaseSavePoint(String savePoint) {
		if(transaction.contains("SAVEPOINT " + savePoint + ";")) {
			transaction.remove("SAVEPOINT " + savePoint + ";");
			logger.info("SavePoint" + savePoint + " has been released");
		} else {
			logger.warn("SavePoint " + savePoint + " does not exist");
		}
	}

	public void setCommit() {
		transaction.add("COMMIT;");
	}
	
	public void sendCommit() {
		setCommit();
		String sql = transaction.stream().collect(Collectors.joining("\r\n"));
		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(sql);
			logger.info("Transaction has been committed to the database");
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("SQL error has been thrown. Unable to perform transaction.");
		}
		
	}

}
