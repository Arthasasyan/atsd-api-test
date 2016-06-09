package com.axibase.tsd.api.method.property;


import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.builder.PropertyBuilder;
import com.axibase.tsd.api.model.property.Property;
import com.axibase.tsd.api.transport.http.AtsdHttpResponse;
import com.axibase.tsd.api.transport.http.HTTPMethod;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Dmitry Korchagin.
 */

@SuppressWarnings("unchecked")
public class PropertyQueryTest extends PropertyMethod {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    @BeforeClass
    public static void setUpBeforeClass() {
        prepareRequestSender();
    }


    @Test
    public void test_TypeEntitiesStartEnd_bothFounded_wrongNotFounded() throws IOException {
        final Property property = new Property("query-type9", "query-entity9");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        insertPropertyCheck(property);

        final Property lastProperty = new Property(null, "query-entity9-2");
        lastProperty.setType(property.getType());
        insertPropertyCheck(lastProperty);

        final Property wrongProperty = new Property(null, "query-wrongentity-9");
        wrongProperty.setType(property.getType());
        insertPropertyCheck(wrongProperty);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entities", new String[] {property.getEntity(), lastProperty.getEntity()});
        queryObj.put("startDate", Util.ISOFormat(Util.getPastDate()));
        queryObj.put("interval", new JSONObject() {{
            put("count", 2);
            put("unit", "DAY");
        }});

        assertTrue(queryProperties(queryObj, property, lastProperty));
        assertFalse(queryProperties(queryObj, wrongProperty));
    }

    @Test
    public void test_TypeEntityStartInterval_LastDEFAULT_bothFounded() throws IOException {
        final Property property = new Property("query-type8", "query-entity8");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        property.setDate(Util.getPastDate());
        insertPropertyCheck(property);

        final Property lastProperty = new Property();
        lastProperty.setType(property.getType());
        lastProperty.setEntity(property.getEntity());
        lastProperty.addTag("t2", "tv2");
        lastProperty.setDate(Util.getCurrentDate());
        insertPropertyCheck(lastProperty);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("startDate", Util.ISOFormat(Util.getPastDate()));
        queryObj.put("interval", new JSONObject() {{
            put("count", 2);
            put("unit", "DAY");
        }});

        assertTrue(queryProperties(queryObj, property, lastProperty));
    }

    @Test
    public void test_TypeEntityStartInterval_LastFALSE_bothFounded() throws IOException {
        final Property property = new Property("query-type7", "query-entity7");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        property.setDate(Util.getPastDate());
        insertPropertyCheck(property);

        final Property lastProperty = new Property();
        lastProperty.setType(property.getType());
        lastProperty.setEntity(property.getEntity());
        lastProperty.setDate(Util.getCurrentDate());
        insertPropertyCheck(lastProperty);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("startDate", Util.ISOFormat(Util.getPastDate()));
        queryObj.put("interval", new JSONObject() {{
            put("count", 2);
            put("unit", "DAY");
        }});
        queryObj.put("last", false);

        assertTrue(queryProperties(queryObj, property, lastProperty));
    }

    @Test
    public void test_TypeEntityStartInterval_LastTRUE_propertyLastFounded() throws IOException {
        final Property property = new Property("query-type6", "query-entity6");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        property.setDate(Util.getPastDate());
        insertPropertyCheck(property);

        final Property lastProperty = new Property();
        lastProperty.setType(property.getType());
        lastProperty.setEntity(property.getEntity());
        lastProperty.setDate(Util.getCurrentDate());
        insertPropertyCheck(lastProperty);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("startDate", Util.ISOFormat(Util.getPastDate()));
        queryObj.put("interval", new JSONObject() {{
            put("count", 2);
            put("unit", "DAY");
        }});
        queryObj.put("last", true);

        assertTrue(queryProperties(queryObj, lastProperty));
        assertFalse(queryProperties(queryObj, property));
    }

    @Test
    public void test_TypeEntity_StartPast_IntervalGiveFuture_propertyFounded() throws IOException {
        final Property property = new Property("query-type5", "query-entity5");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        property.setDate(Util.getCurrentDate());
        insertPropertyCheck(property);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("startDate", Util.ISOFormat(Util.getPastDate()));
        queryObj.put("interval", new JSONObject() {{
            put("count", 2);
            put("unit", "DAY");
        }});

        assertTrue(queryProperties(queryObj, property));
    }


    @Test
    public void test_TypeEntity_StartEQDate_Interval1MS_propertyFounded() throws IOException {
        final Property property = new Property("query-type4", "query-entity4");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        property.setDate(Util.getCurrentDate());
        insertPropertyCheck(property);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("startDate", property.getDate());
        queryObj.put("interval", new JSONObject() {{
            put("count", 1);
            put("unit", "MILLISECOND");
        }});

        assertTrue(queryProperties(queryObj, property));
    }


