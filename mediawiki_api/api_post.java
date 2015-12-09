package mediawiki_api;

import core_objects.pair;
import gui_panels.gui_login_panel.STIKI_WATCHLIST_OPTS;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Andrew G. West - api_post.java - Complex API executions (such as logon,
 * and making edits), require data to be POSTED, rather than requested via
 * an HTTP-GET request. This class processes API requests that must be
 * POSTED, unlike its companion [api_retrieve], which is mainly for queries.
 * <p/>
 * The different actions possible enabled by this class are:
 * <p/>
 * [1]:  "login" -- Provided credentials, log a user onto Wikipedia --
 * and return a cookie with session variables/tokens. As of Apr.
 * 2010, this is a two-step process.
 * [2]:  "logout" -- Terminate a login session to Wikipedia
 * [3]:  "revert" -- Given an RID, revert the edit
 * [4]:  "rollback" -- Rollback from a page version
 * [5]:  "append text" -- Edit a Wikipedia page, by appending text
 * [6]:  "prepend text" -- Edit a Wikipedia page, by pre-pending text
 * [7]:  "edit full text" -- Edit a Wikipedia page, by providing the
 * complete replacement text for the page.
 * [8]:  "review rid" -- per the PendingChanges/FlaggedRevs programs
 * <p/>
 * POST-ing is a two-way street. In addition to sending the data, the server
 * also has an (XML) response. We build handlers to parse these responses:
 * <p/>
 * [9]:  "login" -- For cookie building pertaining to login.
 * [10]  "edit was made" -- Given the InputStream resulting from an
 * edit post. Was the edit actually made? (editing conflicts,
 * for instance, could cause such an action to fail).
 * [11]: "rollback was made" -- Given the InputStream resulting from
 * an edit post -- Did the rollback succeed?
 */
public class api_post {

	// **************************** PRIVATE FIELDS ***************************

	/**
	 * Three possible outcomes of a revert/rollback action.
	 */
	public enum EDIT_OUTCOME {
		SUCCESS, BEATEN, ASSERT_FAIL, ERROR
	}

	/**
	 * Watchlisting behavior when interacting with an article. This ENUM
	 * references how it is handled at the *per-edit* level. Contrast this
	 * with [gui_login_panel.STIKI_WATCHLIST_OPTS] which wraps
	 * watchlisting behavior at broader granularity.
	 */
	public enum EDIT_WATCHLIST {
		WATCH, UNWATCH, PREFERENCES, NOCHANGE
	}

	/**
	 * The [base_url()] function over which all API POSTS operate is
	 * specific to English Wikipedia. Should we wish to override it, this
	 * value can be set or overwritten. It's contents will be interpreted
	 * if it is a non-null value.
	 */
	public static String BASE_URL_OVERRIDE = null;


	// **************************** PUBLIC METHODS ***************************

	/**
	 * Return the base URL for making API POSTS over en.wp. Note that
	 * this is fixed aside from an internal code override option.
	 *
	 * @return Base URL for making API POSTS over en.wp.
	 */
	public static String base_url() {
		if (BASE_URL_OVERRIDE != null)
			return (BASE_URL_OVERRIDE);
		else return ("https://en.wikipedia.org/w/api.php");
	}


	// ******** POST HANDLERS *********

	/**
	 * Given credentials, login a user to Wikipedia (session-wise) --
	 * returning cookie-data sufficient to maintain a login-session
	 *
	 * @param user User-name of user to be logged in
	 * @param pass Password associated with user 'user'
	 * @return A semicolon separated list of pairs of the form "key=value",
	 * which constitute a cookie, and need to be issued with edit requests.
	 * If the login attempt falls for any reason, the structure will be NULL.
	 */
	public static String process_login(String user, String pass)
			throws Exception {

		// Build the initial request, and POST it
		String post_data = "action=login";
		post_data += "&lgname=" + URLEncoder.encode(user, "UTF-8");
		post_data += "&lgpassword=" + URLEncoder.encode(pass, "UTF-8");
		URLConnection con_token = post(post_data + "&format=xml", null);

		// Given this initial post, we get the token returned, and build
		// a temporary cookie containing a session identifier.
		api_xml_login token_handler = new api_xml_login();
		do_parse_work(con_token.getInputStream(), token_handler);
		String lgtoken = token_handler.get_result();
		String token_cookie = get_cookie_from_response(con_token);

		// We append this token to the previous request (second phase) --
		// Response is sufficient to build Wikipedia session cookie
		post_data += "&lgtoken=" + URLEncoder.encode(lgtoken, "UTF-8");
		post_data += "&format=xml";
		URLConnection con_cookie = post(post_data, token_cookie);
		api_xml_login cookie_handler = new api_xml_login();
		do_parse_work(con_cookie.getInputStream(), cookie_handler);
		return (cookie_handler.get_result());
	}

