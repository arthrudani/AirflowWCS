package com.daifukuamerica.wrxj.web.validators;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.daifukuamerica.wrxj.web.model.json.wrx.LoadDataModel;


/**
 * 
 * Author: dystout
 * Created : May 2, 2017
 * 
 * Field validation. Uses services to perform validation on model 
 * to check for errors before continuation of controller methods. 
 * Should 
 * 
 * TODO- Implement with JSR 303 validator (hibernate validator maybe?) 
 *
 */
public class LoadValidator implements Validator
{

	@Override
	public boolean supports(Class<?> arg0)
	{
		return LoadDataModel.class.equals(arg0);
	}

	@Override
	public void validate(Object target, Errors errors)
	{
		//TODO impl validation
	}

}
