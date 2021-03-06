package com.helicaltech.pcni.model;


import java.io.Serializable;
import java.util.List;

/**
 * This is persistent class for role which maps it's properties with role table
 * and persist them to a database, this class's object or instance in stored in
 * role table as per rule this class should have the default constructor, as
 * well as getter and setter method's for it's properties Annotation Entity mark
 * this class as Entity Bean and annotation Table allows you to specify the
 * details of the table that will be used to persist the entity in the database.
 *
 * @author Muqtar Ahmed
 * @version 1.1
 * @since 1.0
 */

public class Role implements Serializable {

	/**
	 * this is static and final filed of serial version id automatically
	 * generated by eclipse
	 */

	private static final long serialVersionUID = 1L;

	/**
	 * each entity bean have the primary key and annotate with Id generatedvalue
	 * generate the automaticaly determined the most appropriate primary key
	 * with strategy
	 */

	private int id;

	/**
	 * details of the column role name to which field's or properties is mapped
	 */

	private String role_name;

	/**
	 * details of the column organization id to which field's or properties is
	 * mapped
	 */

	private Integer org_id;

	/**
	 * ManyToMany mapping between role and user table. user object
	 */

	private List<User> users;

	/**
	 * default constructor
	 */

	public Role() {
	}

	/**
	 * over loaded constructor with two arguments role name and organization id
	 *
	 * @param roleName
	 *            role name
	 * @param org_id
	 *            organization id
	 */

	public Role(String roleName, Integer org_id) {
		this.role_name = roleName;
		this.org_id = org_id;
	}

	/**
	 * over loaded constructor with three arguments
	 *
	 * @param id
	 *            role Id
	 * @param roleName
	 *            role name
	 * @param users
	 *            list of users
	 */

	public Role(int id, String roleName, List<User> users) {
		super();
		this.id = id;
		this.role_name = roleName;
		this.users = users;
	}

	/**
	 * getter method for id primary key for role table
	 *
	 * @return generated id
	 */

	public int getId() {
		return id;
	}

	/**
	 * setter method for id primary key for role table
	 *
	 * @param id
	 *            generated id
	 */

	public void setId(int id) {
		this.id = id;
	}

	/**
	 * getter method for role name
	 *
	 * @return role name
	 */

	public String getRole_name() {
		return role_name;
	}

	/**
	 * setter method for role name
	 *
	 * @param role_name
	 *            role name
	 */

	public void setRole_name(String role_name) {
		this.role_name = role_name;
	}

	/**
	 * getter method for list of user objects
	 *
	 * @return list of user objects
	 */

	public List<User> getUsers() {
		return users;
	}

	/**
	 * setter method for list of user objects
	 *
	 * @param users
	 *            user object
	 */

	public void setUsers(List<User> users) {
		this.users = users;
	}

	/**
	 * getter method for organization id
	 *
	 * @return organization id
	 */

	public Integer getOrg_id() {
		return org_id;
	}

	/**
	 * setter method for organization id
	 *
	 * @param org_id
	 *            organization id
	 */

	public void setOrg_id(Integer org_id) {
		this.org_id = org_id;
	}
}
