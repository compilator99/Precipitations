/**
 * 
 */
package http.file.processor;

/**
 * @author home
 *
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


public class FileProcessor {

	/**
	 * @param args
	 */
	private static String urlString = "https://www.ncei.noaa.gov/data/normals-daily/access/";
	
	public static Map<String, LinkedList<Double>> elevations = new HashMap<>();
	public static Map<String, LinkedList<Double>> daily25Prctl = new HashMap<>();
	
	public static String[] blanks = new String[] {"-9999", "-7777", "-6666", "-4444", "blank"};
	
	public static void main(String[] args) {
		LinkedList<String> fileNames = getListOfFiles();
		
		fileNames.forEach(name -> {
		//if(name.equalsIgnoreCase("AQC00914000.csv") || name.equalsIgnoreCase("AQW00061705.csv"))	
			loadElevations(name);
			
		});
		
		elevations.forEach((key, value) -> System.out.println(key + ": " + value.toString()));
	}
	
	public static void loadElevations(String name) {
		 System.out.println("Parsing file: " + name);
		
		 URL url = null;
	     URLConnection connection;
	     InputStreamReader fileListStream;
		 String csvSplitBy = "\",\"";
		 String[] headerArray;
	     
	     try {
				url = new URL(urlString + name);
		 } catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
		 }
	     
		 BufferedReader buffer = null;
		 String line = "";
		 String header = "";
		 LinkedList<String> headers = new LinkedList<>();

		 try {
		       	connection = url.openConnection();
		       	fileListStream = new InputStreamReader(connection.getInputStream());
		        	
		        buffer = new BufferedReader(fileListStream);
		        header = buffer.readLine();
		        
		        headerArray = header.split(csvSplitBy);
		        for(int i=0; i < headerArray.length; i++) {
		        	String item = headerArray[i].replace("\"", "");
		        	System.out.println(item);
		        	headers.add(item);
		        }
		        
		        int elevationIndex = headers.indexOf("ELEVATION");
		        int prcp25PctlIndex = headers.indexOf("DLY-PRCP-25PCTL");
		        int dateIndex = headers.indexOf("DATE");
		        
		        if((dateIndex == -1) || (elevationIndex == -1 && prcp25PctlIndex == -1) ) {
		        	return;
		        }
		        
		        while ((line = buffer.readLine()) != null) {
		        	String[] itemsArray = line.split(csvSplitBy);
		        	String dateStr = itemsArray[dateIndex];
		        	String elevtionStr = itemsArray[elevationIndex];
		        	String prcp25PctlStr = itemsArray[prcp25PctlIndex];
		        	
		        	if(Arrays.asList(blanks).contains(elevtionStr) && Arrays.asList(blanks).contains(prcp25PctlStr)) {
		        		continue;
		        	}
		        	
		        	double elevation = 0.0;
		        	double prcp25Pctl = 0.0;
		        	boolean isElev = true;
		        	boolean isPrcp25 = true;
		        	
		        	try {
		        		elevation = Double.parseDouble(elevtionStr);
		        	} catch (NumberFormatException ne) {
		        		isElev = false;
		        	}
		        	try {
		        		prcp25Pctl = Double.parseDouble(prcp25PctlStr);
		        	} catch (NumberFormatException ne) {
		        		isPrcp25 = false;
		        	}
		        	if(!isElev && !isPrcp25) {
		        		continue;
		        	}
		        	
		        	//arbitrary added 2022. The date we could take from file's xml..But no time for this
		        	String startDateStr = "10-31-2022";  //arbitrary added 2022
		        	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M-d-yyyy", Locale.ENGLISH);
		        	LocalDate startDate = LocalDate.parse(startDateStr, formatter);
		        	
		        	String endDateStr = "12-01-2022";
		        	LocalDate endDate = LocalDate.parse(endDateStr, formatter);
		        	
		        	LocalDate date = LocalDate.parse(dateStr + "-2022", formatter);
		        	
		        	if(startDate.isBefore(date) &&  endDate.isAfter(date)) {
		        		
		        		//System.out.println("The date is: " + date.toString());
		        	
			        	if(elevations.containsKey(dateStr)) {
			        		LinkedList elevList = elevations.get(dateStr);
			        		elevList.add(elevation);
			        	} else {
			        		LinkedList<Double> elevList = new LinkedList<>();
			        		elevList.add(elevation);
			        		elevations.put(dateStr, elevList);
			        	}
			        	
			        	if(daily25Prctl.containsKey(dateStr)) {
			        		LinkedList prctlList = daily25Prctl.get(dateStr);
			        		prctlList.add(elevation);
			        	} else {
			        		LinkedList<Double> prctlList = new LinkedList<>();
			        		prctlList.add(elevation);
			        		daily25Prctl.put(dateStr, prctlList);
			        	}
		        	}
		        	
		        }
		 }
		 catch (FileNotFoundException e) {
	            e.printStackTrace();
	     } catch (IOException e) {
	            e.printStackTrace();
	     } finally {
	         if (buffer != null) {
	            try {
	                    buffer.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	         }
	     }	 
				
	}
	
	
	public static LinkedList getListOfFiles() {
		// TODO Auto-generated method stub
	      URL url = null;
	      URLConnection connection;
	      InputStreamReader fileListStream;
	      LinkedList<String> fileNames = new LinkedList<String>();
	      
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
			
	    
	    BufferedReader buffer = null;
	    String line = "";

	        try {
	        	connection = url.openConnection();
	        	fileListStream = new InputStreamReader(connection.getInputStream());
	        	
	            buffer = new BufferedReader(fileListStream);
	            while ((line = buffer.readLine()) != null) {
	            	line = line.replace("&nbsp;", "");
	                //System.out.println(line);
	                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); 
	                DocumentBuilder builder;  
	                try {  
	                    builder = factory.newDocumentBuilder();  
	                    if(line.contains(".csv")) {
	                    	Document document = builder.parse(new InputSource(new StringReader(line)));  
	                    	//System.out.println(document);
	                    	NodeList nodes = document.getChildNodes();
	                    	for(int i=0; i < nodes.getLength(); i++) {
	                    		Node node = nodes.item(i);
	                    		if(node.getFirstChild().getTextContent().contains(".csv")) {
	                    			String fileName = node.getFirstChild().getTextContent();
	                    			System.out.println(fileName);
	                    			fileNames.add(fileName.toString());
	                    			break;
	                    		}
	                    	}
	                    }
	                } catch (Exception e) {  
	                    e.printStackTrace();  
	                } 
	                
	            }

	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            if (buffer != null) {
	                try {
	                    buffer.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	        }	         
	        
	        return fileNames;
	}

}
