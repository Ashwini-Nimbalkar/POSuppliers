package com.tru.POSupplier;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;

public class MainApp {

	public static void main(String args[]) {
		CamelContext ctext = new DefaultCamelContext();

		FileTransferRoute fl = new FileTransferRoute();
		//OutgoingRouteHKG othkg = new OutgoingRouteHKG();

		try {
			ctext.addRoutes(fl);
			ctext.start();
			Thread.sleep(500000);
			ctext.stop();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
