package com.xck.jdk.file.config;

import org.springframework.core.annotation.Order;

public class Report {

	@Order(1)
	private String user_id = "";

	@Order(2)
	private String msg_id = "";

	@Order(3)
	private String content = "";

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id == null ? "" : user_id;
	}

	public String getMsg_id() {
		return msg_id;
	}

	public void setMsg_id(String msg_id) {
		this.msg_id = msg_id == null ? "" : msg_id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content == null ? "" : content;
	}
}
