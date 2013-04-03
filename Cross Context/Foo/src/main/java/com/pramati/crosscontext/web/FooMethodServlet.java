package com.pramati.crosscontext.web;

import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.web.servlet.DispatcherServlet;

import com.pramati.crosscontext.model.Person;
import com.pramati.crosscontext.util.Utils;


public class FooMethodServlet extends DispatcherServlet {
	
	
	private static final long serialVersionUID = -1810934140633095625L;
	
	private ObjectMapper jacksonMapper = new ObjectMapper();
	
	@Override
	protected void doService(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String mode = request.getParameter(WEBConstants.PARAM_MODE);
		String methodName = request.getParameter(WEBConstants.PARAM_METHOD);
		
		if (methodName == null || methodName.trim().equals("")) {
			response.getWriter().println("Error: Kindly provide the method name");
			return;
		}
		
		if (WEBConstants.MODE_CROSS_CONTEXT.equalsIgnoreCase(mode)){
			
			ServletContext srcServletContext = request.getSession().getServletContext();
			ServletContext targetServletContext = srcServletContext.getContext("/Bar");
//			save the class loader which loaded the 'Foo' application in a variable 
			ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
			try{
				
				Object object = targetServletContext.getAttribute("org.springframework.web.servlet.FrameworkServlet.CONTEXT.bar");
//				get the class loader which loaded the 'Bar' application
				ClassLoader targetServiceClassLoader = object.getClass().getClassLoader();
//				and set it as the current context class loader.
				Thread.currentThread().setContextClassLoader(targetServiceClassLoader);
				
				Class<?> classBarService = (Class<?>) 
							targetServiceClassLoader.loadClass("com.pramati.crosscontext.service.BarService");
				
				Method getBeanMethod = object.getClass().getMethod("getBean", String.class);
//				Get the barService defined in the 'Bar' application context.
				Object barService = getBeanMethod.invoke(object, "barService");
				
//				Get the method of the 'barService'
				Method targetMethod = classBarService.getMethod(methodName, (Class[]) null);
				if (targetMethod == null){
					response.getWriter().println("Error: The method['" + methodName + "' does not exist" );
					return;
				}
//				Invoke the method on 'barService'
				Object responseFromBarMethod = targetMethod.invoke(barService, (Object[]) null);
				response.getWriter().println(responseFromBarMethod);
			} finally {
				Thread.currentThread().setContextClassLoader(currentClassLoader);
			}
		} else if (WEBConstants.MODE_FORWARD.equalsIgnoreCase(mode)) {
			
			ServletContext servContext = getServletContext().getContext("/Bar");
			RequestDispatcher rd = servContext.getRequestDispatcher("/Bar?" + WEBConstants.PARAM_METHOD + "=" + methodName);
			if (rd != null){
				rd.forward(request, response);
			}
		}  else if (WEBConstants.MODE_REDIRECT.equalsIgnoreCase(mode)) {
			
			response.sendRedirect("../Bar?" + WEBConstants.PARAM_METHOD + "=" + methodName);
		} 
		else if (WEBConstants.MODE_URL.equalsIgnoreCase(mode)){
			
			String targetURL = "http://" + request.getServerName() + ":" + request.getServerPort() + "/Bar?" + WEBConstants.PARAM_METHOD + "=" + methodName;
			HttpURLConnection connection = null;
			try {
				URL url = new URL(targetURL);
				connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");
				connection.setDoInput(true);
				connection.setDoOutput(true);
				response.getWriter().println(Utils.getResponseAsString(connection));
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (connection != null) {
					connection.disconnect();
				}
			}
		} else if (WEBConstants.MODE_CROSS_CONTEXT_WITH_PARAM.equalsIgnoreCase(mode)){
			
			ServletContext srcServletContext = request.getSession().getServletContext();
			ServletContext targetServletContext = srcServletContext.getContext("/Bar");
			ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
			try{
				
				Object object = targetServletContext.getAttribute("org.springframework.web.servlet.FrameworkServlet.CONTEXT.bar");
				ClassLoader targetServiceClassLoader = object.getClass().getClassLoader();
				Thread.currentThread().setContextClassLoader(targetServiceClassLoader);
				
				Class<?> classBarService = (Class<?>) 
							targetServiceClassLoader.loadClass("com.pramati.crosscontext.service.BarService");
				Method getBeanMethod = object.getClass().getMethod("getBean", String.class);
				Object barService = getBeanMethod.invoke(object, "barService");
				
				Method[] methods  = classBarService.getMethods();
				Method targetMethod = null;
				for (int i = 0; i < methods.length; i ++){
					if (methods[i].getName().equals("barMethodWithParam")){
						targetMethod = methods[i];
						break;
					}
				}
				if (targetMethod == null){
					response.getWriter().println("Error: The method['" + methodName + "' does not exist" );
					return;
				}
				
				Person person = new Person(1, "Kayal");
//				serialize the param 'Person' with the 'Foo' application jacksonMapper 
				String serializedPerson = jacksonMapper.writeValueAsString(person);

				Class<?> classPerson = (Class<?>) 
					targetServiceClassLoader.loadClass("com.pramati.crosscontext.model.Person");
				
//				Get the jacksonMapper defined in the 'Bar' application context.
				Object targetJacksonMapper = getBeanMethod.invoke(object, "jacksonMapper");
				
//				Get the 'readValue' method of the 'Bar' application's jacksonMapper
				Method readValueMethod = targetJacksonMapper.getClass().getMethod("readValue", String.class, Class.class);
				
//				Deserialize the Person using 'readValue' method of the 'Bar' application's jacksonMapper
				Object deserializedPerson = readValueMethod.invoke(targetJacksonMapper, serializedPerson, classPerson);
				
//				invoke the 'barMethodWithParam' method of 'BarService' along with the deserialized Person as param
				Object responseFromBarMethod = targetMethod.invoke(barService, new Object[]{ deserializedPerson});
			
//				Get the 'writeValueAsString' method of the 'Bar' application's jacksonMapper
				Method writeValueAsStringMethod = targetJacksonMapper.getClass().getMethod("writeValueAsString", Object.class);
				
//				serialize the response by invoking 'writeValueAsString' method of the 'Bar' application's jacksonMapper
				String responseString = (String) writeValueAsStringMethod.invoke(targetJacksonMapper, responseFromBarMethod);
				
				response.getWriter().println(responseString);
			} finally {
				Thread.currentThread().setContextClassLoader(currentClassLoader);
			}
		}
		
	}
	
}
