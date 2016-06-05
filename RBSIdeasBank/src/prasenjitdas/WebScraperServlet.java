/* This program fetches data from RBS Ideas Bank page
 * and parses the data to create a csv file
 */

package prasenjitdas;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.channels.Channels;
import java.security.GeneralSecurityException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.*;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;
import com.google.api.services.prediction.Prediction;
import com.google.api.services.prediction.PredictionScopes;
import com.google.api.services.prediction.model.Input;
import com.google.api.services.prediction.model.Input.InputInput;
import com.google.api.services.prediction.model.Output;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

@SuppressWarnings("serial")
public class WebScraperServlet extends HttpServlet {
	
	private static final GcsService gcsService = GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());
	private static final String PROJECT_ID = "786374646928";
	private static final String MODEL_ID = "lobPredictor";
	private static final String MODEL2_ID = "sentimentPredictor";
	private static final String APPLICATION_NAME = "certain-density-126216.appspot.com";
	// Global instance of the JSON factory.
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	// Global instance of the HTTP transport.
	private static HttpTransport httpTransport;
	
	private String stringLastRunDate;
	private Date lastRunDate;
	private static SimpleDateFormat dateFormatter;
	private String stringLastRunTime;
	private Time lastRunTime;
	private static SimpleDateFormat timeFormatter;
	private static final TimeZone UTC = TimeZone.getTimeZone("GMT");
	
	private boolean newComment=false;
	private String htmlBody=new String();
	private int pageCount=0;
	private Date updateLastRunDate=null;
	private Time updateLastRunTime=null;
	
	public void init(ServletConfig config)
			throws ServletException{
	/** This date & time has to be changed to the last run date if the code is re-deployed */
	/** All the comments with date greater than stringLastRunDate will be written in the message body */	
		stringLastRunDate="17-04-2016";
		stringLastRunTime="12:00 AM";
	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {		
		String key=new String();
		Cache cache = null;
		Calendar midnight = null;
		Calendar fiveMinutesPastMidnight = null;
		boolean getLatestComment=true;
		boolean getSentiment=false;
		StringBuilder delimitedData=new StringBuilder();
		
		try{
			CacheFactory cacheFactory=CacheManager.getInstance().getCacheFactory();
			cache=cacheFactory.createCache(Collections.emptyMap());
		}catch(CacheException e){
			System.err.println("Something went wrong while formatting cache");
			e.printStackTrace();
		}
		//Fetch lastRunDate from memcache
		key="lastRunDate";
		//if lastRunDate is not available in memcache then use default value
		dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
		if(cache.containsKey(key)){
			lastRunDate=(Date) cache.get(key);
		}else{
			//Convert the lastRunDate from String to Date
			try{
				lastRunDate=dateFormatter.parse(stringLastRunDate);
			}catch (ParseException e){
				System.err.println("Something went wrong while formatting lastRunDate");
				e.printStackTrace();
			}
		}
		//Use the last run date from request parameter if it is available. This will override memcache value
		String date = req.getParameter("date");
		if(date==null){
			;
		}else{
			stringLastRunDate=date;
			try {
				lastRunDate=dateFormatter.parse(stringLastRunDate);
			} catch (ParseException e) {
				System.err.println("Something went wrong while formatting lastRunDate");
				e.printStackTrace();
			}
		}
		//Fetch lastRunTime from memcache
		key="lastRunTime";
		//if lastRunTime is not available in memcache then use default value
		timeFormatter = new SimpleDateFormat("h:mm a");
		if(cache.containsKey(key)){
			lastRunTime=(Time) cache.get(key);
		}else{
			//Convert the lastRunTime from String to Time
			try{
				lastRunTime=new Time(timeFormatter.parse(stringLastRunTime).getTime());
			}catch (ParseException e){
				System.err.println("Something went wrong while formatting lastRunTime");
				e.printStackTrace();
			}
		}
		//Set time for daily report time check
		midnight = Calendar.getInstance(UTC);
		midnight.set(Calendar.HOUR, 0);
		midnight.set(Calendar.AM_PM, Calendar.AM);
		midnight.set(Calendar.HOUR_OF_DAY, 0);
		midnight.set(Calendar.MINUTE, 0);
		midnight.set(Calendar.SECOND, 0);
		midnight.set(Calendar.MILLISECOND, 0);
		fiveMinutesPastMidnight = Calendar.getInstance(UTC);
		fiveMinutesPastMidnight.set(Calendar.HOUR, 0);
		fiveMinutesPastMidnight.set(Calendar.AM_PM, Calendar.AM);
		fiveMinutesPastMidnight.set(Calendar.HOUR_OF_DAY, 0);
		fiveMinutesPastMidnight.set(Calendar.MINUTE, 5);
		fiveMinutesPastMidnight.set(Calendar.SECOND, 0);
		fiveMinutesPastMidnight.set(Calendar.MILLISECOND, 0);
		//If this is module is run at any other than other than midnight it will only fetch the latest comments
		if(Calendar.getInstance(UTC).getTime().after(midnight.getTime()) && Calendar.getInstance(UTC).getTime().before(fiveMinutesPastMidnight.getTime())){
			getLatestComment=false;
		}
		//If run now parameter in the request is set then get all the comments
		String runtype=req.getParameter("runtype");
		if(runtype==null){
			
		}else{
			if(req.getParameter("runtype").equals("fullreport"))
			{
				getLatestComment=false;
			}
		}
		//If sentiment analysis is not checked then don't call sentiment API
		String sentiment = req.getParameter("sentiment");
		if(sentiment==null){
			;
		}else{
			if(sentiment.equals("getSentiment")){
				getSentiment=true;
			}
		}
		//Parse the RBS Idea Bank Portal and obtain the comma delimited output String
		try {
			delimitedData=parsePage(getLatestComment, getSentiment);
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Once all the records are processed update the lastRunDate/lastRun Time to the date/time of the first comment on the first page i.e. the latest comment
		if(newComment){
			lastRunDate=updateLastRunDate;
			lastRunTime=updateLastRunTime;
			//Write the lastRunDate and lastRunTime to memcache
			key="lastRunDate";
			cache.put(key, lastRunDate);
			key="lastRunTime";
			cache.put(key, lastRunTime);
		}
		//Display number of pages scanned
		resp.getWriter().println(pageCount+" pages scanned");
		//Write the data to cloud storage
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		Date now = new Date();
		String strDate = sdfDate.format(now);
		String filename="output-"+strDate+".csv";
		if(!getLatestComment){
			writeOutputCloudStorage(delimitedData,filename);
		}
		//Write the number of pages scanned and recent messages in the mail body if there are any
		if (htmlBody.isEmpty() &&  getLatestComment){
			;
		}else{
			htmlBody=htmlBody+pageCount+" pages scanned";
			//Email the content in mail body without attachment
			if(getLatestComment){
				delimitedData.delete(0, delimitedData.length());
				delimitedData=delimitedData.append(";;;;;;;;;");
			}
			sendMessage(htmlBody, delimitedData, filename);
		}
	}
	
	public StringBuilder parsePage(boolean getLatestComment, boolean getSentiment) throws IOException, GeneralSecurityException{
		int pageNumber=0;
		int apiUsageCount=0;
		long startTime=System.currentTimeMillis();
		boolean processAll=true;
		boolean labelIsPredicted=false;
		String url=new String();
		String author=new String();
		String subject=new String();
		String status=new String();
		String date=new String();
		String time=new String();
		String userComment=new String();
		String dateTime=new String();
		String label1=new String(), label2=new String(), label3=new String(), predictedLabel=new String(), predictedSentiment = new String();
		Date commentDate=null;
		Time commentTime=null;
		Document doc=null;
		Elements ideaMessagetInput=null;
		Element body=null, bodyPrev=null;
		StringBuilder delimitedData=new StringBuilder();
		
		pageCount=0;
		htmlBody="";
		newComment=false;
		updateLastRunDate=null;
		updateLastRunTime=null;
		
		delimitedData=delimitedData.append("Subject,Status,Author,Date,Time,Comment,Label1,Label2,Label3,Predicted,Sentiment\n");
		while(processAll)
		{
			//Increment  Page Number for fetching next page
			pageNumber++;
			url="https://www.communities.rbs.co.uk/t5/Ideas-Bank/idb-p/idea/tab/most-recent/page/"+pageNumber;
			try {
				doc=Jsoup.connect(url).get();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			body=doc.body();
			if(bodyPrev!=null && bodyPrev.text().equals(body.text())){
				processAll=false;
				break;
			}
			bodyPrev=body;
			//Increment count of pages fetched
			pageCount++;
			//Select all occurrences of idea message on the current page
			ideaMessagetInput = body.select("div[class=lia-quilt lia-quilt-idea-message lia-quilt-layout-idea-message]");
			for(int messageCounter=0; messageCounter<ideaMessagetInput.size();messageCounter++){
				//Select the author
				try{
					author="";
					author=ideaMessagetInput.get(messageCounter).select("a[class=lia-link-navigation lia-page-link lia-user-name-link]").text();
				}catch(Exception e){
					e.printStackTrace();
				}
				//Select the subject
				try{
					subject="";
					subject=ideaMessagetInput.get(messageCounter).select("a[class=lia-link-navigation idea-article-link]").text();
				}catch(Exception e){
					e.printStackTrace();
				}
				//Select the status
				try{
					status="";
					status=ideaMessagetInput.get(messageCounter).select("a[class=lia-link-navigation message-status-link]").get(0).text();
				}catch(Exception e){
					e.printStackTrace();
				}
				//Select the posted date
				try{
					date="";
					date=ideaMessagetInput.get(messageCounter).select("span[class=local-date]").text();
				}catch(Exception e){
					e.printStackTrace();
				}
				//Select the posted time
				try{
					time="";
					time=ideaMessagetInput.get(messageCounter).select("span[class=local-time]").text();
				}catch(Exception e){
					e.printStackTrace();
				}	
				if (date.isEmpty()){
					try{
						dateTime="";
						dateTime=ideaMessagetInput.get(messageCounter).select("span[class=local-friendly-date]").attr("title");
					}catch(Exception e){
						e.printStackTrace();
					}
					date="";
					date=dateTime.substring(0, 11);
					time="";
					time=dateTime.substring(13, 20);
				}
				//Select the user comment
				try{
					userComment="";
					userComment=ideaMessagetInput.get(messageCounter).select("div[class=lia-message-body-content]").text();
				}catch(Exception e){
					e.printStackTrace();
				}
				//Select the label1
				try{
					label1="";
					label1=ideaMessagetInput.get(messageCounter).select("a[class=label-link lia-link-navigation]").get(0).text();
				}catch(Exception e){
					e.printStackTrace();
				}
				//Select the label2
				try{
					label2="";
					label2=ideaMessagetInput.get(messageCounter).select("a[class=label-link lia-link-navigation]").get(1).text();
				}catch(Exception e){
					e.printStackTrace();
				}
				//Select the label3
				try{
					label3="";
					label3=ideaMessagetInput.get(messageCounter).select("a[class=label-link lia-link-navigation]").get(2).text();
				}catch(Exception e){
					e.printStackTrace();
				}
				//Subject
				delimitedData=delimitedData.append(subject.replace(",", ""));
				delimitedData=delimitedData.append(",");
				//Status
				delimitedData=delimitedData.append(status.replace(",", ""));
				delimitedData=delimitedData.append(",");
				//Author
				delimitedData=delimitedData.append(author.replace(",", ""));
				delimitedData=delimitedData.append(",");
				//Add date to output, edited date will not be displayed
				delimitedData=delimitedData.append(date.substring(1,11).trim());
				delimitedData=delimitedData.append(",");
				//Add time to output, edited time will not be displayed
				delimitedData=delimitedData.append(time.trim());
				delimitedData=delimitedData.append(",");			
				//Add User Comment
				delimitedData=delimitedData.append(userComment.replace(",", "").trim());
				//If labels are not available call Google Predictor API for a label
				if (label1.isEmpty() && label2.isEmpty() && label3.isEmpty()){
					try{
						if(apiUsageCount==90){
							if((System.currentTimeMillis() - startTime)<100000){
								while((System.currentTimeMillis() - startTime)<100000);
								//Reset the variable and proceed
								startTime=System.currentTimeMillis();
								apiUsageCount=0;
							}
						}
						predictedLabel=predictLabel(userComment.replace(",", "").trim());
						apiUsageCount++;
						delimitedData=delimitedData.append(",");
						delimitedData=delimitedData.append(predictedLabel.replace(",", ""));
						delimitedData=delimitedData.append(",");
						delimitedData=delimitedData.append(",");
						delimitedData=delimitedData.append(",");
						//Add an identifier that the label is a predicted value
						delimitedData=delimitedData.append("Y");
						labelIsPredicted=true;
					}catch (IOException | GeneralSecurityException e){
						System.err.println("Something went wrong while predicting");
						e.printStackTrace();
					}finally{
						delimitedData=delimitedData.append("\n");
					}
				}else{
					delimitedData=delimitedData.append(",");
				}
				//Add label1
				if(!label1.isEmpty()){
					delimitedData=delimitedData.append(label1.replace(",", ""));
					if(label2.isEmpty() && label3.isEmpty()){
						delimitedData=delimitedData.append("\n");
					}
					else{
						delimitedData=delimitedData.append(",");
					}
				}
				//Add label2
				if(!label2.isEmpty()){
					delimitedData=delimitedData.append(label2.replace(",", ""));
					if (label3.isEmpty()){
						delimitedData=delimitedData.append("\n");
					}else{
						delimitedData=delimitedData.append(",");
					}
				}
				//Add label3
				if(!label3.isEmpty()){
					delimitedData=delimitedData.append(label3.replace(",", ""));
					delimitedData=delimitedData.append("\n");
				}
				//Convert the comment date to a Date object
				try{
					commentDate=dateFormatter.parse(date.substring(1,11).trim());
				}catch (ParseException e){
					System.err.println("Something went wrong while formatting commentDate");
					e.printStackTrace();
				}
				//If the api has been called 90 times within 100 seconds then wait for quota to be available
				if(apiUsageCount==90){
					if((System.currentTimeMillis() - startTime)<100000){
						while((System.currentTimeMillis() - startTime)<100000);
						//Reset the variable and proceed
						startTime=System.currentTimeMillis();
						apiUsageCount=0;
					}
				}
				//Predict the sentiment
				if(getSentiment){
					predictedSentiment=predictSentiment(userComment.replace(",", "").trim());
					apiUsageCount++;
					delimitedData=delimitedData.deleteCharAt(delimitedData.length()-1);
					if(label2.isEmpty() && label3.isEmpty() && !labelIsPredicted){
						delimitedData=delimitedData.append(",");
						delimitedData=delimitedData.append(",");
						delimitedData=delimitedData.append(",");
					}else{
						if(label3.isEmpty() && !labelIsPredicted){
							delimitedData=delimitedData.append(",");
							delimitedData=delimitedData.append(",");
						}else
							if(!labelIsPredicted){
								delimitedData=delimitedData.append(",");
							}
					}
					//Initialize labelIsPredicted for processing next row
					labelIsPredicted=false;
					delimitedData=delimitedData.append(",");
					delimitedData=delimitedData.append(predictedSentiment.replace(",", ""));
					delimitedData=delimitedData.append("\n");
				}
				//Convert the comment time to a Time object
				try{
					commentTime=new Time(timeFormatter.parse(time.trim()).getTime());
				}catch (ParseException e){
					System.err.println("Something went wrong while formatting commentTime"+time.trim());
					e.printStackTrace();
				}
				//If this is a new comment add it to the message body
				if(commentDate.after(lastRunDate) || (commentDate.equals(lastRunDate) && commentTime.after(lastRunTime))){
					if(label1.isEmpty() && label2.isEmpty() && label3.isEmpty()){
						htmlBody=htmlBody+subject.replace(",", "")+" ("+predictedLabel+")"+"<br>"+userComment.replace(",", "").trim()+"<br><br>";
					}else{
						htmlBody=htmlBody+subject.replace(",", "")+" ("+label1+")"+"<br>"+userComment.replace(",", "").trim()+"<br><br>";
					}	
					//Update the lastRunDate/lastRun Time to the date/time of the first comment on the first page i.e. the latest comment
					if(!newComment){
						updateLastRunDate=commentDate;
						updateLastRunTime=commentTime;
					}
					newComment=true;
				}else{
				//Stop processing pages once comments added since last run are processed if getLatestComment flag is true
					if(getLatestComment){
						processAll=false;
						break;
					}
				}
			}
		}
		return delimitedData;
	}
	
	public void writeOutputCloudStorage(StringBuilder data, String filename){
		GcsFilename GoogleCloudFilename = new GcsFilename(APPLICATION_NAME,filename);
		GcsOutputChannel outputChannel = null;
		try {
			outputChannel = gcsService.createOrReplace(GoogleCloudFilename, GcsFileOptions.getDefaultInstance());
		} catch (IOException e) {
			System.err.println("Something went wrong while writing to Google Cloud Storage.");
			e.printStackTrace();
		}
		OutputStream output=Channels.newOutputStream(outputChannel);
		PrintStream printOutput=new PrintStream(output);
		printOutput.print(data.toString());
		printOutput.close();
	}
	
	public void sendMessage(String messageBody, StringBuilder attachmentData, String attachmentFilename){
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
	    try {
	    	Multipart mp = new MimeMultipart();
		    MimeBodyPart htmlPart = new MimeBodyPart();
	    	htmlPart.setContent(messageBody, "text/html");
	    	mp.addBodyPart(htmlPart);
	    	
	    	if(attachmentData.toString().equals(";;;;;;;;;")){
	    		;
	    	}else{
	    		MimeBodyPart attachment = new MimeBodyPart();
		    	String attachmentDataString = attachmentData.toString();
		    	byte[] attachmentDataByte = attachmentDataString.getBytes();
		    	InputStream attachmentDataStream = new ByteArrayInputStream(attachmentDataByte);
		    	attachment.setFileName(attachmentFilename);
		    	attachment.setContent(attachmentDataStream, "text/comma-separated-values");
		    	mp.addBodyPart(attachment);
	    	}
	    		
	    	Message msg = new MimeMessage(session);
	    	msg.setFrom(new InternetAddress("daemon@certain-density-126216.appspotmail.com", "WebScraperDaemon"));
	    	msg.addRecipient(Message.RecipientType.TO, new InternetAddress("prasenjit.das@gmail.com", "Prasenjit Das"));
	    	msg.addRecipient(Message.RecipientType.CC, new InternetAddress("prasenjit_das@infosys.com", "Prasenjit Das"));
	    	msg.setSubject("Output from RBS communities");
	    	msg.setContent(mp);
	    	Transport.send(msg);
	    } catch (AddressException e) {
	    	System.err.println("Something went wrong while sending the mail.");
	    	throw new RuntimeException(e);
	    } catch (MessagingException e) {
	    	System.err.println("Something went wrong while sending the mail.");
	    	throw new RuntimeException(e);
	    } catch (UnsupportedEncodingException e) {
	    	System.err.println("Something went wrong while sending the mail.");
			e.printStackTrace();
		}
	}
	
	public String predictLabel(String text) throws IOException, GeneralSecurityException {
		httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		GoogleCredential credential = GoogleCredential.getApplicationDefault();
		if (credential.createScopedRequired()) {
		    credential = credential.createScoped(PredictionScopes.all());
		}
		Prediction prediction = new Prediction.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
	    Input input = new Input();
	    InputInput inputInput = new InputInput();
	    inputInput.setCsvInstance(Collections.<Object>singletonList(text));
	    input.setInput(inputInput);
	    Output output = prediction.trainedmodels().predict(PROJECT_ID, MODEL_ID, input).execute();
	    return output.getOutputLabel();
	 }
	
	public String predictSentiment(String text) throws IOException, GeneralSecurityException {
		httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		GoogleCredential credential = GoogleCredential.getApplicationDefault();
		if (credential.createScopedRequired()) {
		    credential = credential.createScoped(PredictionScopes.all());
		}
		Prediction prediction = new Prediction.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
	    Input input = new Input();
	    InputInput inputInput = new InputInput();
	    inputInput.setCsvInstance(Collections.<Object>singletonList(text));
	    input.setInput(inputInput);
	    Output output = prediction.trainedmodels().predict(PROJECT_ID, MODEL2_ID, input).execute();
	    return output.getOutputLabel();
	 }
}