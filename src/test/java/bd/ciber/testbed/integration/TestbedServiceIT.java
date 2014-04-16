package bd.ciber.testbed.integration;

import java.util.HashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import bd.ciber.testbed.TestbedController;
import bd.ciber.testbed.TestbedService;
import bd.ciber.testbed.db.DataProfile;
import bd.ciber.testbed.db.Settings;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring/service-context.xml" })
public class TestbedServiceIT {
	private static final Logger LOG = LoggerFactory.getLogger(TestbedServiceIT.class);
	
	private static TestbedService testbedService;
	
	@Autowired
	private TestbedController controller;

	@Test
	public void testAll() throws Exception {
		controller.dropAll();
		DataProfile dataProfile = new DataProfile();
		dataProfile.setName("integrationTest1");
		dataProfile.setThreads(3);
		HashMap<String, Integer> formatSpec = new HashMap<String, Integer>();
		formatSpec.put("pdf", Integer.valueOf(5));
		dataProfile.setFormatSpec(formatSpec);
		LOG.debug("about to store profile");
		String id = controller.postDataProfile(dataProfile);
		LOG.debug("stored data profile under key: {}", id);
		Settings s = new Settings();
		s.setCurrentDataProfile(id);
		LOG.debug("about to store settings");
		controller.updateSettings(s);
		LOG.debug("stored settings");
		testbedService.start();
		Thread.sleep(1000*60);
		testbedService.stop();
		Thread.sleep(1000*60*30);
	}

}
