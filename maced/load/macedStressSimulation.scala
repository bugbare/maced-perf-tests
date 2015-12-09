package maced.load

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class MacedStressSimulation extends Simulation {

	val httpProtocol = http
		.baseURL("https://registration-uat.ws.macmillaneducation.com")
		.acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
		.acceptLanguageHeader("en-US,en;q=0.5")
    	.acceptEncodingHeader("gzip, deflate")
    	.userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")
		.inferHtmlResources(BlackList(""".*\.css""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""", """.*\.(t|o)tf""", """.*\.png"""), WhiteList())

	object Home {

	    val home = exec(http("Go To Home Page")
	        .get(uri02 + "/")
	        .check(status.is(200)))
	        .pause(1)
  	}

  	object ClickLogin {

	  	val clickLogin = exec(http("Click On Login Button")
				.post("/index-xml.php?site_id=RDCv2")
				.headers(headers_plain_text)
				.formParam("action", "get_standalone_login_html")
				.formParam("SID", "qe82igsgtngvjpqejfafl8sou1")
				.formParam("callback", "MEC_webservices.WS_launch_fancybox")
				.check(status.is(200)))
				.pause(1)
  	}

  	object SubmitLoginDetails {

  		val submitLoginDetails = group("Submit Valid Login Details") {

	  		exec(http("postLoginDetails")
				.post("/index-xml.php?site_id=RDCv2")
				.headers(headers_plain_text)
				.formParam("username", "rshah")
				.formParam("password", "PerfTest01")
				.formParam("action", "loginpost")
				.formParam("SID", "qe82igsgtngvjpqejfafl8sou1")
				.formParam("second_callback", "is_logged_in_hook")
				.formParam("callback", "MEC_webservices.WS_callback_function_CORS256063")
				//.resources(http("getResourceList1")
				//.get("https://registration-uat.ws.macmillaneducation.com/index-xml.php?site_id=RDCv2&callback=jQuery1831027647802676074207_1449224920412&action=get_sgk_cookie&SID=no8en2tvg8tq9iv4lpe06jh5d1&_=1449225680782")
				//.check(headerRegex("Set-Cookie","sgk_cookie=(.*)").saveAs("USERPASS"))
				//.silent
				//.headers(headers_cache_control),
	            //http("getResourceList2")
				//.get("https://registration-uat.ws.macmillaneducation.com/index-xml.php?site_id=RDCv2&callback=jQuery1831027647802676074207_1449224920413&action=get_MEE_OAUTH_cookie&SID=no8en2tvg8tq9iv4lpe06jh5d1&_=1449225680784")
				//.check(headerRegex("Set-Cookie","MEC_WS_SESSIONID=(.*)").saveAs("MEC_WS_SESSIONID"))
				//.silent
				//.headers(headers_cache_control),
	            //http("getResourceList3")
				//.get("https://registration-uat.ws.macmillaneducation.com/index-xml.php?site_id=RDCv2&callback=jQuery1831027647802676074207_1449224920412&action=get_MEE_OAUTH_cookie&SID=no8en2tvg8tq9iv4lpe06jh5d1&_=1449225680991")
				//.check(headerRegex("Set-Cookie","MEE_OAUTH=(.*)").saveAs("MEE_OAUTH"))
				//.silent
				//.headers(headers_cache_control),
			)
			.exec(http("getResourcePage")
				.get(uri02 + "/resources/")
				.headers(headers_html_xml_webp)
				.check(regex("""LS1""")))
		}

  	}

  	object GoResourcePage {

  		val goResourcePage = group("Go To The ResourcePage") {


		  		exec(http("postRDC")
					.post("/index-xml.php?site_id=RDCv2")
					.headers(headers_plain_text)
					.formParam("action", "is_valid_session_id")
					.formParam("SID", "qe82igsgtngvjpqejfafl8sou1")
					.formParam("callback", "MEC_webservices.WS_callback_function_CORS135987")
					.resources(http("postResourceCheck1")
					.post("/index-xml.php?site_id=RDCv2")
					.silent
					.headers(headers_plain_text)
					.formParam("action", "isloggedin")
					.formParam("SID", "qe82igsgtngvjpqejfafl8sou1")
					.formParam("second_callback", "WS_INIT")
					.formParam("callback", "MEC_webservices.WS_callback_function_CORS74532"),
		            http("postResourceCheck2")
					.post("/index-xml.php?site_id=RDCv2")
					.silent
					.headers(headers_plain_text)
					.formParam("action", "isloggedin")
					.formParam("SID", "qe82igsgtngvjpqejfafl8sou1")
					.formParam("second_callback", "is_logged_in_hook")
					.formParam("callback", "MEC_webservices.WS_callback_function_CORS703806"),
		            http("checkResources1")
					.options(uri05 + "/ResourceLinks/?_=1448553267473")
					.silent
					.headers(headers_authorization),
		            http("checkResources2")
					.options(uri05 + "/ResourcePacks/?_=1448553267474")
					.silent
					.headers(headers_authorization)))
					/*http("getResources1")
					.get(uri05 + "/ResourceLinks/?_=1448553267473")
					.headers(headers_getResources),
		            http("getResources2")
					.get(uri05 + "/ResourcePacks/?_=1448553267474")
					.headers(headers_getResources)))*/
	            .exec(http("getResourcePage")
					.get(uri02 + "/resources/")
					.headers(headers_html_xml_webp)
					.check(status.is(200))
					.check(regex("""Teachers""")))
					
				
			}
  	}

// Set up header map objects, to inject into each scenario executable
	val headers_plain_text = Map(
		"Accept" -> "text/plain, */*; q=0.01",
		"Accept-Encoding" -> "gzip, deflate",
		"Cache-Control" -> "no-cache",
		"Origin" -> "https://skillful-uat.macmillan.education",
		"Pragma" -> "no-cache")

	val headers_cache_control = Map(
		"Cache-Control" -> "no-cache",
		"Pragma" -> "no-cache")

	val headers_html_xml_webp = Map(
		"Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
		"Cache-Control" -> "no-cache",
		"Pragma" -> "no-cache",
		"Upgrade-Insecure-Requests" -> "1")

	val headers_authorization = Map(
		"Access-Control-Request-Headers" -> "accept, authorization",
		"Access-Control-Request-Method" -> "GET",
		"Cache-Control" -> "no-cache",
		"Origin" -> "https://skillful-uat.macmillan.education",
		"Pragma" -> "no-cache")

	val headers_getResources = Map(

		"Accept" -> "text/html,text/javascript, application/xhtml+xml,application/xml,application/json, */*; q=0.01;q=0.9,image/webp,*/*;q=0.8",
		"Accept-Encoding" -> "gzip, deflate, sdch",
		"Accept-Language" -> "en-US,en;q=0.8",
		"Cache-Control" -> "no-cache",
		"Connection" -> "keep-alive",
		"Host" -> "skillful-uat.macmillan.education",
		"Pragma" -> "no-cache",
		"Referer" -> "https://skillful-uat.macmillan.education/",
		"Upgrade-Insecure-Requests" -> "1",
		"User-Agent" -> "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36")
		//"Accept" -> "application/json, text/javascript, */*; q=0.01",
		//"Cache-Control" -> "no-cache",
		//"Origin" -> "https://skillful-uat.macmillan.education",
		//"Pragma" -> "no-cache")
// Set up URL paths for all required resources  
    val uri02 = "https://skillful-uat.macmillan.education"
    val uri05 = "https://mee-uat-dataservices-subscription.ws.macmillaneducation.com/Svc/Subscription"
    val uri08 = "https://registration-uat.ws.macmillaneducation.com/index-xml.php"
	val scn = scenario("Login And View Resources")
		.exec(Home.home)
		.exec(ClickLogin.clickLogin)
		.exec(SubmitLoginDetails.submitLoginDetails)
		.exec(GoResourcePage.goResourcePage)
	setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
	//setUp(scn.inject(splitUsers(10) into (rampUsers(2) over (1 seconds)) separatedBy(30 seconds))).protocols(httpProtocol)
}