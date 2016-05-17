package test;

import java.io.IOException;
import java.io.PrintWriter;

import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class TestServlet
 */
@WebServlet(description = "TestServlet", urlPatterns = { "/TestServlet" })
public class TestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public TestServlet() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setContentType("application/text");
		PrintWriter out=response.getWriter();
		out.println("<h1>Hurray !!\n Servlet is Working!! </h1>");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setContentType("application/json"); 
		Gson gson = new Gson();
		try{
			StringBuilder sb = new StringBuilder();
			String s;
			while ((s=request.getReader().readLine())!=null){
				 sb.append(s); 
			}
			ProspectRegistrationModel ProspectRegistration= (ProspectRegistrationModel) gson.fromJson(sb.toString(), ProspectRegistrationModel.class);
			UserDetailModel[] userDetail=ProspectRegistration.getUserDetails();
			Status status=new Status();
			status.setEmailAddress(userDetail[0].emailAddress);
			System.out.println(userDetail[0].emailAddress);
			status.setTrackingId(1200);
			response.getOutputStream().print(gson.toJson(status));
			response.getOutputStream().flush();
		  } catch (Exception ex) {
			Status status=new Status();
			status.setTrackingId(-1);
			response.getOutputStream().print(gson.toJson(status));
			response.getOutputStream().flush();
		  }
	}
}
