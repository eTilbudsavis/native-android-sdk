package com.eTilbudsavis.etasdk.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {
	
	public <T extends IValidator> Validator(Class<T> validator) {
		
	}

	interface IValidator {
		public boolean isValid();
	}
	
	public class EmailValidator implements IValidator {

		String regex = "^[^@]+@[^@]+$";
		String email;
		Pattern pattern;
		
		public EmailValidator() {
			pattern = Pattern.compile(regex);
		}
		
		public void setEmail(String email) {
			this.email = email;
		}
		
		public boolean isValid() {
			Matcher matcher = pattern.matcher(email);
			return matcher.matches();
		}
		
	}
	
}