	/**
	 * Terminate the current session, as initiated by [process_login()].
	 */
	public static void process_logout() throws Exception {
		post("action=logout", null);
	}

	/**
	 * Revert a Wikipedia edit
	 *
	 * @param rid            Revision-ID of edit which should be undone
	 * @param title          Title of page on which 'rid' resides -- redundant?
	 * @param summary        Edit-summary to associate with reversion
	 * @param minor          Whether or not the edit should be marked 'minor'
	 * @param token          Edit token to edit page 'rid' -- user/session specific
	 * @param session_cookie Cookie string (as returned at login), which
	 *                       indicates the user doing the editing (in combination with 'token').
	 *                       Anonymous sessions should simply pass in NULL or the empty string.
	 * @param watchlist      Watchlist behavior for article edited
	 * @param assert_user    Whether edit should fail if user not logged in
	 * @return InputStream over server-response to the edit POST
	 */
	public static InputStream edit_revert(long rid, String title,
	                                      String summary, boolean minor, pair<String, String> edit_token,
	                                      String session_cookie, EDIT_WATCHLIST watchlist,
	                                      boolean assert_user) throws Exception {

		// Building post-string is straightforward. Fields known not
		// to contain special characters are not encoded.
		String post_data = "action=edit";
		post_data += "&undo=" + rid;
		post_data += "&title=" + URLEncoder.encode(title, "UTF-8");
		post_data += "&summary=" + URLEncoder.encode(summary, "UTF-8");
		if (minor)
			post_data += "&minor=true";
		else post_data += "&notminor=true";
		post_data += "&token=" + URLEncoder.encode(edit_token.fst, "UTF-8");
		post_data += "&starttimestamp=" + edit_token.snd;
		post_data += "&watchlist=" + watchlist.toString().toLowerCase();
		if (assert_user)
			post_data += "&assert=user";
		post_data += "&format=xml";
		return (api_post.post(post_data, session_cookie).getInputStream());
	}

	/**
	 * Rollback an edit (assuming the editor has sufficient permissions),
	 * which should be encoded in the session cookie (an argument).
	 *
	 * @param title       Title of the page on which 'rollback' will be used
	 * @param user        The OFFENDING EDITOR who will be rolled-back
	 * @param summary     Edit summary to leave with the rollback action
	 * @param rb_token    Rollback token (fetched at RID granularity)
	 * @param cookie      Cookie string (as returned at login) identifying user
	 * @param watchlist   Watchlist behavior for article edited
	 * @param assert_user Whether edit should fail if user not logged in
	 * @return InputStream over server-response to the edit POST
	 */
	public static InputStream edit_rollback(String title, String user,
	                                        String summary, String rb_token, String cookie,
	                                        EDIT_WATCHLIST watchlist, boolean assert_user) throws Exception {

		String post_data = "action=rollback";
		post_data += "&title=" + URLEncoder.encode(title, "UTF-8");
		post_data += "&user=" + URLEncoder.encode(user, "UTF-8");
		post_data += "&summary=" + URLEncoder.encode(summary, "UTF-8");
		post_data += "&token=" + URLEncoder.encode(rb_token, "UTF-8");
		post_data += "&watchlist=" + watchlist.toString().toLowerCase();
		if (assert_user)
			post_data += "&assert=user";
		post_data += "&format=xml";
		return (api_post.post(post_data, cookie).getInputStream());
	}

	/**
	 * Edit a Wikipedia article by appending text to the page.
	 *
	 * @param title       Title of the Wikipedia page being edited.
	 * @param summary     Edit-summary to associate with the edit
	 * @param append_text Text to be be appended to the page
	 * @param minor       Whether or not the edit should be marked 'minor'
	 * @param token       Edit token specific to user editing
	 * @param cookie      Cookie so edit will be mapped to logged-in user
	 * @param force       Respect token timestamp, or force edit committal?
	 * @param watchlist   Watchlist behavior for article edited
	 * @param assert_user Whether edit should fail if user not logged in
	 * @return InputStream over server-response to the edit POST
	 */
	public static InputStream edit_append_text(String title, String summary,
	                                           String append_text, boolean minor, pair<String, String> edit_token,
	                                           String cookie, boolean force, EDIT_WATCHLIST watchlist,
	                                           boolean assert_user) throws Exception {

		// UTF-encode all user-fields so URL format is sound
		String post_data = "action=edit";
		post_data += "&title=" + URLEncoder.encode(title, "UTF-8");
		post_data += "&summary=" + URLEncoder.encode(summary, "UTF-8");
		post_data += "&appendtext=" + URLEncoder.encode(append_text, "UTF-8");
		if (minor)
			post_data += "&minor=true";
		else post_data += "&notminor=true";

		post_data += "&token=" + URLEncoder.encode(edit_token.fst, "UTF-8");
		if (!force)
			post_data += "&starttimestamp=" + edit_token.snd;
		post_data += "&watchlist=" + watchlist.toString().toLowerCase();
		if (assert_user)
			post_data += "&assert=user";
		post_data += "&format=xml";
		return (api_post.post(post_data, cookie).getInputStream()); // Do it!
	}

