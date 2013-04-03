package com.pramati.crosscontext.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeansException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.pramati.crosscontext.service.BarService;


public class BarServlet extends DispatcherServlet {

	private final String PARAM_RESOURCE = "resource";
	private final String PARAM_METHOD = "method";
	
	BarService barService = null;
	
	private static final long serialVersionUID = -1810934140633095625L;

	@Override
    protected WebApplicationContext initWebApplicationContext() throws BeansException {
		
		WebApplicationContext webAppContext = super.initWebApplicationContext();
		barService = (BarService) webAppContext.getBean("barService");
		return webAppContext;
	}
	
	@Override
	public void doService(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
		
		String fileName = request.getParameter(PARAM_RESOURCE);
		String methodName = request.getParameter(PARAM_METHOD);
		
		if (fileName != null){
			InputStream istream = getServletContext().getResourceAsStream("/WEB-INF/classes/" + fileName);
			if (istream != null){
				StringWriter writer = new StringWriter();
				IOUtils.copy(istream, writer, "utf-8");
				response.getWriter().println("The content recived from " + fileName);
				response.getWriter().println(writer.toString());
			}
		} else if (methodName != null){
			
			Method[] methods = barService.getClass().getMethods();
			Method targetMethod = null;
			for (int i = 0; i < methods.length; i++) {
				if (methods[i].getName().equals(methodName)){
					targetMethod = methods[i];
					break;
				}
			}
			if (targetMethod == null){
				response.getWriter().println("Error: The method['" + methodName + "' does not exist" );
				return;
			} 
			try {
				Object responseFromBarMethod = targetMethod.invoke(barService, (Object[]) null);
				response.getWriter().println(responseFromBarMethod);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
	}
}
