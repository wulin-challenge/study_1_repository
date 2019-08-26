package org.wulin.google.guava;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.HashBasedTable;

public class HashBasedTableTest {
	
	@Test
	public void tableTest() {
		HashBasedTable<String, String, Object> table = HashBasedTable.create();
		
		table.put("rowKey1", "columnKey1", 1);
		table.put("rowKey1", "columnKey2", 2);
		table.put("rowKey2", "columnKey1", 5);
		table.put("rowKey2", "columnKey2", 6);
		
		Map<String, Object> column = table.column("columnKey1");
		Map<String, Object> row = table.row("rowKey2");
		Map<String, Object> column2 = table.column("rowKey2");
		System.out.println(column);
		
	}

}
