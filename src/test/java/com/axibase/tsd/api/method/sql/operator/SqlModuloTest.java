package com.axibase.tsd.api.method.sql.operator;

import com.axibase.tsd.api.Registry;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.ProcessingException;
import java.util.*;

import static org.testng.AssertJUnit.assertEquals;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlModuloTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-modulo-";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";
    private static final String TEST_METRIC1_NAME = TEST_PREFIX + "metric-1";
    private static final String TEST_METRIC2_NAME = TEST_PREFIX + "metric-2";
    private static double EPS = 10e-4;


    @BeforeClass
    public static void prepareData() throws Exception {
        Registry.Entity.register(TEST_ENTITY_NAME);
        Registry.Metric.register(TEST_METRIC1_NAME);
        Registry.Metric.register(TEST_METRIC2_NAME);

        final List<Series> seriesList = new ArrayList<>();
        seriesList.add(new Series() {{
            setMetric(TEST_METRIC1_NAME);
            setEntity(TEST_ENTITY_NAME);
            setData(Arrays.asList(
                    new Sample("2016-06-03T09:23:00.000Z", "7"),
                    new Sample("2016-06-03T09:24:00.000Z", "0"),
                    new Sample("2016-06-03T09:25:00.000Z", "12"),
                    new Sample("2016-06-03T09:26:00.000Z", "10.3"),
                    new Sample("2016-06-03T09:27:00.000Z", "10")
            ));
            setTags(Collections.unmodifiableMap(new HashMap<String, String>() {{
                put("a", "b");
                put("b", "c");
            }}));
        }});


        seriesList.add(new Series() {{
            setMetric(TEST_METRIC2_NAME);
            setEntity(TEST_ENTITY_NAME);
            setData(Arrays.asList(
                    new Sample("2016-06-03T09:23:00.000Z", "5"),
                    new Sample("2016-06-03T09:24:00.000Z", "7"),
                    new Sample("2016-06-03T09:25:00.000Z", "-2"),
                    new Sample("2016-06-03T09:26:00.000Z", "-2.1")
            ));
            setTags(Collections.unmodifiableMap(new HashMap<String, String>() {{
                put("a", "b");
                put("b", "c");
            }}));
        }});

        SeriesMethod.insertSeriesCheck(seriesList);
    }


    /*
      Following tests related to issue #2922
     */

    /**
     * issue #2922
     */
    @Test
    public void testDividingPositiveByPositiveInteger() {
        String sqlQuery = String.format(
                "SELECT m1.value AS 'num', m2.value AS 'den', m1.value %s m2.value AS 'modulo' FROM '%s' m1\n " +
                        "OUTER JOIN '%s' m2\nWHERE t1.datetime = '2016-06-03T09:23:00.000Z' AND t1.entity = '%s'",
                "%", TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );
        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList("7.0", "5.0", "2.0")
        );

        List<List<String>> resultRows = executeQuery(sqlQuery)
                .readEntity(StringTable.class)
                .filterRows("num", "den", "modulo");
        assertTableRows(expectedRows, resultRows);
    }


    /**
     * issue #2922
     */
    @Test
    public void testDividingZeroByPositiveInteger() {
        String sqlQuery = String.format(
                "SELECT m1.value AS 'num', m2.value AS 'den', m1.value %s m2.value AS 'modulo' FROM '%s' m1\n " +
                        "OUTER JOIN '%s' m2\nWHERE t1.datetime = '2016-06-03T09:24:00.000Z' AND t1.entity = '%s'",
                "%", TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList("0.0", "7.0", "0.0")
        );

        List<List<String>> resultRows = executeQuery(sqlQuery)
                .readEntity(StringTable.class)
                .filterRows("num", "den", "modulo");
        assertTableRows(expectedRows, resultRows);
    }

    /**
     * issue #2922
     */
    @Test
    public void testDividingPositiveByZeroInteger() {
        String sqlQuery = String.format(
                "SELECT m2.value AS 'num', m1.value AS 'den', m2.value %s m1.value AS 'modulo' FROM '%s' m1\n " +
                        "OUTER JOIN '%s' m2\nWHERE t1.datetime = '2016-06-03T09:24:00.000Z' AND t1.entity = '%s'",
                "%", TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList("7.0", "0.0", "NaN")
        );

        List<List<String>> resultRows = executeQuery(sqlQuery)
                .readEntity(StringTable.class)
                .filterRows("num", "den", "modulo");
        assertTableRows(expectedRows, resultRows);
    }


    /**
     * issue #2922
     */
    @Test
    public void testDividingPositiveByNegativeInteger() {
        String sqlQuery = String.format(
                "SELECT m1.value AS 'num', m2.value AS 'den', m1.value %s m2.value AS 'modulo' FROM '%s' m1\n " +
                        "OUTER JOIN '%s' m2\nWHERE t1.datetime = '2016-06-03T09:25:00.000Z' AND t1.entity = '%s'",
                "%", TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList("12.0", "-2.0", "0.0")
        );

        List<List<String>> resultRows = executeQuery(sqlQuery)
                .readEntity(StringTable.class)
                .filterRows("num", "den", "modulo");

        assertTableRows(expectedRows, resultRows);
    }

    /**
     * issue #2922
     */
    @Test
    public void testDividingNegativeByPositiveInteger() {
        String sqlQuery = String.format(
                "SELECT m2.value AS 'num', m1.value AS 'den', m2.value %s m1.value AS 'modulo' FROM '%s' m1\n " +
                        "OUTER JOIN '%s' m2\nWHERE t1.datetime = '2016-06-03T09:25:00.000Z' AND t1.entity = '%s'",
                "%", TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );


        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList("-2.0", "12.0", "-2.0")
        );

        List<List<String>> resultRows = executeQuery(sqlQuery)
                .readEntity(StringTable.class)
                .filterRows("num", "den", "modulo");
        assertTableRows(expectedRows, resultRows);
    }


    /**
     * issue #2922
     */
    @Test
    public void testDividingPositiveByNegativeDecimal() {
        String sqlQuery = String.format(

                "SELECT m1.value AS 'num', m2.value AS 'den', m1.value %s m2.value AS 'modulo' FROM '%s' m1\n " +
                        "OUTER JOIN '%s' m2\nWHERE t1.datetime = '2016-06-03T09:26:00.000Z' AND t1.entity = '%s'",
                "%", TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );

        Double expectedModulo = 1.9;

        Double resultModulo = Double.parseDouble(executeQuery(sqlQuery)
                .readEntity(StringTable.class)
                .getValueAt(2, 0)
        );

        assertEquals(expectedModulo, resultModulo, EPS);
    }


    /**
     * issue #2922
     */
    @Test
    public void testDividingNegativeByPositiveDecimal() {
        String sqlQuery = String.format(
                "SELECT m2.value AS 'num', m1.value AS 'den', m2.value %s m1.value AS 'modulo' FROM '%s' m1\n " +
                        "OUTER JOIN '%s' m2\nWHERE t1.datetime = '2016-06-03T09:26:00.000Z' AND t1.entity = '%s'",
                "%", TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );
        Double expectedModulo = -2.1;

        Double resultModulo = Double.parseDouble(executeQuery(sqlQuery)
                .readEntity(StringTable.class)
                .getValueAt(2, 0)
        );

        assertEquals(expectedModulo, resultModulo, EPS);
    }


    /**
     * issue #2922
     */
    @Test
    public void testDividingNullByNumber() {
        String sqlQuery = String.format(
                "SELECT m1.value AS 'num', m2.value AS 'den', m1.value %s m2.value AS 'modulo' FROM '%s' m1\n " +
                        "OUTER JOIN '%s' m2\nWHERE t1.datetime = '2016-06-03T09:27:00.000Z' AND t1.entity = '%s'",
                "%", TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList("10.0", "null", "null")
        );

        List<List<String>> resultRows = executeQuery(sqlQuery)
                .readEntity(StringTable.class)
                .filterRows("num", "den", "modulo");
        assertTableRows(expectedRows, resultRows);
    }


    /**
     * issue #2922
     */
    @Test
    public void testDividingNumberByNull() {
        String sqlQuery = String.format(
                "SELECT m2.value AS 'num', m1.value AS 'den', m2.value %s m1.value AS 'modulo' FROM '%s' m1\n " +
                        "OUTER JOIN '%s' m2\nWHERE t1.datetime = '2016-06-03T09:27:00.000Z' AND t1.entity = '%s'",
                "%", TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList("null", "10.0", "null")
        );

        List<List<String>> resultRows = executeQuery(sqlQuery)
                .readEntity(StringTable.class)
                .filterRows("num", "den", "modulo");
        assertTableRows(expectedRows, resultRows);
    }


    /**
     * issue #2922
     */
    @Test(expectedExceptions = ProcessingException.class)
    public void testDividingStringByString() {
        String sqlQuery = String.format(
                "SELECT m2.value AS 'num', m1.value AS 'den', tags.a %s tags.b AS 'modulo' FROM '%s' m1\n " +
                        "OUTER JOIN '%s' m2\nWHERE t1.datetime = '2016-06-03T09:27:00.000Z' AND t1.entity = '%s'",
                "%", TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );

        executeQuery(sqlQuery)
                .readEntity(StringTable.class);
    }

    /**
     * issue #2922
     */
    @Test(expectedExceptions = ProcessingException.class)
    public void testDividingNaNByNumber() {
        String sqlQuery = String.format(
                "SELECT value, 0/0 %s m1.value AS 'modulo' FROM '%s'\n " +
                        "WHERE t1.datetime = '2016-06-03T09:23:00.000Z' AND t1.entity = '%s' ",
                "%", TEST_METRIC1_NAME, TEST_ENTITY_NAME
        );

        executeQuery(sqlQuery)
                .readEntity(StringTable.class);
    }

}