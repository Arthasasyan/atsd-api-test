package com.axibase.tsd.api.method.entitygroup;

import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.entitygroup.EntityGroup;
import com.axibase.tsd.api.util.NotCheckedException;
import com.axibase.tsd.api.util.Util;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

/**
 * @author Dmitry Korchagin.
 */
public class EntityGroupMethod extends BaseMethod {
    private final static String METHOD_ENTITYGROUP = "/entity-groups/{group}";
    private final static String METHOD_ENTITYGROUP_ENTITIES = "/entity-groups/{group}/entities";
    private final static String METHOD_ENTITYGROUP_ENTITIES_ADD = "/entity-groups/{group}/entities/add";
    private final static String METHOD_ENTITYGROUP_ENTITIES_SET = "/entity-groups/{group}/entities/set";
    private final static String METHOD_ENTITYGROUP_ENTITIES_DELETE = "/entity-groups/{group}/entities/delete";
    final static String SYNTAX_ALLOWED_ENTITYGROUP_EXPRESSION = "properties('some.prop').size() > 0";

    public static Response createOrReplaceEntityGroup(EntityGroup entityGroup) {
        Response response = executeApiRequest(webTarget -> webTarget
                .path(METHOD_ENTITYGROUP)
                .resolveTemplate("group", entityGroup.getName())
                .request()
                .put(Entity.json(entityGroup)));
        response.bufferEntity();
        return response;
    }

    public static void createOrReplaceEntityGroupCheck(EntityGroup entityGroup) throws Exception {
        Response response = createOrReplaceEntityGroup(entityGroup);
        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response)) {
            throw new IllegalStateException("Fail to execute createOrReplaceEntityGroup query");
        }

        response = getEntityGroup(entityGroup.getName());
        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response)) {
            throw new IllegalStateException("Fail to execute getEntityGroup query");
        }

        if (!compareJsonString(jacksonMapper.writeValueAsString(entityGroup), response.readEntity(String.class))) {
            throw new IllegalStateException("Fail to check entityGroup inserted");
        }
    }

    public static boolean entityGroupExist(EntityGroup entityGroup) throws Exception {
        Response response = getEntityGroup(entityGroup.getName());
        if (response.getStatus() == NOT_FOUND.getStatusCode()) {
            return false;
        }
        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response)) {
            throw new IllegalStateException("Fail to execute getEntityGroup query");
        }

        final String expected = jacksonMapper.writeValueAsString(entityGroup);
        final String given = response.readEntity(String.class);
        return compareJsonString(expected, given, true);
    }

    public static boolean entityGroupExist(String entityGroup) throws NotCheckedException {
        final Response response = EntityGroupMethod.getEntityGroup(entityGroup);
        if (Response.Status.Family.SUCCESSFUL == Util.responseFamily(response)) {
            return true;
        } else if (response.getStatus() == NOT_FOUND.getStatusCode()) {
            return false;
        }
        throw new NotCheckedException("Fail to execute entity group query");
    }

    public static Response getEntityGroup(String groupName) {
        Response response = executeApiRequest(webTarget -> webTarget
                .path(METHOD_ENTITYGROUP)
                .resolveTemplate("group", groupName)
                .request()
                .get());
        response.bufferEntity();
        return response;
    }

    public static Response updateEntityGroup(EntityGroup entityGroup) {
        Response response = executeApiRequest(webTarget -> webTarget
                .path(METHOD_ENTITYGROUP)
                .resolveTemplate("group", entityGroup.getName())
                .request()
                .method("PATCH", Entity.json(entityGroup)));
        response.bufferEntity();
        return response;
    }

    public static Response deleteEntityGroup(String groupName) {
        Response response = executeApiRequest(webTarget -> webTarget
                .path(METHOD_ENTITYGROUP)
                .resolveTemplate("group", groupName)
                .request()
                .delete());
        response.bufferEntity();
        return response;
    }

    public static Response getEntities(String groupName, Map<String, String> parameters) {
        Response response = executeApiRequest(webTarget -> {
            WebTarget target = webTarget.path(METHOD_ENTITYGROUP_ENTITIES).resolveTemplate("group", groupName);
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                target = target.queryParam(entry.getKey(), entry.getValue());
            }
            return target.request().get();
        });

        response.bufferEntity();
        return response;
    }

    public static Response getEntities(String groupName) {
        return getEntities(groupName, new HashMap<>());
    }

    public static Response addEntities(String groupName, Boolean createEntities, List<String> entityNames) {
        Response response = executeApiRequest(webTarget -> {
            WebTarget target = webTarget
                    .path(METHOD_ENTITYGROUP_ENTITIES_ADD)
                    .resolveTemplate("group", groupName);
            if (createEntities != null) {
                target = target.queryParam("createEntities", createEntities);
            }
            return target.request().post(Entity.json(entityNames));
        });

        response.bufferEntity();
        return response;
    }

    public static Response addEntities(String groupName, List<String> entityNames) {
        return addEntities(groupName, true, entityNames);
    }

    public static Response setEntities(String groupName, Boolean createEntities, List<String> entityNames) {
        Response response = executeApiRequest(webTarget -> {
            WebTarget target = webTarget
                    .path(METHOD_ENTITYGROUP_ENTITIES_SET)
                    .resolveTemplate("group", groupName);
            if (createEntities != null) {
                target = target.queryParam("createEntities", createEntities);
            }
            return target.request().post(Entity.json(entityNames));
        });

        response.bufferEntity();
        return response;
    }

    public static Response setEntities(String groupName, List<String> entityNames) {
        return setEntities(groupName, true, entityNames);
    }

    public static Response deleteEntities(String groupName, List entityNames) {
        Response response = executeApiRequest(webTarget -> webTarget
                .path(METHOD_ENTITYGROUP_ENTITIES_DELETE)
                .resolveTemplate("group", groupName)
                .request()
                .post(Entity.json(entityNames)));
        response.bufferEntity();
        return response;
    }
}



