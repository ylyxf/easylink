package com.jirfox.easylink.utils;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import groovy.sql.Sql;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.internal.OraclePreparedStatement;

public class DataSyncer {

	private static Logger logger = LoggerFactory.getLogger(DataSyncer.class);

	private static final int BATCH_SIZE = 100;

	public static Integer doWriteData(Sql targetSql, String targetTable, List<Map<String, Object>> data)
			throws SQLException {
		OracleConnection connection = targetSql.getConnection().unwrap(oracle.jdbc.driver.OracleConnection.class);
		oracleBatchInsert(connection, targetTable, data);

		return data.size();
	}

	private static void oracleBatchInsert(OracleConnection connection, String targetTable,
			List<Map<String, Object>> batchData) throws SQLException {
		Map<String, List<Map<String, Object>>> grouped = batchData.stream()
				.collect(Collectors.groupingBy(m -> String.join(",", m.keySet())));
		for (Map.Entry<String, List<Map<String, Object>>> entry : grouped.entrySet()) {
			List<Map<String, Object>> groupedData = entry.getValue();
			String[] cols = StringUtils.split(entry.getKey(), ",");
			String args = Arrays.stream(cols).map(q -> "?").collect(Collectors.joining(","));

			String sql = "INSERT INTO " + targetTable + "(" + entry.getKey() + ") VALUES (" + args + ")";
			try (OraclePreparedStatement ps = (OraclePreparedStatement) connection.prepareStatement(sql)) {
				// https://docs.oracle.com/cd/E11882_01/java.112/e16548/oraperf.htm#JJDBC28757
				// BATCH_SIZE 不是越大越好
				ps.setExecuteBatch(BATCH_SIZE);

				for (Map<String, Object> data : groupedData) {
					for (int j = 0; j < cols.length; j++) {
						ps.setObject(j + 1, data.get(cols[j]));
					}
					ps.execute();
				}
				ps.sendBatch();
			} catch (SQLException e) {
				logger.error("Oracle批量插入出错", e);
				throw e;
			}
		}
	}

}