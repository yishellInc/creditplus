package com.creditplus.p2p.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Roles entity. @author MyEclipse Persistence Tools
 */

public class RoleVO implements java.io.Serializable {

	// Fields
	private Integer id;
	private Integer enable;
	private String name;
	private Set roles = new HashSet(0);
	private Set resources = new HashSet(0);
	
	/** default constructor */
	public RoleVO() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getEnable() {
		return this.enable;
	}

	public void setEnable(Integer enable) {
		this.enable = enable;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}



	public Set getRoles() {
		return roles;
	}



	public void setRoles(Set roles) {
		this.roles = roles;
	}



	public Set getResources() {
		return resources;
	}



	public void setResources(Set resources) {
		this.resources = resources;
	}


}