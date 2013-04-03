package com.pramati.crosscontext.service;

import com.pramati.crosscontext.model.Person;

public class BarService {

	public String barMethod(){
		
		return "BarService.barMethod(): Hi, I am invoked";
	}
	
	public Person barMethodWithParam(Person person){
		
		System.out.println("BarService.barMethodWithParam(Person): Hi, I am invoked" + person);
		return person;		
	}
}
