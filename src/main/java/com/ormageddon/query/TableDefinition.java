package com.ormageddon.query;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Scanner;

import com.ormageddon.annotations.Column;
import com.ormageddon.annotations.JoinColumn;
import com.ormageddon.metamodel.ColumnField;
import com.ormageddon.metamodel.ForeignKeyField;
import com.ormageddon.metamodel.MetaModel;
import com.ormageddon.metamodel.PrimaryKeyField;

public class TableDefinition {
	
	private static HashMap<String, String> dataTypes = new HashMap<String,String>();
	
	static {
		
		Scanner scan;
		try {
			scan = new Scanner(new File("src\\main\\resources\\dataTypes.csv"));
			while (scan.hasNextLine()) {
				String[] dataMapping = scan.nextLine().split(",");
				dataTypes.put(dataMapping[0],dataMapping[1]);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * This method generates the SQL string for creating a table from an annotated class.
	 * @param metaModel (which is generated from an annotated class)
	 * @return DDL String for creating a table.
	 */
	public String createTableIfNotExists(MetaModel<Class<?>> metaModel) {
		
		String newTableQuery = "";
		if (metaModel != null) {
			// CREATE TABLE IF NOT EXISTS table (
			newTableQuery += "CREATE TABLE IF NOT EXISTS " + metaModel.getTableName() + " (\r\n";
			// id SERIAL PRIMARY KEY,
			PrimaryKeyField pk = metaModel.getPrimaryKey();
			newTableQuery += pk.getColumnName() + " SERIAL PRIMARY KEY";
			
			for (ColumnField column : metaModel.getColumns()) {
				newTableQuery += ",\r\n";
				// column DataTypeE NOT NULL UNIQUE,
				newTableQuery += column.getColumnName() + " " + dataType(column.getField());
				if (column.getField().getAnnotation(Column.class) != null && column.getField().getAnnotation(Column.class).notNull()) {
					newTableQuery += " NOT NULL";
				}
				if (column.getField().getAnnotation(Column.class) != null && column.getField().getAnnotation(Column.class).unique()) {
					newTableQuery += " UNIQUE";
				}
			}
			if (metaModel.getForeignKeys() != null && !metaModel.getForeignKeys().isEmpty()) {

				for (ForeignKeyField fk : metaModel.getForeignKeys()) {
					newTableQuery += ",\r\n";
					// fk_column DataType REFERENCES 
					newTableQuery += fk.getColumnName() + " INTEGER REFERENCES ";
					// tableName, columnName
					newTableQuery += fk.getReference().getTableName() + "(" + fk.getReference().getPrimaryKey().getColumnName() +")";
				}
			}
		
		newTableQuery += "\r\n);";
		
		}
		return newTableQuery;
	}

	/**
	 * Helper method for grabbing the dataType of the field
	 * @param field
	 * @return Data type
	 */
	public String dataType(Field field) {
		return dataTypes.get(field.getType().getSimpleName().toLowerCase());
	}
	
	public HashMap<String, String> getDataTypes() {
		return dataTypes;
	}
	
}
