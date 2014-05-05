package bd.ciber.testbed;

import static org.mongojack.JacksonDBCollection.wrap;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.util.List;
import java.util.Map;

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

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.GroupCommand;

@Controller
public class TestbedController {
	private static final Logger LOG = LoggerFactory.getLogger(TestbedController.class);
	
	@Autowired
	private DB db;

	private JacksonDBCollection<DataProfile, String> getDataProfileColl() {
		return wrap(db.getCollection(DataProfile.class.getName()), DataProfile.class, String.class);
	}
	private JacksonDBCollection<Settings, String> getSettingsColl() {
		return wrap(db.getCollection(Settings.class.getName()), Settings.class, String.class);
	}
	private JacksonDBCollection<TestBatchResult, String> getTestBatchResultColl() {
		return wrap(db.getCollection(TestBatchResult.class.getName()), TestBatchResult.class, String.class);
	}
	private JacksonDBCollection<PolyglotTestResult, String> getPolyglotTestResultColl() {
		return wrap(db.getCollection(PolyglotTestResult.class.getName()), PolyglotTestResult.class, String.class);
	}

	@Autowired
	private TestbedService testbedService;

	@RequestMapping(value="/DataProfile", method=PUT)
	public @ResponseBody String postDataProfile(@RequestBody DataProfile profile) {
		return getDataProfileColl().insert(profile).getSavedId();
	}

	@RequestMapping(value="/DataProfile/{id}", method=GET)
	public @ResponseBody DataProfile getDataProfile(@PathVariable("id") String id) {
		return getDataProfileColl().findOneById(id);
	}
	
	@RequestMapping(value="/PolyglotTestResult", method=PUT)
	public @ResponseBody String postPolyglotTestResult(@RequestBody PolyglotTestResult polyglotTestResult) {
		return getPolyglotTestResultColl().insert(polyglotTestResult).getSavedId();
	}
	
	@RequestMapping(value="/PolyglotTestResult", method=GET)
	public @ResponseBody List<PolyglotTestResult> postPolyglotTestResult() {
		return getPolyglotTestResultColl().find().sort(new BasicDBObject("_id", -1)).limit(50).toArray();
	}

	@RequestMapping(value="/PolyglotTestResult/{id}", method=GET)
	public @ResponseBody PolyglotTestResult getPolyglotTestResult(@PathVariable("id") String id) {
		return getPolyglotTestResultColl().findOneById(id);
	}
	
	@RequestMapping(value="/PolyglotDistinctFormatsTested", method=GET)
	public @ResponseBody Map<String, Integer> getPolyglotDistinctFormatsTested() {
		DBObject match = new BasicDBObject("$match", new BasicDBObject("type", "airfare") );

		// build the $projection operation
		DBObject fields = new BasicDBObject("department", 1);
		fields.put("amount", 1);
		fields.put("_id", 0);
		DBObject project = new BasicDBObject("$project", fields );

		// Now the $group operation
		DBObject groupFields = new BasicDBObject( "_id", "$department");
		groupFields.put("average", new BasicDBObject( "$avg", "$amount"));
		DBObject group = new BasicDBObject("$group", groupFields);

		// run aggregation
		AggregationOutput output = getPolyglotTestResultColl()..aggregate( match, project, group );
		return getPolyglotTestResultColl().group(new GroupCommand(inputCollection, keys, condition, initial, reduce, finalize));
	}

	@RequestMapping(value="/TestBatchResult", method=PUT)
	public @ResponseBody String putTestBatchResult(@RequestBody TestBatchResult testBatchResult) {
		return getTestBatchResultColl().insert(testBatchResult).getSavedId();
	}
	
	@RequestMapping(value="/TestBatchResult/{id}", method=POST)
	public void postTestBatchResult(@PathVariable("id") String id, @RequestBody TestBatchResult testBatchResult) {
		getTestBatchResultColl().updateById(id, testBatchResult);
	}

	@RequestMapping(value="/TestBatchResult/{id}", method=GET)
	public @ResponseBody TestBatchResult getTestBatchResult(@PathVariable("id") String id) {
		return getTestBatchResultColl().findOneById(id);
	}

	@RequestMapping(value="/Settings", method=GET)
	public @ResponseBody Settings getSettings() {
		return getSettingsColl().findOne();
	}
	
	@RequestMapping(value="/Settings", method=POST)
	public void updateSettings(@RequestBody Settings settings) {
		Settings existing = getSettingsColl().findOne();
		LOG.debug("found settings {}", existing);
		if(existing != null) {
			getSettingsColl().updateById(existing.getId(), settings);
		} else {
			getSettingsColl().insert(settings);
		}
	}
	
	@RequestMapping(value="/Service", method=GET)
	public @ResponseBody String updateSettings(@RequestParam("action") String action) throws Exception {
		LOG.info("Service action requested: {}", action);
		if("start".equals(action)) {
			this.testbedService.start();
			return "starting";
		} else if("stop".equals(action)) {
			this.testbedService.stop();
			return "stopping";
		}
		throw new Exception("unrecognized action");
	}

	public void dropAll() {
		getDataProfileColl().drop();
		getSettingsColl().drop();
		getTestBatchResultColl().drop();
		getPolyglotTestResultColl().drop();
	}
}
