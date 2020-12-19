package autoclave;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.Queue;
import java.util.Scanner;
import java.util.LinkedList;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import java.util.Timer;


import com.pi4j.io.gpio.GpioPinDigitalInput;

import com.pi4j.io.gpio.PinPullResistance;

import com.pi4j.io.gpio.trigger.GpioCallbackTrigger;
import com.pi4j.io.gpio.trigger.GpioPulseStateTrigger;
import com.pi4j.io.gpio.trigger.GpioSetStateTrigger;
import com.pi4j.io.gpio.trigger.GpioSyncStateTrigger;

 
public class MAX31855_Thermocouple{
	public static SpiDevice spi = null;

	public static void main(String[] args) throws InterruptedException, IOException {
    	
		System.out.println("Starting Thermocouple Application.");
        
		spi = SpiFactory.getInstance(SpiChannel.CS0,SpiDevice.DEFAULT_SPI_SPEED,SpiDevice.DEFAULT_SPI_MODE);
        
		Queue<Float> couplerQ = new LinkedList<Float>();
        
        GpioController gpio = GpioFactory.getInstance();
        final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, "MyLED", PinState.LOW);
        pin.setShutdownOptions(true, PinState.LOW);
        
        float sum = 0;
        float average = 0;
        int targetTemp = 0;
        int runtime = 0;
        int maxmin = 0;
        int dwell = 0;
        
        
        String[] config = new String[4];
        
        
        String lstconfig[] = new String[4];
        Scanner lastconfigcsv = new Scanner(new File("/home/pi/Desktop/config.csv"));
       
        
        for(int k = 0; lastconfigcsv.hasNext(); k++) {
        	
        	lstconfig[k] = lastconfigcsv.next();
        	
        }
		lastconfigcsv.close();
		
        System.out.println("Your existing settings: ");
        System.out.println(lstconfig[0] + ": target temp in celcius");
        System.out.println(lstconfig[1] + ": hold time in min.");
        System.out.println(lstconfig[2] + ": max heat up time in min.");
        System.out.println(lstconfig[3] + ": dwell time in seconds.");
        
        Scanner target = new Scanner(System.in);
        String choice = "n";
        System.out.println("would you like to keep these settings? y or n");
        
        if(target.hasNext())
        	choice = target.next();
        
        if(choice.equals("y")) {
        	targetTemp =  Integer.parseInt(lstconfig[0]);
        	runtime = Integer.parseInt(lstconfig[1]);
        	maxmin = Integer.parseInt(lstconfig[2]);
        	dwell = Integer.parseInt(lstconfig[3]);
        }
        	
        if(choice.equals("n")) {
        
        
        
        
        System.out.println(getConversionValue() + "C: is your current temp. (IN CELCIUS)");
        
        
        
        
       System.out.println("What would you like to be your target temp? (IN CELCIUS)");
       if(target.hasNextInt()) 
        	targetTemp = target.nextInt(); 
       
       String targettempstring = Integer.toString(targetTemp);
       config[0] = targettempstring;
       
        System.out.println("How long would you like to hold temp? (IN MINUTES)");
        if(target.hasNextInt()) 
        	runtime = target.nextInt();
        
        String runtimestring = Integer.toString(runtime);
        config[1] = runtimestring;
        
        System.out.println("How long would you like to allow for warm up time? (IN MINUTES)");
        if(target.hasNextInt())
        	maxmin = target.nextInt();
        
        String maxminstring = Integer.toString(maxmin);
        config[2] = maxminstring;
        
        System.out.println("How long would you like your dwel to be if you don't know 2 seconds is a good starting point? (IN SECONDS)");
        if(target.hasNextInt())
        	dwell = target.nextInt();
        
       target.close();
        
        String dwel = Integer.toString(dwell);
        config[3] = dwel;
        
       
//this portion of code stores the user config to a csv file        
        FileWriter configWriter = new FileWriter("config.csv");
        for(int i = 0; i < 4; i++) {
        	configWriter.append(config[i]); // print list to queue
	    	configWriter.append("\n");
        }
        System.out.println("Saved your settings to the config file");
        configWriter.close();
        }
       
        
        System.out.println("Your target temp: " + targetTemp + "C");
        System.out.println("Your temp maintain time: " + runtime + " Min");
        System.out.println("Your maximump allowed warmupt time: " + maxmin + " Min");
        System.out.println("Your current temp: " + getConversionValue() + "C");
        System.out.println("Your dwell: " + dwell + " Sec");
        //code to calc time it takes to get the sensor value so we can remove this from our run time later
        
