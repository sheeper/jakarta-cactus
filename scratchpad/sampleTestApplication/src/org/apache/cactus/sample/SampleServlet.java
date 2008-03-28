package org.apache.cactus.sample;

import javax.servlet.http.HttpServlet;

public class SampleServlet extends HttpServlet {
	
	public String doGet () {
		return "Hello World";
	}
}