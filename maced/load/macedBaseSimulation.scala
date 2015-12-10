package maced.load

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class macedBaseSimulation extends Simulation {

	val httpProtocol = http
		.baseURL("https://registration-uat.ws.macmillaneducation.com")
		.acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
		.acceptLanguageHeader("en-US,en;q=0.5")
    	.acceptEncodingHeader("gzip, deflate")
    	.userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:42.0) Gecko/20100101 Firefox/42.0")
		.inferHtmlResources(BlackList(""".*\.js""", """.*\.css""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""", """.*\.(t|o)tf""", """.*\.png"""), WhiteList())

	//val headers_0 = Map("Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")

	val headers_1 = Map("Accept" -> "*/*")

	val headers_2 = Map("Accept" -> "text/css,*/*;q=0.1")

	val headers_3 = Map(
		"Accept" -> "application/font-woff2;q=1.0,application/font-woff;q=0.9,*/*;q=0.8",
		"Accept-Encoding" -> "identity")

	val headers_5 = Map(
		"Cache-Control" -> "no-cache",
		"Origin" -> "http://skillful-uat.macmillan.education",
		"Pragma" -> "no-cache")

	val headers_17 = Map(
		//"Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
		"Access-Control-Request-Headers" -> "authorization",
		"Access-Control-Request-Method" -> "GET",
		"Origin" -> "http://skillful-uat.macmillan.education")

	val headers_19 = Map(
		"Accept" -> "application/json, text/javascript, */*; q=0.01",
		"Origin" -> "http://skillful-uat.macmillan.education",
		"Authorization" -> "Bearer ${MEE_OAUTH}")

	val headers_cookie = Map(
		//"User-Agent" -> "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:42.0) Gecko/20100101 Firefox/42.0",
		//"Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
		//"Accept-Language" -> "en-GB,en;q=0.5",
		//"Accept-Encoding" -> "gzip, deflate",
		"Referer" -> "http://skillful-uat.macmillan.education/",
		"Cookie" -> "MEC_WS_SESSIONID=${MEC_WS_SESSIONID}; USERPASS=${USERPASS}; MEE_OAUTH=${MEE_OAUTH}"
		)

    val uri1 = "http://skillful-uat.macmillan.education"
    val uri2 = "https://mee-uat-dataservices-subscription.ws.macmillaneducation.com/Svc/Subscription"
    val uri3 = "http://rdc-uat.macmillan.education"
    val uri4 = "https://registration-uat.ws.macmillaneducation.com/index-xml.php"
    //val uri5 = "https://use.typekit.net/c/0f7b18/1w;museo-sans,7cdcb44be4a7db8877ffa5c0007b8dd865b3bbc383831fe2ea177f62257a9191,Py8:W:n1,Py6:W:n3,PyC:W:n5,PyF:W:n7/l"

	val scn = scenario("macedBaseSimulation")
		.exec(http("request_home")
			.get(uri1 + "/"))
		.pause(1)
		.exec(http("request_getAWSCookies")
			.get(uri3 + "/?dm=27bac6181890453140bce750f0370dbc&action=load&blogid=28&siteid=1&back=http%3A%2F%2Fskillful-uat.macmillan.education%2F")
			.silent
			.headers(headers_1)
			)
		.pause(1)
		.exec(http("request_getSessionCookie")
			.get("/index-xml.php?site_id=RDCv2&action=get_session_id_html&parenturl=http%3A%2F%2Fskillful-uat.macmillan.education%2F")
			//.headers(headers_0)
			.check(regex("""WS_sessionID=(.*)"""").saveAs("MEC_WS_SESSIONID"))
			.resources
			(
				http("request_checkLoginStatus")
				.post(uri4 + "?site_id=RDCv2")
				.headers(headers_5)
				.formParam("action", "isloggedin")
				.formParam("SID", "${MEC_WS_SESSIONID}")
				.formParam("second_callback", "WS_INIT"),
	            http("request_checkLoginStatus")
				.post(uri4 + "?site_id=RDCv2")
				.headers(headers_5)
				.formParam("action", "isloggedin")
				.formParam("SID", "${MEC_WS_SESSIONID}")
				.formParam("second_callback", "is_logged_in_hook")
			)
		)
		.pause(1)
		.exec(http("request_getLoginForm")
			.post("/index-xml.php?site_id=RDCv2")
			.headers(headers_5)
			.formParam("action", "get_standalone_login_html")
			.formParam("SID", "${MEC_WS_SESSIONID}")
			.formParam("callback", "MEC_webservices.WS_launch_fancybox"))
		.pause(5)
		.exec(http("request_submitLoginDetails")
			.post("/index-xml.php?site_id=RDCv2")
			.headers(headers_5)
			.formParam("username", "rshah")
			.formParam("password", "PerfTest01")
			.formParam("action", "loginpost")
			.formParam("SID", "${MEC_WS_SESSIONID}")
			.formParam("second_callback", "is_logged_in_hook")
			.resources
			(	
				http("request_getSgkCookie")
				.get(uri4 + "?site_id=RDCv2&action=get_sgk_cookie&SID=${MEC_WS_SESSIONID}")
				.headers(headers_1)
				.check(regex(""""SGK_USERPASS":"(.*)",""").saveAs("USERPASS")),
	            http("request_getMeeOAuthCookie")
				.get(uri4 + "?site_id=RDCv2&action=get_MEE_OAUTH_cookie&SID=${MEC_WS_SESSIONID}")
				.headers(headers_1)
				.check(regex(""""MEE_OAUTH_token":"(.*)",""").saveAs("MEE_OAUTH"))
			)
		)
		.exec(addCookie(Cookie("MEC_WS_SESSIONID", "${MEC_WS_SESSIONID}")
			.withDomain("skillful-uat.macmillan.education")))
		.exec(addCookie(Cookie("MEE_OAUTH", "${MEE_OAUTH}")
			.withDomain("skillful-uat.macmillan.education")))
		.exec(addCookie(Cookie("USERPASS", "${USERPASS}")
			.withDomain("skillful-uat.macmillan.education")))
		.pause(1)
		.exec(http("request_getResourcesPage")
			.get(uri1 + "/resources/")
			.headers(headers_cookie)
			.check(regex("""Your Resources"""))
			.resources
			(
				http("request_getAWSCookies")
				.get(uri3 + "/?dm=27bac6181890453140bce750f0370dbc&action=load&blogid=28&siteid=1&back=http%3A%2F%2Fskillful-uat.macmillan.education%2Fresources%2F")
				.silent
				.headers(headers_1)
			)
		)
		.pause(1)
		.exec(http("request_getIdentity")
			.post("/index-xml.php?site_id=RDCv2")
			.headers(headers_5)
			.formParam("action", "is_valid_session_id")
			.formParam("SID", "${MEC_WS_SESSIONID}")
			.resources
			(
				http("request_getIdentity")
				.post(uri4 + "?site_id=RDCv2")
				.headers(headers_5)
				.formParam("action", "isloggedin")
				.formParam("SID", "${MEC_WS_SESSIONID}"),
	            http("request_getIdentity")
				.post(uri4 + "?site_id=RDCv2")
				.headers(headers_5)
				.formParam("action", "isloggedin")
				.formParam("SID", "${MEC_WS_SESSIONID}"),
	            http("request_getResources")
				.options(uri2 + "/ResourceLinks/?")
				.headers(headers_17),
	            http("request_getResources")
				.get(uri2 + "/ResourceLinks/?")
				.headers(headers_19)
				.check(regex("""L&S"""))
			)
		)

	setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
	//setUp(scn.inject(splitUsers(50) into (rampUsers(10) over (1 seconds)) separatedBy(5 seconds))).protocols(httpProtocol)
}