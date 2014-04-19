package bd.ciber.testbed;

import static org.mongojack.JacksonDBCollection.wrap;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import javax.annotation.PostConstruct;

import org.mongojack.JacksonDBCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import bd.ciber.testbed.db.DataProfile;
import bd.ciber.testbed.db.PolyglotTestResult;
import bd.ciber.testbed.db.Settings;
import bd.ciber.testbed.db.TestBatchResult;

import com.mongodb.DB;

@Controller
public class TestbedController {
	private static final Logger LOG = LoggerFactory.getLogger(TestbedController.class);
	
	@Autowired
	private DB db;

	private JacksonDBCollection<DataProfile, String> dataProfileColl;
	private JacksonDBCollection<Settings, String> settingsColl;
	private JacksonDBCollection<TestBatchResult, String> testBatchResultColl;
	private JacksonDBCollection<PolyglotTestResult, String> polyglotTestResultColl;

	@Autowired
	private TestbedService testbedService;

	@PostConstruct
	public void init() {
		dataProfileColl = wrap(db.getCollection(DataProfile.class.getName()), DataProfile.class, String.class);
		settingsColl = wrap(db.getCollection(Settings.class.getName()), Settings.class, String.class);
		testBatchResultColl = wrap(db.getCollection(TestBatchResult.class.getName()), TestBatchResult.class, String.class);
		polyglotTestResultColl = wrap(db.getCollection(PolyglotTestResult.class.getName()), PolyglotTestResult.class, String.class);
	}

	@RequestMapping(value="/DataProfile", method=PUT)
	public @ResponseBody String postDataProfile(@RequestBody DataProfile profile) {
		return dataProfileColl.insert(profile).getSavedId();
	}

	@RequestMapping(value="/DataProfile", method=GET)
	public @ResponseBody DataProfile getDataProfile(@RequestParam("id") String id) {
		return dataProfileColl.findOneById(id);
	}
	
	@RequestMapping(value="/PolyglotTestResult", method=PUT)
	public @ResponseBody String postPolyglotTestResult(@RequestBody PolyglotTestResult polyglotTestResult) {
		return polyglotTestResultColl.insert(polyglotTestResult).getSavedId();
	}

	@RequestMapping(value="/PolyglotTestResult", method=GET)
	public @ResponseBody PolyglotTestResult getPolyglotTestResult(@RequestParam("id") String id) {
		return polyglotTestResultColl.findOneById(id);
	}

	@RequestMapping(value="/TestBatchResult", method=PUT)
	public @ResponseBody String putTestBatchResult(@RequestBody TestBatchResult testBatchResult) {
		return testBatchResultColl.insert(testBatchResult).getSavedId();
	}
	
	@RequestMapping(value="/TestBatchResult/{id}", method=POST)
	public void postTestBatchResult(@PathVariable("id") String id, @RequestBody TestBatchResult testBatchResult) {
		testBatchResultColl.updateById(id, testBatchResult);
	}

	@RequestMapping(value="/TestBatchResult/{id}", method=GET)
	public @ResponseBody TestBatchResult getTestBatchResult(@PathVariable("id") String id) {
		return testBatchResultColl.findOneById(id);
	}

	@RequestMapping(value="/Settings", method=GET)
	public @ResponseBody Settings getSettings() {
		return settingsColl.findOne();
	}
	
	@RequestMapping(value="/Settings", method=POST)
	public void updateSettings(@RequestBody Settings settings) {
		Settings existing = settingsColl.findOne();
		LOG.debug("found settings {}", existing);
		if(existing != null) {
			settingsColl.updateById(existing.getId(), settings);
		} else {
			settingsColl.insert(settings);
		}
	}

	public void dropAll() {
		dataProfileColl.drop();
		settingsColl.drop();
		testBatchResultColl.drop();
		polyglotTestResultColl.drop();
	}
}
