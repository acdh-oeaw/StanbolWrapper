package at.ac.oeaw.acdh;

import java.io.*;
import java.net.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class StanbolWrapperServlet
 */
@WebServlet("/StanbolWrapperServlet")
public class StanbolWrapperServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final int MAXFILESIZE = 10240;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public StanbolWrapperServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		if(request.getParameter("resourceUri") == null){
			response.getWriter().println("parameter resourceUri must be set");
			return;
		}
		
		if(request.getParameter("outFormat") == null){
			response.getWriter().println("parameter outFormat must be set");
			return;
		}
		
		byte[] buffer = new byte[1024];
		int bytesRead;
		
		
		
		
		URL stanbolURL = new URL(this.getInitParameter("stanbol.url"));
		HttpURLConnection stanbolCon = (HttpURLConnection) stanbolURL.openConnection();
		
		//stanbolCon.setDoInput(true);
		stanbolCon.setDoOutput(true);

		stanbolCon.setRequestMethod("POST");
		


		stanbolCon.setRequestProperty("Accept", request.getParameter("outFormat"));
		
		
		stanbolCon.setRequestProperty("Accept", "application/rdf+json");
		stanbolCon.setRequestProperty("Content-Type", "text/plain");
		
		OutputStream stanbolOut = stanbolCon.getOutputStream();
		
		stanbolOut.write("&content=".getBytes());
		 
		String txtSrcURLString = request.getParameter("resourceUri"); 
				
		URL txtSrcURL = new URL(txtSrcURLString);
		HttpURLConnection txtSrcCon = (HttpURLConnection) txtSrcURL.openConnection();
		InputStream txtSrcIn = txtSrcCon.getInputStream();

		bytesRead = 0;
		
		int sumOfBytesRead = 0;
		
		while((bytesRead = txtSrcIn.read(buffer)) >0){
		    sumOfBytesRead += bytesRead;
		    if(sumOfBytesRead > MAXFILESIZE) {
		        stanbolOut.write(URLEncoder.encode(new String(buffer, 0, (bytesRead + MAXFILESIZE - sumOfBytesRead)), "utf-8").getBytes());
		        break;
		    }
		    else {
			    stanbolOut.write(URLEncoder.encode(new String(buffer, 0, bytesRead), "utf-8").getBytes());
		    }
		}
		
		
		stanbolOut.flush();
		stanbolOut.close();
		txtSrcIn.close();
		

		InputStream stanbolIn = stanbolCon.getInputStream();
		OutputStream clientOut = response.getOutputStream();
		
		
		bytesRead = 0;
		
		if(sumOfBytesRead > MAXFILESIZE && ((bytesRead = stanbolIn.read(buffer)) > 0)) { //make a note on size restriction first
		    clientOut.write(("{\"_comment\": \"input restricted to " + MAXFILESIZE + " bytes...\",").getBytes());
		    clientOut.write(buffer, 1, bytesRead -1);
		}
		while((bytesRead = stanbolIn.read(buffer)) > 0){
			clientOut.write(buffer, 0, bytesRead);
		}
		clientOut.flush();
		clientOut.close();
		
		stanbolIn.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
