package com.ormageddon.metamodel;

import java.lang.reflect.Field;
import java.util.Objects;

import com.ormageddon.annotations.JoinColumn;

public class ForeignKeyField {
	
	private Field field;
	private MetaModel<Class<?>> reference;
	
	// generate ForeignKeyField by passing fields with @JoinColumn
	public ForeignKeyField(Field field) {
		
		if (field.getAnnotation(JoinColumn.class) == null) {
			throw new IllegalStateException("Cannot Create ForeignKeyField object! Provided field, " +
					  						getName() + "is not annotated with @JoinColumn");
		}
		this.field = field;
		setReference();
	}
	
	public Field getField() {
		return this.field;
	}
	
	// get field name
	public String getName() {
		return field.getName();
	}
	
	public Object getValue(Object obj) throws IllegalArgumentException, IllegalAccessException {
		field.setAccessible(true);
		return field.get(obj);
	}
	
	// get field type
	public Class<?> getType() {
		return field.getType();
	}
	
	// get column name from annotation. If blank, create snake case column name using field name
	public String getColumnName() {
		return !field.getAnnotation(JoinColumn.class).name().isBlank() ? field.getAnnotation(JoinColumn.class).name() : field.getName().replaceAll("([a-z])([A-Z]+)","$1_$2").toLowerCase() + "_id";
	}
	
	// get reference from annotation
	public MetaModel<Class<?>> setReference() {
		reference = MetaModel.of(field.getAnnotation(JoinColumn.class).references());
		return reference;
	}
	
	public MetaModel<Class<?>> getReference() {
		return reference;
	}

	@Override
	public int hashCode() {
		return Objects.hash(field);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ForeignKeyField other = (ForeignKeyField) obj;
		return Objects.equals(field, other.field);
	}
	
}