        maxmin = maxmin*60000; // convert min to ms 
        
        //heat up sequence
        final long startwarmuptimer = System.currentTimeMillis();        
        long elapsedwarmup = 0; 		
        System.out.println("warming up......");
        while((getConversionValue() < targetTemp) && (elapsedwarmup < maxmin)) {
        	pin.high();
        	elapsedwarmup = System.currentTimeMillis() - startwarmuptimer;
        }
        
        if(elapsedwarmup >= maxmin) {
        	System.out.println("maximum allowed warmup time exceded.");
        	System.exit(-1);
        }
        
        
        long runtimems = runtime*60000; //user input in min conversion to ms for conversion 1 min = 60,000 ms
        long timeelapsed = 0;
        System.out.println(timeelapsed + "time elapsed ms");
        
        
        while(timeelapsed < runtimems) {
        	long start0 = System.currentTimeMillis();
        	//System.out.println((runtime - timeelapsed) + "runtime remaining in ms");
        	for (int j=0; j < 100; j++) {
	        	sum += getConversionValue();
	        	//System.out.println(j + ", " + sum + ", " + getConversionValue());
	        	Thread.sleep(1);
	        }
	       
	       // System.out.println(sum + "is the sum");
	        
	        average = (sum/100);
	        sum = 0;
	        couplerQ.add(average); // for every iteration this adds these 100 ms average of 100 readings for graphing etc later. 
	        						// could also write to a file for other analysis
	        
	       /* System.out.println(average + ": is average.");
	        System.out.println("Target: " + targetTemp);
	        System.out.println("Current: " + getConversionValue());
	        System.out.println(getConversionValue());
	    	*/
	        final long start = System.currentTimeMillis();
	    	long end = start + (dwell * 1000);
	    	
	    	if(getConversionValue() < targetTemp) {
	    		System.out.println("on");
	    		System.out.println((timeelapsed/60000) + " min: elapsed time. " + getConversionValue() + "C: Current temp.");
	    		while(System.currentTimeMillis() < end) {
	    			pin.high();
	    		}	
	    	}
	    	
	    	if(getConversionValue() >= targetTemp) {
	    		System.out.println("off");
	    		System.out.println((timeelapsed/60000) + " min: elapsed time. " + getConversionValue() + "C: Current temp.");
	    		while(System.currentTimeMillis() < end){
	    			pin.low();
	    		}
	    	}
	    	long end0 = System.currentTimeMillis();
	        long interationtime = end0 - start0;
	        timeelapsed = timeelapsed + interationtime;
	} 
        FileWriter csvWriter = new FileWriter("log.csv");
        while (couplerQ.size() > 0) {
        	System.out.println("print to queue: " + couplerQ.peek());
        	String append = Float.toString(couplerQ.peek());
        	csvWriter.append(append); // print list to queue
        	csvWriter.append("\n");
        	couplerQ.remove();
        	
        }
        csvWriter.close();
}
      

    
public static double getConversionValue() throws IOException {

        byte data[] = new byte[] {0,0, 0, 0};// Dummy payloads. It's not responsible for anything.
       
        byte[] result = spi.write(data); //Request data from MAX31855 via SPI with dummy pay-load
        
        if((result[0] & 128)==0 && (result[1] & 1)==1 ) {//Sign bit is 0 and D16 is high corresponds to Thermocouple not connected.
            System.out.println("Thermocouple is not connected");
            return 0;
        }
        String stringResult=String.format("%32s",Integer.toString(ByteBuffer.wrap(result).getInt(), 2)).replace(' ', '0');
        double valInt=0.0;
        
        if(stringResult.charAt(0)=='1' ){  //Checking for signed bit. If need to convert to 2's Complement.
         	StringBuilder onesComplementBuilder = new StringBuilder();
         	
        	for(char bit : stringResult.substring(0, 12).toCharArray()) {
        	    onesComplementBuilder.append((bit == '0') ? 1 : 0);  // if bit is '0', append a 1. if bit is '1', append a 0.
        	}
        	String onesComplement = onesComplementBuilder.toString();
        	valInt = -1*( Integer.valueOf(onesComplement, 2) + 1); // two's complement = one's complement + 1. This is the positive value of our original binary string, so make it negative again.
        	
        }else{
        	valInt=Integer.parseInt(stringResult.substring(0, 12),2); //+ve no convert to double value
        }
        
        if(stringResult.charAt(12)=='1') //Check for D18 and D19 for fractional values
        	valInt+=0.5;	
        if(stringResult.charAt(13)=='1')
        	valInt+=0.25;
        
        return valInt;
    }
}

	
