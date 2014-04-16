package bd.ciber.testbed;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.JargonProperties;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.IRODSGenQueryBuilder;
import org.irods.jargon.core.query.IRODSGenQueryFromBuilder;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSet;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IrodsDataProvider implements DataProvider {
	private static final Logger LOG = LoggerFactory.getLogger(IrodsDataProvider.class);
	
	private IRODSFileSystem irodsFileSystem;
	
	@Autowired
	private IRODSAccount irodsAccount;
	
	@Autowired
	private String basePath;

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}
	
	@PostConstruct
	public void init() {
		try {
			irodsFileSystem = IRODSFileSystem.instance();
            JargonProperties origProps = irodsFileSystem.getIrodsSession().getJargonProperties();
            SettableJargonProperties overrideJargonProperties = new SettableJargonProperties(origProps);
            //overrideJargonProperties.setIrodsSocketTimeout(irodsSocketTimeout); // was 300
            //overrideJargonProperties.setIrodsParallelSocketTimeout(irodsSocketTimeout); // was 300
            irodsFileSystem.getIrodsSession().setJargonProperties(overrideJargonProperties);
			LOG.debug("Trying irods connection");
			LOG.debug("account: {}", irodsAccount);
			irodsFileSystem.getIrodsSession().currentConnection(irodsAccount);
			LOG.debug("Got irods connection");
		} catch (JargonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see bd.ciber.testbed.DataSelector#select(java.util.Map)
	 */
	@Override
	public Set<String> select(Map<String, Integer> formatSpec) throws IOException {
		Set<String> result = new HashSet<String>();
		for(Entry<String, Integer> spec : formatSpec.entrySet()) {
			queryForSpec(spec.getKey(), spec.getValue().intValue(), result);
		}
		return result;
	}
	
	private void queryForSpec(String extension, int limit, Set<String> result) {
		LOG.debug("querying for extension {} with limit {}", extension, limit);
		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		IRODSQueryResultSet resultSet;
		try {
			builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_DATA_NAME)
				.addConditionAsGenQueryField(
						RodsGenQueryEnum.COL_COLL_NAME,
						QueryConditionOperators.LIKE,
						basePath+"%")
				.addConditionAsGenQueryField(
						RodsGenQueryEnum.COL_DATA_NAME,
						QueryConditionOperators.LIKE,
						"%."+extension);
			IRODSGenQueryFromBuilder irodsQuery = builder.exportIRODSQueryFromBuilder(limit);
			IRODSGenQueryExecutor qExecutor = 
					irodsFileSystem.getIRODSAccessObjectFactory()
						.getIRODSGenQueryExecutor(irodsAccount);
			resultSet = qExecutor.executeIRODSQuery(irodsQuery, 0);
			for(IRODSQueryResultRow row : resultSet.getResults()) {
				String col = row.getColumn(RodsGenQueryEnum.COL_COLL_NAME.getName());
				String data = row.getColumn(RodsGenQueryEnum.COL_DATA_NAME.getName());
				LOG.debug("got data: {}", data);
				result.add(col+"/"+data);
			}
			qExecutor.closeResults(resultSet);
		} catch(JargonException e) {
			e.printStackTrace(); //FIXME
		} catch (GenQueryBuilderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JargonQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public InputStream stream(String path) throws IOException {
		try {
			IRODSFileFactory iff = irodsFileSystem.getIRODSAccessObjectFactory()
					.getIRODSFileFactory(irodsAccount);
			return iff.instanceSessionClosingIRODSFileInputStream(path);
		} catch(JargonException e) {
			throw new IOException(e);
		}
	}

	@Override
	public String id() {
		try {
			return irodsAccount.toURI(false).toString();
		} catch (JargonException e) {
			throw new Error(e);
		}
	}
	
}
