package com.axibase.tsd.api.method.sql.function.dateformat;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.method.version.VersionMethod;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.version.Version;
import org.testng.annotations.Test;

import java.util.Collections;

import static com.axibase.tsd.api.Util.TestNames.generateEntityName;
import static com.axibase.tsd.api.Util.TestNames.generateMetricName;
import static com.axibase.tsd.api.Util.formatDate;
import static com.axibase.tsd.api.Util.parseDate;
import static java.util.TimeZone.getTimeZone;


public class AutoTimeZoneTest extends SqlTest {
    private static final Sample DEFAULT_SAMPLE = new Sample("2016-06-03T09:41:00.000Z", "0");
    private static final String DEFAULT_PATTERN = "yyyy-MM-dd hh:mm";
    private static final String ALGIERS_TIMEZONE_ID = "Africa/Algiers";


    @Test
    public void testMetricTimeZone() throws Exception {
        Metric metric = new Metric(generateMetricName());
        metric.setTimeZoneID(ALGIERS_TIMEZONE_ID);
        MetricMethod.createOrReplaceMetricCheck(metric);

        Series series = new Series();
        series.setMetric(metric.getName());
        series.setEntity(generateEntityName());
        series.addData(DEFAULT_SAMPLE);
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));


        String sqlQuery = String.format(
                "SELECT date_format(time, '%s', AUTO) FROM '%s'",
                DEFAULT_PATTERN, metric.getName()
        );

        String[][] expectedRows = {
                {formatDate(parseDate(DEFAULT_SAMPLE.getD()), DEFAULT_PATTERN, getTimeZone(metric.getTimeZoneID()))}
        };

        assertSqlQueryRows(sqlQuery, expectedRows, "Failed to define metric timezone by AUTO param");
    }

    @Test
    public void testEntityTimeZone() throws Exception {
        Entity entity = new Entity(generateEntityName());
        entity.setTimeZoneID(ALGIERS_TIMEZONE_ID);
        EntityMethod.createOrReplaceEntityCheck(entity);

        Series series = new Series();
        series.setMetric(generateMetricName());
        series.setEntity(entity.getName());
        series.addData(DEFAULT_SAMPLE);
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));

        String sqlQuery = String.format(
                "SELECT date_format(time, '%s', AUTO) FROM '%s'",
                DEFAULT_PATTERN, series.getMetric()
        );

        String[][] expectedRows = {
                {formatDate(parseDate(DEFAULT_SAMPLE.getD()), DEFAULT_PATTERN, getTimeZone(entity.getTimeZoneID()))}
        };

        assertSqlQueryRows(sqlQuery, expectedRows, "Failed to define entity timezone by AUTO param");
    }


    @Test
    public void testPriorityTimeZone() throws Exception {
        Entity entity = new Entity(generateEntityName());
        entity.setTimeZoneID(ALGIERS_TIMEZONE_ID);
        EntityMethod.createOrReplaceEntityCheck(entity);


        Metric metric = new Metric(generateMetricName());
        String metricTimeZoneId = "Canada/Yukon";
        metric.setTimeZoneID(metricTimeZoneId);
        MetricMethod.createOrReplaceMetricCheck(metric);

        Series series = new Series();
        series.setMetric(metric.getName());
        series.setEntity(entity.getName());
        series.addData(DEFAULT_SAMPLE);
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));


        String sqlQuery = String.format(
                "SELECT date_format(time, '%s', AUTO) FROM '%s'",
                DEFAULT_PATTERN, series.getMetric()
        );

        String[][] expectedRows = {
                {formatDate(
                        parseDate(DEFAULT_SAMPLE.getD()), DEFAULT_PATTERN, getTimeZone(entity.getTimeZoneID()))}
        };

        assertSqlQueryRows(sqlQuery, expectedRows, "Failed to define entity timezone as priority by AUTO param");
    }

    @Test
    public void testDefaultTimeZone() throws Exception {
        Series series = new Series(generateEntityName(), generateMetricName());
        series.addData(DEFAULT_SAMPLE);
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));

        String sqlQuery = String.format(
                "SELECT date_format(time, '%s', AUTO) FROM '%s'",
                DEFAULT_PATTERN, series.getMetric()
        );

        Version version = VersionMethod.queryVersion().readEntity(Version.class);
        String[][] expectedRows = {
                {
                        formatDate(parseDate(DEFAULT_SAMPLE.getD()), DEFAULT_PATTERN,
                                getTimeZone(version.getDate().getTimeZone().getName())
                        )}
        };

        assertSqlQueryRows(sqlQuery, expectedRows, "Failed to define server timezone by AUTO param");
    }
}
