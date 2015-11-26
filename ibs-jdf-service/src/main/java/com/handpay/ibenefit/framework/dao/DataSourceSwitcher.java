package com.handpay.ibenefit.framework.dao;

public class DataSourceSwitcher {

	private static final ThreadLocal<String> DATASOURCE_HOLDER = new ThreadLocal<String>();

	public static void setDataSource(String name) {
		DATASOURCE_HOLDER.set(name);
	}

	public static String getDataSource() {
		return DATASOURCE_HOLDER.get();
	}

	public static void clearDataSource() {
		DATASOURCE_HOLDER.remove();
	}

	public static void setMaster() {
		clearDataSource();
	}

	public static void setSlave() {
		setDataSource("slave");
	}

}
