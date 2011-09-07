package com.feedbooks.opds.webvalidator;

import java.io.IOException;
import javax.servlet.http.*;
import com.feedbooks.opds.*;
import com.thaiopensource.xml.sax.ErrorHandlerImpl;

@SuppressWarnings("serial")
public class OPDSValidatorWebServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("application/json");

		String uri = req.getParameter("uri");
		if (uri != null && !uri.equals("") && uri.startsWith("http")) {
			Validator v = new Validator();
			String opds_version = "1.0";
			String ve = req.getParameter("opds_version");
			if (ve != null && !ve.equals("")) {
				opds_version = ve;
			}
			v.SetOPDSVersion(opds_version);
			ErrorHandlerImpl eh = new JSONErrorHandlerImpl(resp
					.getOutputStream());
			v.SetErrorHandler(eh);
			boolean hadErr = v.validate(req.getParameterValues("uri"));

			if (hadErr) {
				resp.setStatus(409);
			}

			eh.close();
		} else {
			resp.setStatus(400);
		}
	}
}
