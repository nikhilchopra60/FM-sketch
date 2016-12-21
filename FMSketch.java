package ProbablisticCounting;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.zip.CRC32;

import org.jfree.ui.RefineryUtilities;

//import javax.swing.SwingUtilities;
//import jfreechart.XYLineChartExample;

public class FMSketch {
	
	final static String inputFile = "C:\\sem 1\\ITM\\Project\\ft.txt";
	//String outputFile = "C:\\sem 1\\ITM\\Project\\Results.txt";
	Double fractionOfZeroes= 0.0;
	String line = "";
	
	String sourceIP = "";
	String destIP = "";
			
	NavigableMap<String, HashSet<String>> flowList = new TreeMap<String, HashSet<String>>();
	Map<String,int[]> virtual = new HashMap<String,int[]>();
	int flowNum = 0;
	final static int m = 150;
	static Integer[][] FMSketch = new Integer[m][32];
	static final double phi = 0.77351;
	static double sumZS = 0;
	static double result = 0;
	static int flag = 0;

	private static HashMap<Integer,Integer> resultGraph = new HashMap<Integer,Integer>();
	
	
	FMSketch(){
		
		try{
		
				/* Initialize bitmap to zero*/			
				initialize_FM_Sketch();
				
				/*Build hashmap for the flow*/
				buildFlowHashMap();
				
				/*Build bitmap from flow hashmap */
				buildFMSketch();
				
				
			
		}
		catch(Exception e){
				System.out.println("Exception occured : "+e);
				e.printStackTrace();
		}
		
		
		
	}
	
public void buildFlowHashMap() throws IOException{
		
		int start = 1;
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		
		while((line = br.readLine()) != null) {
		 
			/* Split each line of the file on " "	*/
			String[] columns = line.split(" ");
		 
			/* Assign first column of the file to source IP */
			sourceIP = columns[0];
		 
			/* Assign Destination IP */
			for(int k=1; k<columns.length;k++){				 
				if(!(columns[k].equalsIgnoreCase(""))){
					destIP = columns[k];
					break;
				}
			}
			
			if(start ==1){
				HashSet<String> newFlow = new HashSet<String>();
				newFlow.add(destIP);
				flowList.put(sourceIP, newFlow);
				start = 0;
				}
				
				else if(flowList.lastKey().equals(sourceIP)){
					flowList.get(sourceIP).add(destIP);
					
				}
				
				else{
					
					HashSet<String> newFlow = new HashSet<String>();
					newFlow.add(destIP);
					flowList.put(sourceIP, newFlow);
				}
				
		
	}
		
		br.close();
	}
	
public void buildFMSketch() {
	
	int actualCardinality = 0; 
	int estimatedCardinality = 0;
	for (Entry<String, HashSet<String>> flow : flowList.entrySet()) {
		sourceIP = flow.getKey();
		HashSet destIP = flow.getValue();
		actualCardinality = destIP.size();

		// Processing for each flow
		int i = 0;
		
		while (i < actualCardinality) {
			try {
				onlineFMS(destIP);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			i++;
		}
				
		estimatedCardinality = offlineFMS();
		//if(size>=1 && size<=10){
			//new PC();
		//}
		if (actualCardinality>500) System.out.println("actualCardinality = "+actualCardinality + " estimatedCardinality = " + estimatedCardinality);
		resultGraph.put(actualCardinality, estimatedCardinality);
		initialize_FM_Sketch();
							
				}
	}	
	
	private int offlineFMS() {
		int countZ = 0;
		for (int i = 0; i < m; i++) {

			for (int j = FMSketch[i].length - 1; j > 0; j--) {
				if (FMSketch[i][j] != 0)		//Counting consecutive 1's
					countZ++;
				else {
					break;
				}
			}
			sumZS += countZ;
			countZ = 0;
		}
		double sumzsM = sumZS / m;				// Taking average of sum of consecutive 1's
		result = m * (Math.pow(2, sumzsM)) / phi;
		int resultInt = (int) (result);
		
		return resultInt;

	}
	
	private  void onlineFMS(HashSet<String> destIP) throws UnknownHostException {

		flag = 0;
		
		for (String it : destIP) {
			int rowIndex = Hash1(it);
			int colIndex = Hash2(it);
			System.out.println(FMSketch[rowIndex].length - 1);
			System.out.println(colIndex);
			colIndex = (FMSketch[rowIndex].length - 1) - colIndex;
			FMSketch[rowIndex][colIndex] = 1;

		}
		
	}

	private void initialize_FM_Sketch() {
		for (int i = 0; i < m; i++)
			for (int j = 0; j < 32; j++)
				FMSketch[i][j] = 0;

		sumZS = 0;
		result = 0;

	}
	
	private static int Hash1(String it) {

		long crc32 = 0;

		CRC32 crc = new CRC32();
		crc.update(it.getBytes());

		crc32 = crc.getValue();

		return (int) (crc32 % m);
	}

	private static int Hash2(String address) throws UnknownHostException {

		int zeroCount = 0, check = 1;
		Boolean foundOne = false;
		
		/*Counting consecutive zeroes from LSB in each destIP*/
		
		while (!foundOne) {

			if ((address.hashCode() & check) == 0) {
				check = check << 1;
				zeroCount++;
			} else
				foundOne = true;
		}
		return zeroCount;
	}
	

	public static void main(String[] args) throws IOException {
		FMSketch fms = new FMSketch();
		
		/* Chart Plotting*/
		ScatterPlot chart = new ScatterPlot("ITM Project 3", "FM Sketch", resultGraph);
	    chart.pack( );          
	    RefineryUtilities.centerFrameOnScreen( chart );          
	    chart.setVisible( true ); 

	}
}
	

