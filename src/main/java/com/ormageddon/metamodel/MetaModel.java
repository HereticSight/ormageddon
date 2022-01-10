package com.ormageddon.metamodel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.ormageddon.annotations.Column;
import com.ormageddon.annotations.Entity;
import com.ormageddon.annotations.Id;
import com.ormageddon.annotations.JoinColumn;

public class MetaModel<T> {
	
	private Class<?> clazz;
	private PrimaryKeyField primaryKeyField;
	private List<ColumnField> columnFields;
	private List<ForeignKeyField> foreignKeyFields;
	
	// Constructor for MetaModel. Only called via MetaModel.of(clazz).
	public MetaModel(Class<?> clazz) {
		this.clazz = clazz;
		this.primaryKeyField = setPrimaryKey();
		this.columnFields = setColumns();
		this.foreignKeyFields = setForeignKeys();
	}
	
	// Create MetaModel out of a class
	public static MetaModel<Class<?>> of(Class<?> clazz) {
		
		// we check that the class we're passing through has the @Entity annotation
		if (clazz.getAnnotation(Entity.class) == null) {
			throw new IllegalStateException("Cannot create MetaModel object! Provided class "
					+ clazz.getName() + " is not annotated with @Entity");
			
		}
		// if so....return a new MetaModel object of the class passed through 
		return new MetaModel<>(clazz);
	}
	
	/**
	 *  This method will iterate through all of the clazz's declared fields
	 *  and return the first field annotated with @Id
	 *  @return the primary key field
	 */
	public PrimaryKeyField setPrimaryKey() {
		Field[] fields = clazz.getDeclaredFields();
		
		for (Field field : fields) {
			Id pk = field.getAnnotation(Id.class);
			
			if (pk != null) {
				primaryKeyField = new PrimaryKeyField(field);
				return primaryKeyField;
			}
			
		}
		
		throw new RuntimeException ("No Primary Key found in " + clazz.getName());
	}
	
	public PrimaryKeyField getPrimaryKey() {
		return primaryKeyField;
	}
	
	/**
	 *  This method will iterate through all of the clazz's declared fields
	 *  and add all fields annotated with @Column to the columnFields attribute
	 *  @return the list of all of the ColumnFields
	 */
	public List<ColumnField> setColumns() {
		if (columnFields == null) {
			columnFields = new LinkedList<ColumnField>();
		}
		Field[] fields = clazz.getDeclaredFields();
		
		for (Field field : fields) {
			Column column = field.getAnnotation(Column.class);
			
			if (column != null) {
				columnFields.add(new ColumnField(field));
			}
			
		}
		
		if (columnFields.isEmpty()) {
			throw new RuntimeException ("No columns found in: " + clazz.getName());
		}
		
		return columnFields;
	}
	
	public List<ColumnField> getColumns() {
		return columnFields;
	}
	
	public ColumnField getColumn(String columnName) {
		Optional<ColumnField> maybe = getColumns().stream()
												  .filter( m -> m.getColumnName().equals(columnName))
												  .findFirst();
		return maybe.isPresent() ? maybe.get() : null;
	}
	
	
	
	/**
	 *  This method will iterate through all of the clazz's declared fields
	 *  and add all fields annotated with @JoinColumn to the columnFields attribute
	 *  @return the list of all the ForeignKeyFields
	 */
	public List<ForeignKeyField> setForeignKeys() {
		if (foreignKeyFields == null) {
			foreignKeyFields = new LinkedList<ForeignKeyField>();
		}
		
		Field[] fields = clazz.getDeclaredFields();
		
		for (Field field : fields) {
			JoinColumn foreignKey = field.getAnnotation(JoinColumn.class);
			
			if (foreignKey != null) {
				foreignKeyFields.add(new ForeignKeyField(field));
			}
			
		}
		
		return foreignKeyFields;
	}
	
	public List<ForeignKeyField> getForeignKeys() {
		return foreignKeyFields;
	}
	
	public ForeignKeyField getForeignKey(String columnName) {
		
		Optional<ForeignKeyField> maybe = getForeignKeys().stream()
														  .filter( m -> m.getColumnName().equals(columnName))
														  .findFirst();
		
		return maybe.isPresent() ? maybe.get() : null;
	}
	
	public List<String> getColumnNameList() {
		List<String> columns = getColumns().stream().map(c-> c.getColumnName()).collect(Collectors.toList());
		columns.addAll(getForeignKeys().stream().map(f-> f.getColumnName()).collect(Collectors.toList()));
		
		return columns;
	}
	public List<String> getColumnNameListNoFk() {
		List<String> columns = getColumns().stream().map(c-> c.getColumnName()).collect(Collectors.toList());
		
		return columns;
	}
	
	public List<String> getColumnNameListWithId() {
		List<String> columns = new ArrayList<String>();
		columns.add(getPrimaryKey().getColumnName());
		columns.addAll(getColumns().stream().map(c-> c.getColumnName()).collect(Collectors.toList()));
		columns.addAll(getForeignKeys().stream().map(f-> f.getColumnName()).collect(Collectors.toList()));
		
		return columns;
	}
	
	public Class<?> getClazz() {
		return this.clazz;
	}
	
	public String getSimpleClassName() {
		return clazz.getSimpleName();
	}

	public String getClassName() {
		return clazz.getName();
	}

	public String getTableName() {
		return !this.clazz.getAnnotation(Entity.class).name().isBlank() ? this.clazz.getAnnotation(Entity.class).name() : this.getSimpleClassName().replaceAll("([a-z])([A-Z]+)","$1_$2").toLowerCase() + "s" ;
	}
	
}
