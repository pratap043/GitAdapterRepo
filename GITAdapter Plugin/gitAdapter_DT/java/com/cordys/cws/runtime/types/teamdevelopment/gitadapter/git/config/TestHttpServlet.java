package com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.config;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class TestHttpServlet extends HttpServlet
{
	public static final HttpServlet	DUMMY_SERVLET	= new TestHttpServlet();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setStatus(SC_OK);
		resp.setContentType("text/html");
		resp.setCharacterEncoding(UTF_8.name());
		try (PrintWriter w = resp.getWriter()) {
			writeBody(req, w);
		}
	}

	@SuppressWarnings("unused")
	protected void writeBody(HttpServletRequest req, PrintWriter w) {
		
	}
}
