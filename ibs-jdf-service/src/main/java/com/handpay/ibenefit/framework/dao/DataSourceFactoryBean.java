package com.handpay.ibenefit.framework.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class DataSourceFactoryBean implements FactoryBean, ApplicationContextAware {

	private ApplicationContext applicationContext;

	private DataSource dataSource;

	public DataSource getObject() throws Exception {

		String[] names = applicationContext.getBeanNamesForType(DataSource.class);
		List<String> datasourceList = new ArrayList<String>();
		for (String name : names) {
			if (!name.equals("dataSource")) {
				datasourceList.add(name);
			}
		}
		if (datasourceList.size() == 1) {
			return (DataSource) applicationContext.getBean("masterDataSource");
		} else if (datasourceList.size() > 1) {
			dataSource = new DynamicDataSource();
			DynamicDataSource routingDataSource = (DynamicDataSource) dataSource;
			Map<Object, Object> targetDataSourcesMap = new HashMap<Object, Object>();
			for (String name : datasourceList) {
				targetDataSourcesMap.put(name, applicationContext.getBean(name));
			}
			routingDataSource.setTargetDataSources(targetDataSourcesMap);
			routingDataSource.setDefaultTargetDataSource(applicationContext.getBean("masterDataSource"));
			routingDataSource.afterPropertiesSet();
		}

		return dataSource;
	}

	public Class<? extends DataSource> getObjectType() {
		return DataSource.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
