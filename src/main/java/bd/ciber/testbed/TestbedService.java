package bd.ciber.testbed;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import bd.ciber.testbed.db.DataProfile;
import bd.ciber.testbed.db.Settings;
import bd.ciber.testbed.db.TestBatchResult;

@Component
public class TestbedService implements ApplicationContextAware {
	private static final Logger LOG = LoggerFactory.getLogger(TestbedService.class);

	private Timer timer = null;
	
	@Autowired
	private TestbedController controller;

	private ApplicationContext applicationContext;
	
	public synchronized void start() {
		if(timer != null) {
			throw new Error("Testbed service already started");
		}
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				Settings settings = controller.getSettings();
				LOG.debug("got settings {}", settings);
				String profileKey = settings.getCurrentDataProfile();
				LOG.debug("got profile key {}", profileKey);
				DataProfile profile = controller.getDataProfile(profileKey);
				LOG.debug("got profile {}", profile);
				TestBatchRunner runner = (TestBatchRunner) applicationContext.getBean(
						"testBatchRunner", profile);
				try {
					TestBatchResult result = runner.call();
					controller.postTestBatchResult(result.getId(), result);
				} catch(Exception e) {
					LOG.error("Test Batch failed", e);
				}
			}
		}, 1000, 1000*60);
	}

	public void stop() throws Exception {
		timer.cancel();
		timer.purge();
		timer = null;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

}