	/**
	 * Edit a Wikipedia article by pre-pending text to the page.
	 *
	 * @param title        Title of the Wikipedia page being edited.
	 * @param summary      Edit-summary to associate with the edit
	 * @param minor        Whether or not the edit should be marked 'minor'
	 * @param prepend_text Text to be be prepended to the page
	 * @param token        Edit token specific to user editing
	 * @param cookie       Cookie so edit will be mapped to logged-in user
	 * @param force        Respect token timestamp, or force edit committal?
	 * @param watchlist    Watchlist behavior for article edited
	 * @param assert_user  Whether edit should fail if user not logged in
	 * @return InputStream over server-response to the edit POST
	 */
	public static InputStream edit_prepend_text(String title, String summary,
	                                            String prepend_text, boolean minor, pair<String, String> edit_token,
	                                            String cookie, boolean force, EDIT_WATCHLIST watchlist,
	                                            boolean assert_user) throws Exception {

		// UTF-encode all user-fields so URL format is sound
		String post_data = "action=edit";
		post_data += "&title=" + URLEncoder.encode(title, "UTF-8");
		post_data += "&summary=" + URLEncoder.encode(summary, "UTF-8");
		post_data += "&prependtext=" + URLEncoder.encode(prepend_text, "UTF-8");
		if (minor)
			post_data += "&minor=true";
		else post_data += "&notminor=true";

		post_data += "&token=" + URLEncoder.encode(edit_token.fst, "UTF-8");
		if (!force)
			post_data += "&starttimestamp=" + edit_token.snd;
		post_data += "&watchlist=" + watchlist.toString().toLowerCase();
		if (assert_user)
			post_data += "&assert=user";
		post_data += "&format=xml";
		return (api_post.post(post_data, cookie).getInputStream()); // Do it!
	}

	/**
	 * Edit a Wikipedia article by providing full page-replacement text.
	 *
	 * @param title       Title of the Wikipedia page being edited.
	 * @param summary     Edit-summary to associate with the edit
	 * @param append_text Text to be be appended to the page
	 * @param minor       Whether or not the edit should be marked 'minor'
	 * @param token       Edit token specific to user editing
	 * @param cookie      Cookie so edit will be mapped to logged-in user
	 * @param force       Respect token timestamp, or force edit committal?
	 * @param watchlist   Watchlist behavior for article edited
	 * @param assert_user Whether edit should fail if user not logged in
	 * @return InputStream over server-response to the edit POST
	 */
	public static InputStream edit_full_text(String title, String summary,
	                                         String full_text, boolean minor, pair<String, String> edit_token,
	                                         String cookie, boolean force, EDIT_WATCHLIST watchlist,
	                                         boolean assert_user) throws Exception {

		String post_data = "action=edit";
		post_data += "&title=" + URLEncoder.encode(title, "UTF-8");
		post_data += "&summary=" + URLEncoder.encode(summary, "UTF-8");
		post_data += "&text=" + URLEncoder.encode(full_text, "UTF-8");
		if (minor)
			post_data += "&minor=true";
		else post_data += "&notminor=true";

		post_data += "&token=" + URLEncoder.encode(edit_token.fst, "UTF-8");
		if (!force)
			post_data += "&starttimestamp=" + edit_token.snd;
		post_data += "&watchlist=" + watchlist.toString().toLowerCase();
		if (assert_user)
			post_data += "&assert=user";
		post_data += "&format=xml";
		return (api_post.post(post_data, cookie).getInputStream()); // Do it!
	}

	/**
	 * @param rid        Revision ID for which to approve an edit
	 * @param edit_token An edit token retrieved for the page(?)
	 * @param comment    Optional comment to post with review
	 * @param cookie     Cookie so action will be mapped to logged-in user
	 * @return InputStream over server-response to the edit POST
	 */
	public static InputStream review_rid(long rid,
	                                     pair<String, String> edit_token, String comment, String cookie)
			throws Exception {

		String post_data = "action=review";
		post_data += "&revid=" + rid;
		post_data += "&token=" + URLEncoder.encode(edit_token.fst, "UTF-8");
		if (comment != null && comment.length() > 0)
			post_data += "&comment=" + URLEncoder.encode(comment, "UTF-8");
		post_data += "&format=xml";
		return (api_post.post(post_data, cookie).getInputStream());
	}

