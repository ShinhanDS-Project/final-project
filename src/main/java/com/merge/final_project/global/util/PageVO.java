package com.merge.final_project.global.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageVO {
	private static final int DEFAULT_SIZE=10;
	private static final int DEFAULT_MAX_SIZE=50;
	
	private int page;
	private int size;
	private String searchWord;
	private String searchType;
	
	public PageVO() {
		this.page = 1;
		this.size = DEFAULT_SIZE;
	}
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page < 0 ? 1 : page;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size < DEFAULT_SIZE || size > DEFAULT_MAX_SIZE ? DEFAULT_SIZE : size;
	}
	
	public Pageable makePageable() {
		// 정렬 여러개인 경우
		 return PageRequest.of(this.page-1, this.size, Sort.by(Sort.Direction.DESC, "gno").and(Sort.by(Sort.Direction.ASC, "ono")));
//		return PageRequest.of(this.page-1, this.size, dir, props);
	}
	
	public Pageable makePageable(int direction ,String...props) {
		Sort.Direction dir = direction == 0 ? Sort.Direction.DESC : Sort.Direction.ASC;
		// 정렬 여러개인 경우
//		 return PageRequest.of(this.page-1, this.size, Sort.by(Sort.Direction.DESC, "gno").and(Sort.by(Sort.Direction.ASC, "ono")));
		return PageRequest.of(this.page-1, this.size, dir, props);
	}
}
