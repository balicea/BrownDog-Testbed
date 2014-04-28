package bd.ciber.testbed;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import bd.ciber.testbed.db.PolyglotTestResult;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PolyglotTestTask implements Callable<PolyglotTestResult> {
	private static final Logger LOG = LoggerFactory.getLogger(PolyglotTestTask.class);
	String path;
	
	@Autowired
	PolyglotClient polyglotClient;
	
	@Autowired
	DataProvider dataProvider;

	public PolyglotTestTask(String path) {
		this.path = path;
	}
	
	public PolyglotTestTask(DataProvider dataProvider, PolyglotClient polyglotClient, String path) {
		this(path);
		this.polyglotClient = polyglotClient;
		this.dataProvider = dataProvider;
	}

	public PolyglotTestResult call() {
		LOG.info("Starting test: {}", this.path);
		PolyglotTestResult result = new PolyglotTestResult();
		result.setInPath(path);
		result.setInProviderId(this.dataProvider.id());
    	String extension = FilenameUtils.getExtension(path);
		result.setInFormat(extension);
		
		// pick a conversion
    	List<String> formats = polyglotClient.getConversionsForInput(extension);
    	if(formats == null || formats.size() < 1) {
    		result.setReturnCode(PolyglotTestResult.NO_CONVERSIONS);
    		return result;
    	}
    	Collections.shuffle(formats);
    	result.setOutFormat(formats.get(0));
    	
    	result.setSubmitted(System.currentTimeMillis());
    	try(InputStream in = dataProvider.stream(path)) {
			String converted = polyglotClient.convert(path, in, result.getOutFormat());
	    	result.setElapsed(System.currentTimeMillis() - result.getSubmitted());
	    	result.setOutSize(new File(converted).length());
	    	result.setReturnCode(PolyglotTestResult.SUCCESS);
	    	return result;
		} catch (IOException e) {
			result.setReturnCode(PolyglotTestResult.INPUT_DATA_ERROR);
			result.setReturnMessage(e.getLocalizedMessage());
			return result;
		} catch(Exception e) {
			result.setReturnCode(PolyglotTestResult.UNKNOWN_FAILURE);
			result.setReturnMessage(e.getLocalizedMessage());
			return result;
		}
	}

}
