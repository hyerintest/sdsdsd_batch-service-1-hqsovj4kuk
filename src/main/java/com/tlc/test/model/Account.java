package com.tlc.test.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Account {

	private String id;
	private String content;
	private String post;

	public Account postDel(){
		post = "Y";
		return this;
	}
}
