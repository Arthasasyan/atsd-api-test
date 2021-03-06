package com.axibase.tsd.api.method.property;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.method.checks.AbstractCheck;
import com.axibase.tsd.api.method.checks.PropertyCheck;
import com.axibase.tsd.api.model.property.Property;
import com.axibase.tsd.api.model.property.PropertyQuery;
import com.axibase.tsd.api.util.NotCheckedException;
import com.axibase.tsd.api.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.axibase.tsd.api.util.Util.*;

public class PropertyMethod extends BaseMethod {
    private static final String METHOD_PROPERTY_INSERT = "/properties/insert";
    private static final String METHOD_PROPERTY_QUERY = "/properties/query";
    private static final String METHOD_PROPERTY_URL_QUERY = "/properties/{entity}/types/{type}";
    private static final String METHOD_PROPERTY_DELETE = "/properties/delete";
    private static final String METHOD_PROPERTY_TYPE_QUERY = "/properties/{entity}/types";
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    public static <T> Response insertProperty(T... queries) {
        Response response = executeApiRequest(webTarget -> webTarget
                .path(METHOD_PROPERTY_INSERT)
                .request()
                .post(Entity.json(Arrays.asList(queries))));
        response.bufferEntity();
        return response;
    }

    public static <T> Response queryProperty(T... queries) {
        Response response = executeApiRequest(webTarget -> webTarget
                .path(METHOD_PROPERTY_QUERY)
                .request()
                .post(Entity.json(Arrays.asList(queries))));
        response.bufferEntity();
        return response;
    }

    public static <T> Response deleteProperty(T... queries) {
        Response response = executeApiRequest(webTarget -> webTarget
                .path(METHOD_PROPERTY_DELETE)
                .request()
                .post(Entity.json(Arrays.asList(queries))));
        response.bufferEntity();
        return response;
    }

    public static Response urlQueryProperty(String propertyType, String entityName) {
        Response response = executeApiRequest(webTarget -> webTarget
                .path(METHOD_PROPERTY_URL_QUERY)
                .resolveTemplate("entity", entityName)
                .resolveTemplate("type", propertyType)
                .request().get());
        response.bufferEntity();
        return response;
    }

    public static Response typeQueryProperty(String entityName) {
        Response response = executeApiRequest(webTarget -> webTarget
                .path(METHOD_PROPERTY_TYPE_QUERY)
                .resolveTemplate("entity", entityName)
                .request().get());
        response.bufferEntity();
        return response;
    }


    public static void insertPropertyCheck(final Property property, AbstractCheck check) throws Exception {
        Response response = insertProperty(property);
        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response)) {
            throw new Exception("Can not execute insert property query");
        }
        Checker.check(check);
    }

    public static void insertPropertyCheck(final Property property) throws Exception {
        insertPropertyCheck(property, new PropertyCheck(property));
    }

    public static boolean propertyExist(final Property property) throws Exception {
        return propertyExist(property, false);
    }

    public static boolean propertyExist(final Property property, boolean strict) throws Exception {
        Response response = queryProperty(prepareStrictPropertyQuery(property));
        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response)) {
            response.close();
            throw new Exception("Fail to execute queryProperty");
        }
        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));
        String given = response.readEntity(String.class);
        logger.debug("check: {}\nresponse: {}", expected, given);
        return compareJsonString(expected, given, strict);
    }

    public static boolean propertyTypeExist(String propertyType) {
        final PropertyQuery q = new PropertyQuery();
        q.setEntity("*");
        q.setType(propertyType);
        q.setStartDate(MIN_STORABLE_DATE);
        q.setEndDate(MAX_STORABLE_DATE);

        q.setLimit(1);

        final Response response = queryProperty(q);
        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response)) {
            throw new NotCheckedException("Fail to execute property query");
        }

        String given = response.readEntity(String.class);
        return !"[]".equals(given);
    }

    private static Map prepareStrictPropertyQuery(final Property property) {
        Map<String, Object> query = new HashMap<>();
        query.put("entity", property.getEntity());
        query.put("type", property.getType());
        query.put("key", property.getKey());
        if (null == property.getDate()) {
            query.put("startDate", MIN_QUERYABLE_DATE);
            query.put("endDate", MAX_QUERYABLE_DATE);
        } else {
            query.put("startDate", property.getDate());
            query.put("interval", new HashMap<String, Object>() {{
                put("unit", "MILLISECOND");
                put("count", "1");
            }});
        }
        query.put("exactMatch", true);

        return query;
    }
}
