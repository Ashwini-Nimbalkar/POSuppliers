package com.tru.POSupplier;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spi.PropertiesComponent;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.camel.dataformat.zipfile.ZipSplitter;

public class FileTransferRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {

		InputStream in = getClass().getClassLoader().getResourceAsStream("application.properties");

		Properties props = new Properties();

		props.load(in);
		PropertiesComponent prc = getContext().getPropertiesComponent();
		prc.setInitialProperties(props);
		getContext().setPropertiesComponent(prc);

		String Url = prc.loadProperties().getProperty("Url.hkg");
		String DriverClassName = prc.loadProperties().getProperty("DriverClassName");
		String Username = prc.loadProperties().getProperty("Username.hkg");
		String Password = prc.loadProperties().getProperty("Password.hkg");

		DataSource dataSource = setupDataSource(Url, DriverClassName, Username, Password);

		// SimpleRegistry reg = new SimpleRegistry() ;
		// reg.bind("myds",dataSource);

		getContext().getRegistry().bind("mydata", dataSource);

		onException(Exception.class).continued(true)
				.log(LoggingLevel.ERROR, "An Error processing the ${header.CamelFileName} File")
				.setHeader("FileName", simple("${file:name}")).setHeader("RunDate", simple("${date:now:yyyyMMdd}"))
				.setHeader("RjctReason", constant("Error while transferring the file or No Files present"))
				//.to("sql:insert into INVADJHDR(FILENAME,TIMESENT,ESBSNTSTS,PROCESSTS,PROCESRMK) values(:#filename,:#reportTime,'S','S','Sent from ESB successfully')?dataSource=#mydata")
				.log("${header.RjctReason} in ${file:name} file").stop();

		//from("{{hkgIncoming.path}}").routeId("POSupplier-Route")
		 from("file:C://in?delete=true").routeId("HkgIn-Route") 
		 .setHeader("filename", simple("${file:name}"))		
		 .multicast().split(new ZipSplitter()).streaming()
		 .convertBodyTo(String.class).to("file:C://out")
		  //.to("{{as400hkg.path}}", "{{hkgOutgoing.path}}")
		.log("file sending from ftp")
		.setHeader("reportTime", simple("${date:now:yyyy-MM-dd HH:mm:ss}"))
		.to("sql:insert into ISPLIBCIN.EFPTRIGTPR(FILENAME,TIMESENT,ESBSNTSTS,PROCESSTS,PROCESRMK) values(:#filename,:#reportTime,'S','S','Sent from ESB successfully')?dataSource=#mydata");

		/*
		 * from("file:C://in?delete=true").routeId("HkgIn-Route") .to("file:C://out")
		 */

		/*
		 * from("sftp://10.43.243.180/../../opt/upload/hkg/in?" +
		 * "username=ashwini&password=Js2F0djh2&delete=true").routeId(
		 * "HkgIncoming-Route") .log("file sending") .to(
		 * "sftp://10.43.243.180/../../opt/upload/hkg/out?username=ashwini&password=Js2F0djh2"
		 * ).log("done");
		 */

	}

	private DataSource setupDataSource(String Url, String DriverClassName, String Username, String Password)
			throws SQLException {
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName(DriverClassName);
		ds.setUsername(Username);
		ds.setPassword(Password);
		ds.setUrl(Url);
		return ds;
	}
}