	/**
	 * Convert STiki's global watchlist settings (which users pick) to one
	 * that applies at the per-edit level
	 *
	 * @param opt     STiki's global watchlist setting
	 * @param is_warn Should be set to TRUE if the edit in question is
	 *                a warning message/template. FALSE if this is an article revert.
	 * @return The per-edit watchlist strategy that should be applied
	 */
	public static EDIT_WATCHLIST convert_wl(
			STIKI_WATCHLIST_OPTS opt, boolean is_warn) {

		if (opt.equals(STIKI_WATCHLIST_OPTS.NEVER))
			return (EDIT_WATCHLIST.NOCHANGE);
		else if (opt.equals(STIKI_WATCHLIST_OPTS.ONLY_ARTICLES)) {
			if (is_warn)
				return (EDIT_WATCHLIST.NOCHANGE);
			else return (EDIT_WATCHLIST.WATCH);
		} else if (opt.equals(STIKI_WATCHLIST_OPTS.ONLY_USERTALK)) {
			if (is_warn)
				return (EDIT_WATCHLIST.WATCH);
			else return (EDIT_WATCHLIST.NOCHANGE);
		} else if (opt.equals(STIKI_WATCHLIST_OPTS.WATCH_BOTH))
			return (EDIT_WATCHLIST.WATCH);
		else // if(opt.equals(STIKI_WATCHLIST_OPTS.USER_PREFS))
			return (EDIT_WATCHLIST.PREFERENCES);
	}


	// **** POST-RESPONSE HANDLERS ****

	/**
	 * Execute an XML parse over the server-response from an edit
	 * POST, to determine if the edit was made or not.
	 *
	 * @param in InputStream containing server response from edit POST.
	 * @return Outcome of edit attempt. The {BEATEN} element will be
	 * returned if the edit would result in "nochange"
	 */
	public static EDIT_OUTCOME edit_was_made(InputStream in) throws Exception {
		api_xml_edit_response handler = new api_xml_edit_response();
		do_parse_work(in, handler);
		return (handler.get_result());
	}

	/**
	 * Execute an XML parse over the server-response from a rollback POST.
	 *
	 * @param in InputStream containing server response from rollback POST.
	 * @return Return zero (0) if the user was beaten to the revert.
	 * If successful, the RID (>0) of the earliest reverted edit will be
	 * returned. Responses less than zero (<0) are indicative of errors.
	 * In particular, "-2" is reserved for "badtoken", and "-3" is
	 * reserved for "assertuserfailed" errors.
	 */
	public static long rollback_response(InputStream in)
			throws Exception {
		api_xml_rb_response handler = new api_xml_rb_response();
		do_parse_work(in, handler);
		return (handler.get_result());
	}


	// *************************** PRIVATE METHODS ***************************

	/**
	 * POST a request to the Wikipedia-API (possibly with cookies).
	 *
	 * @param post_data Data to be posted. Must already be properly encoded
	 *                  in UTF-8 formatting so as not to break the URL-fetching.
	 * @param cookie    String of semicolon-separated pairs of the form
	 *                  "key=value", which are cookie elements sent with the POST request.
	 *                  If the post request doesn't require these session variables, or if the
	 *                  user is anonymous, either null or the empty string may be passed.
	 * @return The URLConnection created/used. The HTTP response can easily be
	 * obtained by calling the con.getInputStream() method.
	 */
	private static URLConnection post(String post_data, String cookie)
			throws Exception {

		// Open up the connection
		URL url = new URL(base_url());
		URLConnection conn = url.openConnection();

		// If provided, insert the cookie data into headers
		if ((cookie != null) && (!cookie.isEmpty()))
			conn.setRequestProperty("Cookie", cookie);

		// Then make the POST request
		conn.setDoOutput(true);
		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		wr.write(post_data);
		wr.flush();
		return (conn);
	}

	/**
	 * Parse an XML document, given an InputStream and XML-handler.
	 *
	 * @param in InputStream operating over XML content
	 * @param dh XML handler designed for data from 'in'
	 */
	private static void do_parse_work(InputStream in, DefaultHandler handler)
			throws Exception {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		parser.parse(in, handler); // Parse
		in.close(); // Close up the URL-connection
	}

	/**
	 * Fetch a cookie sent from a URL connection/response.
	 *
	 * @param uc URL connection/response containing a cookie object
	 * @return String version of the cookie object sent by 'uc'. It may
	 * be possible for a response to contain multiple cookies. This method
	 * should not be used in that case; only when 0 or 1 cookies are expected.
	 * Empty string is returned if no cookies are present.
	 */
	private static String get_cookie_from_response(URLConnection uc) {
		String cookie = "", headerName = null;
		for (int i = 1; (headerName = uc.getHeaderFieldKey(i)) != null; i++) {
			if (headerName.equalsIgnoreCase("Set-Cookie"))
				cookie += uc.getHeaderField(i); // Must APPEND
		} // An HTTP response contains many headers; not all cookies
		return (cookie);
	}

}
