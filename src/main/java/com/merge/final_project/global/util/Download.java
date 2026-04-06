package com.merge.final_project.global.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class Download {

	@Value("${file.upload.path}")
	private String uploadPath;

	@GetMapping("/download")
	public void download(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		response.setContentType("text/html;charset=utf-8");
		String file_repo = uploadPath; // 배포시 외부 경로 설정
		String filename_org = request.getParameter("filename_org");
		String filename_real = request.getParameter("filename_real");
		// 한글처리
		filename_org = new String(filename_org.getBytes("UTF-8"),"ISO-8859-1");
		OutputStream out = response.getOutputStream();
		String downFile = file_repo+"/"+filename_real; 
		File f = new File(downFile);
		
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Content-disposition", "attachment; fileName="+filename_org);
		
		FileInputStream in = new FileInputStream(f);
		byte[] buffer = new byte[1024*8];
		while(true) {
			int count = in.read(buffer);
			if (count == -1) {
				break;
			}
			out.write(buffer, 0, count);
		}
		in.close();
		out.close();
	}
}
