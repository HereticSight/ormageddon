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

import com.ormageddon.annotations.Column;
import com.ormageddon.annotations.Id;
import com.ormageddon.annotations.JoinColumn;
import com.ormageddon.metamodel.MetaModel;
import com.ormageddon.query.QueryBuilder;
import com.ormageddon.util.Configuration;

public class CrudOperations implements OrmRepository<Object> {
	
	private Configuration config;
	private static QueryBuilder qBuild = new QueryBuilder();
	
	public CrudOperations(Configuration config) {
		this.config = config;
	}
	
//	/**
//
//	Class<T> clazz - a list of object types you want to be fetched
//	ResultSet resultSet - pointer to your retrieved results 
//
//	*/
//
//	    List<Field> fields = Arrays.asList(clazz.getDeclaredFields());
//	    for(Field field: fields) {
//	        field.setAccessible(true);
//	    }
//
//	    List<T> list = new ArrayList<>(); 
//    while(resultSet.next()) {
//
//        T dto = clazz.getConstructor().newInstance();
//
//        for(Field field: fields) {
//            Col col = field.getAnnotation(Col.class);
//            if(col!=null) {
//                String name = col.name();
//                try{
//                    String value = resultSet.getString(name);
//                    field.set(dto, field.getType().getConstructor(String.class).newInstance(value));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        list.add(dto);
//
//    }
	
	@Override
	public Optional<List<Object>> findAll(Class<?> clazz) {
		try (Connection conn = config.getConnection()) {
			List<Object> objects = new LinkedList<Object>();
			List<Field> fields = Arrays.asList(clazz.getDeclaredFields());
			for (Field field : fields) {
				field.setAccessible(true);
			}
			List<Object> list = new ArrayList<>();
			Statement stmt = conn.createStatement();
			MetaModel<Class<?>> metaModel = config.getMetaModel(clazz);
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
			config.addAllFromDbToCache(clazz);
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
		try (Connection conn = config.getConnection()) {
			List<String> cols = Arrays.asList(columns.split("\\s*,\\s*"));
			List<String> cons = Arrays.asList(conditions.split("\\s*,\\s*"));
			List<Field> fields = Arrays.asList(clazz.getDeclaredFields());
			for (Field field : fields) {
				field.setAccessible(true);
			}
			List<Object> list = new ArrayList<>();
			Statement stmt = conn.createStatement();
			MetaModel<Class<?>> metaModel = config.getMetaModel(clazz);
			String sql = qBuild.findByQuery(metaModel);
			List<String> query = new ArrayList<String>();
			for (int i = 0; i < cols.size(); i++ ) {
				if (cols.get(i) == "id") {
					query.add(cols.get(i) + " = " + cons.get(i));
				} else if (metaModel.getColumn(cols.get(i)) != null && metaModel.getColumn(cols.get(i)).getType() == String.class) {
					query.add(cols.get(i) + " = '" + cons.get(i) + "'");
				} else {
					query.add(cols.get(i) + " = " + cons.get(i));				
				}
			}
			sql += query.stream().collect(Collectors.joining(" AND ")) + (";");
			System.out.println(sql);
			
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


//	@Override
//	public Optional<Object> findBy() {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public int save(Object model) {
		
		return 0;
	}

	@Override
	public boolean remove(Object model) {
		try (Connection conn = config.getConnection()) {
			MetaModel<Class<?>> metaModel = config.getMetaModel(model.getClass());
			String sql = qBuild.deleteQuery(metaModel);
			sql += metaModel.getPrimaryKey().getColumnName() + " = " + metaModel.getPrimaryKey().getValue(model) + ";";
			
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(sql);
			return true;
			
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
	}

	@Override
	public boolean update(Object model, String columns) {
		MetaModel<Class<?>> metaModel = config.getMetaModel(model.getClass());
		String[] desiredColumns = columns.split("\\s*,\\s*");
		String sql = qBuild.updateQuery(metaModel);
		List<String> sqlUpdate = new ArrayList<String>();
		for ( String string : desiredColumns) {
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
			sql += "WHERE " + metaModel.getPrimaryKey().getColumnName() + " = " + metaModel.getPrimaryKey().getValue(model);
			sql += ";";
			System.out.println(sql);
		} catch (IllegalArgumentException e1) {
			e1.printStackTrace();
			return false;
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
			return false;
		}
		
		
		try (Connection conn = config.getConnection()) {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(sql);
			return true;
			
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
	}

}
