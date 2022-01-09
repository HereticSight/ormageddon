package com.ormageddon.metamodel;

import java.lang.reflect.Field;
import java.util.Objects;

import com.ormageddon.annotations.Id;

public class PrimaryKeyField {
	
	private Field field;
	
	// generate PrimaryKeyField by passing fields with @Id
	public PrimaryKeyField(Field field) {
		
		if (field.getAnnotation(Id.class) == null) {
			throw new IllegalStateException("Cannot create PrimaryKeyField object! Provided field, " +
					  						getName() + "is not annotated with @Id");
		}
		this.field = field;
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
		return !field.getAnnotation(Id.class).name().isBlank() ? field.getAnnotation(Id.class).name() : field.getName().replaceAll("([a-z])([A-Z]+)","$1_$2").toLowerCase();
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
		PrimaryKeyField other = (PrimaryKeyField) obj;
		return Objects.equals(field, other.field);
	}
	
	
}
