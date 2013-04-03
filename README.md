Overview
===========

There are different web applications deployed in the same web container and if one application wants to access some POJOs  (Plain Old Java Object) present inside other web application, in order to fulfill its needs. (Say there are two web applications 'Foo' and 'Bar' deployed in the same web container. Application 'Foo' depends on 'Bar' to access a method say 'barMethod()' of 'BarService' of a POJO class present  inside the 'Bar' application),  then
The 'Bar' application servlet must provide a method to access its POJO classes
The 'Foo' application either has to go with 'RequestDispatcher's' 'forward' or 'Response's' redirect' methods pointing to the 'Bar' application servlet.
	Though both the applications reside inside the same container and 'Foo' application knows what method of the POJO class of 'Bar' application it has to access, it has to go through the network using HTTP protocol as if 'Foo' application request is considered as a browser client request to 'Bar' application. 
Is there an optimal way to avoid the HTTP protocol overhead comes when we use 'forward' / 'redirect'?