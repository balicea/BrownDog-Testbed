package bd.ciber.testbed;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestbedDaemon implements Daemon {
	private static final Logger LOG = LoggerFactory.getLogger(TestbedDaemon.class);

	private ClassPathXmlApplicationContext applicationContext;
	
	public synchronized void start() {
		LOG.info("Starting daemon");
		applicationContext.getBean(TestbedService.class).start();
	}

	@Override
	public void init(DaemonContext context) throws DaemonInitException,
			Exception {
		LOG.info("Initializing daemon");
		this.applicationContext = new ClassPathXmlApplicationContext("/spring/service-context.xml");
        this.applicationContext.registerShutdownHook();
		applicationContext.getBean(TestbedService.class).start();
	}

	@Override
	public void stop() throws Exception {
		LOG.info("Stopping daemon");
		applicationContext.getBean(TestbedService.class).stop();
	}

	@Override
	public void destroy() {
		LOG.info("Destroying daemon");
		this.applicationContext.destroy();
	}

}
