/**
 * 
 */
package com.chronosystems.log.model;

/**
 * @author andre.silva
 *
 */
public class Environment {
	private String path;
	private String server;
	private boolean all;

	public Environment() {
		super();
	}
	public Environment(String server) {
		super();
		this.server = server;
	}
	
	public Environment(String path, String server) {
		super();
		this.path = path;
		this.server = server;
	}
	
	public Environment(String path, boolean all) {
		super();
		this.path = path;
		this.all = all;
	}

	public boolean isValid() {
		return server != null  && path != null && !all;
	}

	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}

	public String getServer() {
		return server;
	}
	public void setServer(String server) {
		this.server = server;
	}

	public boolean isAll() {
		return all;
	}
	
	@Override
	public String toString() {
		if (all) {
			return path + " > ALL SERVERS";
		}

		if (isValid()) {
			return path + " > " + server;
		}
		return "  --------------------------------------------------------------  ";//line separator
	}
}
