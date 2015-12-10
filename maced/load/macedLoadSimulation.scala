package maced.load

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class macedLoadSimulation extends Simulation {

	val httpProtocol = http
		.baseURL("https://registration-uat.ws.macmillaneducation.com")
		.acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
		.acceptLanguageHeader("en-US,en;q=0.5")
    	.acceptEncodingHeader("gzip, deflate")
    	.userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:42.0) Gecko/20100101 Firefox/42.0")
		.inferHtmlResources(BlackList(""".*\.js""", """.*\.css""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""", """.*\.(t|o)tf""", """.*\.png"""), WhiteList())

	object GoHome {
		val goHome = exec(http("request_home")
			.get(uri1 + "/"))
			.pause(1)
	}

	object GetWSSessionCookie {
		val getWSSessionCookie = exec(http("request_getSessionCookie")
			.get("/index-xml.php?site_id=RDCv2&action=get_session_id_html&parenturl=http%3A%2F%2Fskillful-uat.macmillan.education%2F")
			//.headers(headers_0)
			.check(regex("""WS_sessionID=(.*)"""").saveAs("MEC_WS_SESSIONID"))
			.resources
			(
				http("request_checkLoginStatus")
				.post(uri4 + "?site_id=RDCv2")
				.headers(headers_NoCache)
				.formParam("action", "isloggedin")
				.formParam("SID", "${MEC_WS_SESSIONID}")
				.formParam("second_callback", "WS_INIT"),
	            http("request_checkLoginStatus")
				.post(uri4 + "?site_id=RDCv2")
				.headers(headers_NoCache)
				.formParam("action", "isloggedin")
				.formParam("SID", "${MEC_WS_SESSIONID}")
				.formParam("second_callback", "is_logged_in_hook")
			)
		)
		.exec(addCookie(Cookie("MEC_WS_SESSIONID", "${MEC_WS_SESSIONID}")
			.withDomain("skillful-uat.macmillan.education")))
		.pause(1)
	}

	object ClickLogin {
		val clickLogin = exec(http("request_getLoginForm")
			.post("/index-xml.php?site_id=RDCv2")
			.headers(headers_NoCache)
			.formParam("action", "get_standalone_login_html")
			.formParam("SID", "${MEC_WS_SESSIONID}")
			.formParam("callback", "MEC_webservices.WS_launch_fancybox")
			)
		.pause(5)
	}

	object SubmitLogin {
		val submitLogin = exec(http("request_submitLoginDetails")
			.post("/index-xml.php?site_id=RDCv2")
			.headers(headers_NoCache)
			.formParam("username", "rshah")
			.formParam("password", "PerfTest01")
			.formParam("action", "loginpost")
			.formParam("SID", "${MEC_WS_SESSIONID}")
			.formParam("second_callback", "is_logged_in_hook")
			.resources
			(	
				http("request_getSgkCookie")
				.get(uri4 + "?site_id=RDCv2&action=get_sgk_cookie&SID=${MEC_WS_SESSIONID}")
				.headers(headers_acceptAll)
				.check(regex(""""SGK_USERPASS":"(.*)",""").saveAs("USERPASS")),
	            http("request_getMeeOAuthCookie")
				.get(uri4 + "?site_id=RDCv2&action=get_MEE_OAUTH_cookie&SID=${MEC_WS_SESSIONID}")
				.headers(headers_acceptAll)
				.check(regex(""""MEE_OAUTH_token":"(.*)",""").saveAs("MEE_OAUTH"))
			)
		)
	}

	object CreateSessionCookies {
		val createSessionCookies = exec(addCookie(Cookie("MEE_OAUTH", "${MEE_OAUTH}")
			.withDomain("skillful-uat.macmillan.education")))
		.exec(addCookie(Cookie("USERPASS", "${USERPASS}")
			.withDomain("skillful-uat.macmillan.education")))
		.pause(1)
	}

	object GoToResourcesPage {
		val goToResourcesPage = exec(http("request_getResourcesPage")
			.get(uri1 + "/resources/")
			.headers(headers_cookie)
			.check(regex("""Your Resources"""))
		)
		.pause(1)
	}

	object GetResources {
		val getResources = exec(http("request_getIdentity")
			.post("/index-xml.php?site_id=RDCv2")
			.headers(headers_NoCache)
			.formParam("action", "is_valid_session_id")
			.formParam("SID", "${MEC_WS_SESSIONID}")
			.resources
			(
				http("request_getIdentity")
				.post(uri4 + "?site_id=RDCv2")
				.headers(headers_NoCache)
				.formParam("action", "isloggedin")
				.formParam("SID", "${MEC_WS_SESSIONID}"),
	            http("request_getIdentity")
				.post(uri4 + "?site_id=RDCv2")
				.headers(headers_NoCache)
				.formParam("action", "isloggedin")
				.formParam("SID", "${MEC_WS_SESSIONID}"),
	            http("request_getResources")
				.options(uri2 + "/ResourceLinks/?")
				.headers(headers_accessControl),
	            http("request_getResources")
				.get(uri2 + "/ResourceLinks/?")
				.headers(headers_bearerAuth)
				.check(regex("""L&S"""))
			)
		)
		.pause(1)
	}


	val headers_acceptAll = Map("Accept" -> "*/*")

	//val headers_acceptText = Map("Accept" -> "text/css,*/*;q=0.1")

	//val headers_acceptFont = Map(
	//	"Accept" -> "application/font-woff2;q=1.0,application/font-woff;q=0.9,*/*;q=0.8",
	//	"Accept-Encoding" -> "identity")

	val headers_NoCache = Map(
		"Cache-Control" -> "no-cache",
		"Origin" -> "http://skillful-uat.macmillan.education",
		"Pragma" -> "no-cache")

	val headers_accessControl = Map(
		//"Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
		"Access-Control-Request-Headers" -> "authorization",
		"Access-Control-Request-Method" -> "GET",
		"Origin" -> "http://skillful-uat.macmillan.education")

	val headers_bearerAuth = Map(
		"Accept" -> "application/json, text/javascript, */*; q=0.01",
		"Origin" -> "http://skillful-uat.macmillan.education",
		"Authorization" -> "Bearer ${MEE_OAUTH}")

	val headers_cookie = Map(
		"Referer" -> "http://skillful-uat.macmillan.education/",
		"Cookie" -> "MEC_WS_SESSIONID=${MEC_WS_SESSIONID}; USERPASS=${USERPASS}; MEE_OAUTH=${MEE_OAUTH}"
		)

    val uri1 = "http://skillful-uat.macmillan.education"
    val uri2 = "https://mee-uat-dataservices-subscription.ws.macmillaneducation.com/Svc/Subscription"
    val uri3 = "http://rdc-uat.macmillan.education"
    val uri4 = "https://registration-uat.ws.macmillaneducation.com/index-xml.php"

	val loginAndView = scenario("Login And View Resources")
		.exec(GoHome.goHome)
		.exec(GetWSSessionCookie.getWSSessionCookie)
		.exec(ClickLogin.clickLogin)
		.exec(SubmitLogin.submitLogin)
		.exec(CreateSessionCookies.createSessionCookies)
		.exec(GoToResourcesPage.goToResourcesPage)
		.exec(GetResources.getResources)

	val viewResources = scenario("View Resources")
		.exec(GetWSSessionCookie.getWSSessionCookie)
		.exec(SubmitLogin.submitLogin)
		.exec(CreateSessionCookies.createSessionCookies)
		.exec(GetResources.getResources)


	/*setUp(
		viewResources.inject(atOnceUsers(1)),
		loginAndView.inject(atOnceUsers(1))
		).protocols(httpProtocol)*/
	setUp(
		viewResources.inject(splitUsers(100) into (rampUsers(10) over (20 seconds)) separatedBy(5 seconds)),
		loginAndView.inject(splitUsers(100) into (rampUsers(10) over (20 seconds)) separatedBy(5 seconds))
		).protocols(httpProtocol)
	
	//setUp(loginAndView.inject(splitUsers(100) into (rampUsers(10) over (5 seconds)) separatedBy(5 seconds))).protocols(httpProtocol)
}