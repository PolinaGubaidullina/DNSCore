package pages;

import geb.Page;

/**
 * 
 * @author jens Peters
 * Tests available Login Page
 *
 */
public class LoginPage extends Page {
	
	static url = "/daweb3/login/auth"
			
	static at = { title ==~ /Anmeldung|Login/ }
	
	static content = {
		loginForm { $("form")}
		loginButton { $("input", id: "submit") }
	}

//	static content = {
//		loginButton(to: WelcomePage) { $("input", id: "submit") }
//		username { $("input", name: "username") }
//		password { $("input", name: "password") }
//	}

}
