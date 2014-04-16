package bd.ciber.testbed;

import java.util.Collections;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan
@EnableAutoConfiguration
public class Testbed implements Daemon {
	private static final Logger LOG = LoggerFactory.getLogger(Testbed.class);

	private ConfigurableApplicationContext applicationContext;
	
	public synchronized void start() {
		LOG.info("Starting daemon");
		applicationContext.getBean(TestbedService.class).start();
	}

	@Override
	public void init(DaemonContext context) throws DaemonInitException,
			Exception {
		LOG.info("Initializing daemon");
		SpringApplication app = new SpringApplication(Testbed.class);
		app.setSources(Collections.singleton((Object)"classpath:/spring/service-context.xml"));
        this.applicationContext = app.run(context.getArguments());
        this.applicationContext.registerShutdownHook();
	}

	@Override
	public void stop() throws Exception {
		LOG.info("Stopping daemon");
		applicationContext.getBean(TestbedService.class).stop();
	}

	@Override
	public void destroy() {
		LOG.info("Destroying daemon");
		SpringApplication.exit(this.applicationContext, new ExitCodeGenerator(){
			@Override
			public int getExitCode() {
				return 0;
			}});
	}

}
