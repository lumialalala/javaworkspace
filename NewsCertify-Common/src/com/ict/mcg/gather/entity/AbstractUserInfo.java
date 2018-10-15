package com.ict.mcg.gather.entity;

import java.io.Serializable;

public class AbstractUserInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 9278062266678511L;
	public String name;
	public String id;
	public String gender;
	public String location;
	public String approve;
	public String description;
	public AbstractUserInfo() {
		name = "";
		id = "";
		gender = "";
		location = "";
		approve = "";
		description = "";
	}
	
	public void printInfo () {
		System.out.println(name+" "+id+" "+gender+" "+location+" "+approve);
	}
}
