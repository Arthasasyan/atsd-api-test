package com.axibase.tsd.api.method.sql.operator;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.ProcessingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlNotEqualsOperatorTest extends SqlMethod {
    private static final String TEST_PREFIX = "sql-not-equals-syntax-";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";

    @BeforeClass
    public static void prepareData() throws Exception {
        SeriesMethod.insertSeriesCheck(
                new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME) {{
                    setTags(Collections.unmodifiableMap(new HashMap<String, String>() {{
                        put("a", "b");
                    }}));
                    addData(new Sample("2016-06-03T09:23:00.000Z", "1.01"));
                }}
        );
    }

    /*
      Following tests related to issue #2933
     */


    /**
     * issue #2933
     */
    @Test(expectedExceptions = ProcessingException.class)
    public void testNotEqualsWithDatetimeIsFalse() {
        final String sqlQuery = String.format(
                "SELECT entity, value, datetime FROM '%s'" +
                        "WHERE datetime <> '2016-06-03T09:23:00.000Z' AND entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        executeQuery(sqlQuery)
                .readEntity(StringTable.class);
    }

    /**
     * issue #2933
     */
    @Test(expectedExceptions = ProcessingException.class)
    public void testNotEqualsWithDatetimeIsTrue() {
        final String sqlQuery = String.format(
                "SELECT entity, value, datetime FROM '%s'" +
                        "WHERE datetime <> '2016-06-03T09:25:00.000Z' AND entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        executeQuery(sqlQuery)
                .readEntity(StringTable.class);
    }

    /**
     * issue #2933
     */
    @Test
    public void testNotEqualsWithNumericIsFalse() {
        final String sqlQuery = String.format(
                "SELECT entity, value FROM '%s'WHERE value <> 1.2 AND entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        List<List<String>> resultRows = executeQuery(sqlQuery)
                .readEntity(StringTable.class)
                .filterRows("value");

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("1.01")
        );

        assertEquals("Table value rows must be identical", expectedRows, resultRows);
    }

    /**
     * issue #2933
     */
    @Test
    public void testNotEqualsWithNumericIsTrue() {
        final String sqlQuery = String.format(
                "SELECT entity, value FROM '%s'\nWHERE value <> 1.01 AND entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        List<List<String>> resultRows = executeQuery(sqlQuery)
                .readEntity(StringTable.class)
                .getRows();
        assertTrue("Result rows must be empty", resultRows.isEmpty());
    }

    /**
     * issue #2933
     */
    @Test
    public void testNotEqualsWitStringIsFalse() {
        final String sqlQuery = String.format(
                "SELECT entity, value, datetime FROM '%s'\nWHERE tags.a <> 'b'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        List<List<String>> resultRows = executeQuery(sqlQuery)
                .readEntity(StringTable.class)
                .getRows();
        assertTrue("Result rows must be empty", resultRows.isEmpty());
    }

    /**
     * issue #2933
     */
    @Test
    public void testNotEqualsWithStringIsTrue() {
        final String sqlQuery = String.format(
                "SELECT entity, tags.a FROM '%s'\n WHERE tags.a <> 'a' AND entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        List<List<String>> resultRows = executeQuery(sqlQuery)
                .readEntity(StringTable.class)
                .filterRows("tags.a");

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("b")
        );

        assertEquals("Table value rows must be identical", expectedRows, resultRows);
    }
}