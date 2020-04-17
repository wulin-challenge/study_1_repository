package org.wulin.jvm.jdk.unsafe;

public class Data {

	/**
	 * 此处的类型一定要是基本类型,若是 Long ,是不行的
	 */
	private long id;
	
	private String name;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
