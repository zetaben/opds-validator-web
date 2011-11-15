package com.feedbooks.opds.webvalidator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;

import javax.servlet.http.*;

import org.apache.tools.ant.taskdefs.Sleep;
import org.json.JSONString;

import sun.awt.windows.ThemeReader;

import com.feedbooks.opds.*;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.repackaged.org.json.JSONObject;
import com.thaiopensource.xml.sax.ErrorHandlerImpl;
import static com.google.appengine.api.urlfetch.FetchOptions.Builder.*;

@SuppressWarnings("serial")
public class OPDSValidatorWebServlet extends HttpServlet {

	private void handleValidation(HttpServletRequest req,
			HttpServletResponse resp, String content, String name,
			String opds_version) throws IOException {
		Validator v = new Validator();

		v.SetOPDSVersion(opds_version);
		StringWriter validation_results = new StringWriter();
		ErrorHandlerImpl eh = new JSONErrorHandlerImpl(validation_results);
		v.SetErrorHandler(eh);

		String[] args = { content };
		String[] names = { name };
		boolean hadErr = v.validate(args, names);

		if (hadErr) {
			resp.setStatus(409);
		}
		eh.close();
		// resp.getOutputStream();
		BufferedWriter w = new BufferedWriter(resp.getWriter());
		w.append("{ \"results\": ");
		w.append(validation_results.getBuffer());
		w.append(", \"source\": ");
		w.append(JSONObject.quote(content));
		w.append("}");

		w.close();

	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("application/json");
		String uri = req.getParameter("uri");
		if (uri != null && !uri.equals("") && uri.startsWith("http")) {
			String opds_version = "1.0";
			String ve = req.getParameter("opds_version");
			if (ve != null && !ve.equals("")) {
				opds_version = ve;
			}

			URL url = new URL(req.getParameter("uri"));
			HTTPResponse get = URLFetchServiceFactory.getURLFetchService()
					.fetch(
							new HTTPRequest(url, HTTPMethod.GET,
									followRedirects().setDeadline(60.0)));
			String source = new String(get.getContent());

			handleValidation(req, resp, source, url.toString(), opds_version);

		} else {
			resp.setStatus(400);
		}
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("application/json");
		String feed = req.getParameter("feed");
		if (feed != null && !feed.equals("")) {
			String opds_version = "1.0";
			String ve = req.getParameter("opds_version");
			if (ve != null && !ve.equals("")) {
				opds_version = ve;
			}

			handleValidation(req, resp, feed, "direct", opds_version);

		} else {
			resp.setStatus(400);
		}
	}
}
