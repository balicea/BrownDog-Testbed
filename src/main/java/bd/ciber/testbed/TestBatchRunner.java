package bd.ciber.testbed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import bd.ciber.testbed.db.DataProfile;
import bd.ciber.testbed.db.PolyglotTestResult;
import bd.ciber.testbed.db.TestBatchResult;

@Component
@Scope("prototype")
public class TestBatchRunner implements Callable<TestBatchResult>, ApplicationContextAware {
	private static final Logger LOG = LoggerFactory.getLogger(TestBatchRunner.class); 
	
	@Autowired
	private DataProvider dataProvider;
	
	@Autowired
	private TestbedController controller;
	
	private DataProfile profile;
	
	private ExecutorService executor;
	
	private int totalTests;

	private ApplicationContext applicationContext;

	public void init() {
	}
	
	public TestBatchRunner(DataProfile profile) {
		this.profile = profile;
	}
	
	public TestBatchResult call() throws Exception {
		TestBatchResult result = new TestBatchResult();
        result.setProfileId(profile.getId());
		this.executor = Executors.newFixedThreadPool(profile.getThreads());
		LOG.debug("format spec is {}", profile.getFormatSpec());
		Set<String> paths = dataProvider.select(profile.getFormatSpec());
		this.totalTests = paths.size();
		LOG.info("Starting test profile '{}' with {} files", profile.getName(), totalTests);
		final List<Future<PolyglotTestResult>> futures = new ArrayList<Future<PolyglotTestResult>>();
		result.setStartTime(System.currentTimeMillis());
		String id = controller.putTestBatchResult(result);
		result.setId(id);
        for (final String path : paths) {
        	PolyglotTestTask task = (PolyglotTestTask) this.applicationContext.getBean(
        			"polyglotTestTask", path);
            futures.add(executor.submit(task));
        }
        processResults(futures, id);
        result.setElapsedTime(System.currentTimeMillis() - result.getStartTime());
		return result;
	}
	
	public DataProvider getDataProvider() {
		return dataProvider;
	}

	public void setDataProvider(DataProvider dataProvider) {
		this.dataProvider = dataProvider;
	}

	public TestbedController getController() {
		return controller;
	}

	public void setController(TestbedController controller) {
		this.controller = controller;
	}

	private void processResults(final List<Future<PolyglotTestResult>> futures, String batchId) throws InterruptedException, ExecutionException, IOException {
        int count = 0;
        for (final Future<PolyglotTestResult> f : futures) {
            final PolyglotTestResult result = f.get();
            result.setBatchId(batchId);
            LOG.info(result.getInPath()+" ("+(++count)+"/"+totalTests+")");
            controller.postPolyglotTestResult(result);
        }
    }

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

}
