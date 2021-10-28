package com.example.testapplication;

import java.util.Date;
import java.util.Random;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
//import com.espertech.esper.client.*;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.espertech.esper.client.soda.EPStatementObjectModel;
import com.espertech.esper.client.util.EventRenderer;
import com.espertech.esper.core.service.ConfiguratorContext;
import com.espertech.esper.core.service.EPServiceProviderImpl;
import com.espertech.esper.core.service.EPServiceProviderSPI;
import com.espertech.esper.util.JavaClassHelper;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * Hello world!
 *
 */
public class EsperTemperature {
    public static class Temperature {
        String symbol;
        Double price;
        Date timeStamp;

        public Temperature(String s, double p, long t) {
            symbol = s;
            price = p;
            timeStamp = new Date(t);
        }
        public double getPrice() {return price;}
        public String getSymbol() {return symbol;}
        public Date getTimeStamp() {return timeStamp;}

        @Override
        public String toString() {
            return "Temperature: " + price.toString() + ", Time: " + timeStamp.toString();
        }
    }

    private static Random generator=new Random();

    public static void GenerateRandomTick(EPRuntime cepRT){
        double price = (double) generator.nextInt(60);
        long timeStamp = System.currentTimeMillis();
        String symbol = "AAPL";
        Temperature tick= new Temperature(symbol,price,timeStamp);
        System.out.println("Sending temperature: " + tick);
        cepRT.sendEvent(tick);
    }

    public static class CEPListener implements UpdateListener {
        public void update(EventBean[] newData, EventBean[] oldData) {
            //String pricere = (double) newData[0].get("price");
            //int age = (int) newData[0].get("age");
            //System.out.println(String.format("Name: %s, Age: %d", name, age));
            System.out.println("Event received: "
                    + newData[0].getUnderlying());
            SendNotification.sendDeviceNotification();  //na steilw thermokrasia
        }
    }

    public static void checkTemperatureEvents() {
        //The Configuration is meant only as an initialization-time object.
        //Configuration cepConfig = new Configuration();
        // We register Ticks as objects the engine will have to handle
        //ei- cepConfig.addEventType("TemperatureEvent",Temperature.class.getName());
        // EPServiceProvider epServiceProvider = EPServiceProviderManager.getDefaultProvider();
        // epServiceProvider.getEPAdministrator().getConfiguration().addEventType("StartEvent", Tick.class);
        try {
            // We setup the engine
            Configuration config = new Configuration();
            EPServiceProvider cep = EPServiceProviderManager.getDefaultProvider(config);
            //cep.initialize();
            cep.getEPAdministrator().getConfiguration().addEventType("TemperatureEvent", Temperature.class);
            EPRuntime cepRT = cep.getEPRuntime();

            // We register an EPL statement (Query)
            //EPAdministrator cepAdm = cep.getEPAdministrator();
            EPStatement cepStatement = cep.getEPAdministrator().createEPL("select * from TemperatureEvent " +
                    "where price > 40.0");

            //Attach a listener to the statement
            cepStatement.addListener(new CEPListener());

            for (int i = 0; i < 10; i++) {
                GenerateRandomTick(cepRT);
            }
            //GenerateRandomTick(cepRT);
            //cep.destroy();
            //System.out.println( "Hello World!" );
        }catch(Exception e){
            e.printStackTrace();
        }
    }


}
