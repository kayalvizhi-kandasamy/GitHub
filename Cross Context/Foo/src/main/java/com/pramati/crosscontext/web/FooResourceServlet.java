package com.pramati.crosscontext.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.pramati.crosscontext.util.Utils;


public class FooResourceServlet  extends HttpServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1810934140633095625L;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
		
		String mode = req.getParameter(WEBConstants.PARAM_MODE);
		
		if (WEBConstants.MODE_CROSS_CONTEXT.equals(mode)){
			ServletContext barServletContext = getServletContext().getContext("/Bar");
			InputStream istream = barServletContext.getResourceAsStream("/WEB-INF/classes/barResource.txt");
			if (istream != null){
				StringWriter writer = new StringWriter();
				IOUtils.copy(istream, writer, "utf-8");
				resp.getWriter().println("The content recived from barResource.txt:");
				resp.getWriter().println(writer.toString());
			}
		} else if (WEBConstants.MODE_URL.equals(mode)) {
			
			HttpURLConnection connection = null;
			try {
				// Create connection
				URL url = new URL("http://localhost:8080/Bar?resource=barResource.txt");
				connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");
				connection.setDoInput(true);
				connection.setDoOutput(true);
				resp.getWriter().println(Utils.getResponseAsString(connection));
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (connection != null) {
					connection.disconnect();
				}
			}
		}
		
	}
}