    @Test
    public void test_TypeEntity_StartPast_EndFuture_propertyFounded() throws IOException {
        final Property property = new Property("query-type3", "query-entity3");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        property.setDate(Util.getCurrentDate());
        insertPropertyCheck(property);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("startDate", Util.ISOFormat(Util.getPastDate()));
        queryObj.put("endDate", Util.ISOFormat(Util.getFutureDate()));

        assertTrue(queryProperties(queryObj, property));
    }


    @Test
    public void test_TypeEntityStartEnd_ExactDEFAULT_propertyFounded() throws IOException {
        final Property property = new Property("query-type2", "query-entity2");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        property.setDate(Util.getCurrentDate());
        insertPropertyCheck(property);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("startDate", property.getDate());
        queryObj.put("endDate", Util.ISOFormat(Util.getFutureDate()));

        assertTrue(queryProperties(queryObj, property));
    }

    @Test
    public void test_TypeEntityStartEnd_ExactFALSE_propertyFounded() throws IOException {
        final Property property = new Property("query-type1", "query-entity1");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        property.setDate(Util.getCurrentDate());
        insertPropertyCheck(property);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("exactMatch", false);
        queryObj.put("startDate", property.getDate());
        queryObj.put("endDate", Util.ISOFormat(Util.getFutureDate()));

        assertTrue(queryProperties(queryObj, property));
    }

    @Test
    public void test_Example_TypeEntityStartEnd_Partkey_propertyFounded() throws IOException, ParseException {
        final Property property = new Property("disk", "nurswgvml007");
        property.addTag("fs_type", "ext4");
        property.addKey("file_system", "/");
        property.addKey("mount_point", "/sda1");
        property.setDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse("2016-05-25T04:00:00.000Z"));
        insertPropertyCheck(property);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("key", new HashMap<String, String>(){{
            put("file_system", "/");
        }});
        queryObj.put("startDate", "2016-05-25T04:00:00Z");
        queryObj.put("endDate", "2016-05-25T05:00:00Z");

        assertTrue(queryProperties(queryObj, property));
    }

    @Test
    public void test_TypeEntity_Exception() throws IOException {
        JSONArray request = new JSONArray() {{
            add(new JSONObject() {{
                put("type", "testtype");
                put("entity", "testentity");
            }});
        }};

        AtsdHttpResponse response = httpSender.send(HTTPMethod.POST, METHOD_PROPERTY_QUERY, request.toJSONString());
        assertEquals(400, response.getCode());
        assertEquals("{\"error\":\"IllegalArgumentException: Missing parameters. One of the following combinations is required: interval, interval + startTime/startDate, interval + endTime/endDate, startTime/startDate + endTime/endDate\"}", response.getBody());
    }

    @Test
    public void test_Type_Exception() throws IOException {
        JSONArray request = new JSONArray() {{
            add(new JSONObject() {{
                put("type", "testtype");
            }});
        }};

        AtsdHttpResponse response = httpSender.send(HTTPMethod.POST, METHOD_PROPERTY_QUERY, request.toJSONString());
        assertEquals(400, response.getCode());
        assertEquals("{\"error\":\"IllegalArgumentException: Missing parameters. One of the following combinations is required: interval, interval + startTime/startDate, interval + endTime/endDate, startTime/startDate + endTime/endDate\"}", response.getBody());
    }

    @Test
    public void test_TypeStart_Exception() throws IOException {
        JSONArray request = new JSONArray() {{
            add(new JSONObject() {{
                put("type", "testtype");
                put("startDate", "2016-06-01T12:04:59.191Z");
            }});
        }};

        AtsdHttpResponse response = httpSender.send(HTTPMethod.POST, METHOD_PROPERTY_QUERY, request.toJSONString());
        assertEquals(400, response.getCode());
        assertEquals("{\"error\":\"IllegalArgumentException: Missing parameters. One of the following combinations is required: interval, interval + startTime/startDate, interval + endTime/endDate, startTime/startDate + endTime/endDate\"}", response.getBody());
    }

    @Test
    public void test_TypeStartEnd_Exception() throws IOException {
        JSONArray request = new JSONArray() {{
            add(new JSONObject() {{
                put("type", "testtype");
                put("startDate", "2016-06-01T12:04:59.191Z");
                put("endDate", "2016-06-01T13:04:59.191Z");
            }});
        }};

        AtsdHttpResponse response = httpSender.send(HTTPMethod.POST, METHOD_PROPERTY_QUERY, request.toJSONString());
        assertEquals(400, response.getCode());
        assertEquals("{\"error\":\"IllegalArgumentException: entity or entities or entityGroup or entityExpression must not be empty\"}", response.getBody());
    }


}
