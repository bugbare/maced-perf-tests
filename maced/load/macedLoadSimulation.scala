package maced.load

import scala.concurrent.duration._
import scala.util.Random

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class macedLoadSimulation extends Simulation {


	val httpProtocol = http
		.baseURL("https://registration-uat.ws.macmillaneducation.com")
		.acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
		.acceptLanguageHeader("en-US,en;q=0.5")
    	.acceptEncodingHeader("gzip, deflate")
    	.headers(HeadersAgent.headers_agent)
    	//.userAgentHeader("${agent}")
    	//.userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:42.0) Gecko/20100101 Firefox/42.0")
		.inferHtmlResources(BlackList(""".*\.js""", """.*\.css""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""", """.*\.(t|o)tf""", """.*\.png"""), WhiteList())

	val minWait = 3
	val maxWait = 10

	object GoHome {
		val goHome = group("Home") {
			exec(http("Go to the Home Page")
			.get(uri1 + "/")
			)
			.pause(minWait, maxWait)
		}
	}

	object GetWSSessionCookie {
		val getWSSessionCookie = exec(http("Get the WS Session Cookie")
			.get("/index-xml.php?site_id=RDCv2&action=get_session_id_html&parenturl=http%3A%2F%2Fskillful-uat.macmillan.education%2F")
			//.headers(headers_0)
			.check(regex("""WS_sessionID=(.*)"""").saveAs("MEC_WS_SESSIONID"))
			.resources
			(
				http("Initialise identity check")
				.post(uri4 + "?site_id=RDCv2")
				.headers(headers_NoCache)
				.formParam("action", "isloggedin")
				.formParam("SID", "${MEC_WS_SESSIONID}")
				.formParam("second_callback", "WS_INIT"),
	            http("Validate identity")
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
		val clickLogin = exec(http("Click on Login")
			.post("/index-xml.php?site_id=RDCv2")
			.headers(headers_NoCache)
			.formParam("action", "get_standalone_login_html")
			.formParam("SID", "${MEC_WS_SESSIONID}")
			.formParam("callback", "MEC_webservices.WS_launch_fancybox")
			)
		.pause(minWait, maxWait)
	}

	object SubmitLogin {
		val submitLogin = exec(http("Submit Login Details")
			.post("/index-xml.php?site_id=RDCv2")
			.headers(headers_NoCache)
			.formParam("username", "rshah")
			.formParam("password", "PerfTest01")
			.formParam("action", "loginpost")
			.formParam("SID", "${MEC_WS_SESSIONID}")
			.formParam("second_callback", "is_logged_in_hook")
			.resources
			(	
				http("Get SGK Cookie")
				.get(uri4 + "?site_id=RDCv2&action=get_sgk_cookie&SID=${MEC_WS_SESSIONID}")
				.headers(headers_acceptAll)
				.check(regex(""""SGK_USERPASS":"(.*)",""").saveAs("USERPASS")),
	            http("Get Mee OAuth Cookie")
				.get(uri4 + "?site_id=RDCv2&action=get_MEE_OAUTH_cookie&SID=${MEC_WS_SESSIONID}")
				.headers(headers_acceptAll)
				.check(regex(""""MEE_OAUTH_token":"(.*)",""").saveAs("MEE_OAUTH"))
			)
		)
		
		val createSessionCookies = exec(addCookie(Cookie("MEE_OAUTH", "${MEE_OAUTH}")
			.withDomain("skillful-uat.macmillan.education")))
		.exec(addCookie(Cookie("USERPASS", "${USERPASS}")
			.withDomain("skillful-uat.macmillan.education")))
	}

	object GoToResourcesPage {
		
		val goToResourcesPage = group("Go the Resources Page") {
				repeat(3) {
				exec(http("Go to the resources URL")
				.get(uri1 + "/resources/")
				.headers(headers_cookie)
				.check(regex("""Your Resources"""))
				)
				.exec(http("Get resources based on Identity")
				.post("/index-xml.php?site_id=RDCv2")
				.headers(headers_NoCache)
				.formParam("action", "is_valid_session_id")
				.formParam("SID", "${MEC_WS_SESSIONID}")
				.resources
					(
						http("Validate identity")
						.post(uri4 + "?site_id=RDCv2")
						.headers(headers_NoCache)
						.formParam("action", "isloggedin")
						.formParam("SID", "${MEC_WS_SESSIONID}"),
			            http("Validate identity")
						.post(uri4 + "?site_id=RDCv2")
						.headers(headers_NoCache)
						.formParam("action", "isloggedin")
						.formParam("SID", "${MEC_WS_SESSIONID}"),
			            http("Validate the resource links")
						.options(uri2 + "/ResourceLinks/?")
						.headers(headers_accessControl),
			            http("Get the resource links")
						.get(uri2 + "/ResourceLinks/?")
						.headers(headers_bearerAuth)
						.check(regex("""L&S"""))
					)
				)
			.pause(minWait, maxWait)
			}
		}
	}


	val headers_acceptAll = Map("Accept" -> "*/*")

	val headers_NoCache = Map(
		"Cache-Control" -> "no-cache",
		"Origin" -> "http://skillful-uat.macmillan.education",
		"Pragma" -> "no-cache")

	val headers_accessControl = Map(
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

    object HeadersAgent {

    	var agentArray = Array("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:42.0) Gecko/20100101 Firefox/42.0",
					    		"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1",
					    		"Mozilla/5.0 (X11; Linux i586; rv:31.0) Gecko/20100101 Firefox/31.0")
    	var agent = Random.shuffle(agentArray.toList).head
    	val headers_agent = Map("User-Agent" -> agent)
    }

    val uri1 = "http://skillful-uat.macmillan.education"
    val uri2 = "https://mee-uat-dataservices-subscription.ws.macmillaneducation.com/Svc/Subscription"
    val uri3 = "http://rdc-uat.macmillan.education"
    val uri4 = "https://registration-uat.ws.macmillaneducation.com/index-xml.php"

	val loginAndView = scenario("Login And View Resources")
		.exec(GoHome.goHome)
		.exec(GetWSSessionCookie.getWSSessionCookie)
		.exec(ClickLogin.clickLogin)
		.exec(SubmitLogin.submitLogin)
		.exec(SubmitLogin.createSessionCookies)
		.exec(GoToResourcesPage.goToResourcesPage)
		//.exec(GoToResourcesPage.getResources)

	val viewResources = scenario("View Resources")
		.exec(GetWSSessionCookie.getWSSessionCookie)
		.exec(SubmitLogin.submitLogin)
		.exec(SubmitLogin.createSessionCookies)
		.exec(GoToResourcesPage.goToResourcesPage)
		//.exec(GoToResourcesPage.getResources)


	/*setUp(
		viewResources.inject(atOnceUsers(3)),
		loginAndView.inject(atOnceUsers(3))
		).protocols(httpProtocol)*/
	/*setUp(
		viewResources.inject(splitUsers(200) into (rampUsers(15) over (5 seconds)) separatedBy(20 seconds)),
		loginAndView.inject(splitUsers(100) into (rampUsers(10) over (5 seconds)) separatedBy(10 seconds))
		).protocols(httpProtocol)*/
	
	setUp(loginAndView.inject(splitUsers(12) into (rampUsers(1) over (1 seconds)) separatedBy(5 seconds))).protocols(httpProtocol)
}