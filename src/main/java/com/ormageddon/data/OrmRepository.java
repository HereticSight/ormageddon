package com.ormageddon.data;

import java.util.List;
import java.util.Optional;

public interface OrmRepository<T> {
	
	Optional<List<Object>> findAll(Class<?> clazz);
	
	Optional<List<Object>> findBy(Class<?> clazz, String columns, String conditions);
	
//	Optional<List<Object>> findBy(Class<?> clazz, String columns, String conditions, String operators);

	int save(T model);
	
	boolean remove(T model);
	
	boolean update(T model, String updateColumns);

	
}
