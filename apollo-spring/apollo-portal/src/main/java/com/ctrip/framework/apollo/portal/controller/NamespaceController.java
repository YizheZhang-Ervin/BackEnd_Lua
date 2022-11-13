package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.utils.InputValidator;
import com.ctrip.framework.apollo.common.utils.RequestPrecondition;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import com.ctrip.framework.apollo.portal.constant.RoleType;
import com.ctrip.framework.apollo.portal.entity.bo.NamespaceBO;
import com.ctrip.framework.apollo.portal.entity.model.NamespaceCreationModel;
import com.ctrip.framework.apollo.portal.listener.AppNamespaceCreationEvent;
import com.ctrip.framework.apollo.portal.service.AppNamespaceService;
import com.ctrip.framework.apollo.portal.service.NamespaceService;
import com.ctrip.framework.apollo.portal.service.RoleInitializationService;
import com.ctrip.framework.apollo.portal.service.RolePermissionService;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import com.ctrip.framework.apollo.portal.util.RoleUtils;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.ctrip.framework.apollo.common.utils.RequestPrecondition.checkModel;

/**
 * Namespace Controller
 */
@RestController
public class NamespaceController {

    private static final Logger logger = LoggerFactory.getLogger(NamespaceController.class);

    @Autowired
    private ApplicationEventPublisher publisher;
    @Autowired
    private UserInfoHolder userInfoHolder;
    @Autowired
    private NamespaceService namespaceService;
    @Autowired
    private AppNamespaceService appNamespaceService;
    @Autowired
    private RoleInitializationService roleInitializationService;
    @Autowired
    private RolePermissionService rolePermissionService;
    @Autowired
    private PortalConfig portalConfig;


    @RequestMapping(value = "/appnamespaces/public", method = RequestMethod.GET)
    public List<AppNamespace> findPublicAppNamespaces() {
        return appNamespaceService.findPublicAppNamespaces();
    }

    @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces", method = RequestMethod.GET)
    public List<NamespaceBO> findNamespaces(@PathVariable String appId, @PathVariable String env,
                                            @PathVariable String clusterName) {

        return namespaceService.findNamespaceBOs(appId, Env.valueOf(env), clusterName);
    }

    @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName:.+}", method = RequestMethod.GET)
    public NamespaceBO findNamespace(@PathVariable String appId, @PathVariable String env,
                                     @PathVariable String clusterName, @PathVariable String namespaceName) {

        return namespaceService.loadNamespaceBO(appId, Env.valueOf(env), clusterName, namespaceName);
    }

    @RequestMapping(value = "/envs/{env}/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/associated-public-namespace",
            method = RequestMethod.GET)
    public NamespaceBO findPublicNamespaceForAssociatedNamespace(@PathVariable String env,
                                                                 @PathVariable String appId,
                                                                 @PathVariable String namespaceName,
                                                                 @PathVariable String clusterName) {

        return namespaceService.findPublicNamespaceForAssociatedNamespace(Env.valueOf(env), appId, clusterName, namespaceName);
    }

    /**
     * 创建 Namespace
     * <p>
     * ps：关联 Namespace 也调用该接口
     *
     * @param appId  App 编号
     * @param models NamespaceCreationModel 数组
     * @return 成功
     */
    @PreAuthorize(value = "@permissionValidator.hasCreateNamespacePermission(#appId)")
    @RequestMapping(value = "/apps/{appId}/namespaces", method = RequestMethod.POST)
    public ResponseEntity<Void> createNamespace(@PathVariable String appId,
                                                @RequestBody List<NamespaceCreationModel> models) {
        // 校验 `models` 非空
        checkModel(!CollectionUtils.isEmpty(models));
        // 初始化 Namespace 的 Role 们。
        String namespaceName = models.get(0).getNamespace().getNamespaceName();
        String operator = userInfoHolder.getUser().getUserId();
        roleInitializationService.initNamespaceRoles(appId, namespaceName, operator);
        // 循环 `models` ，创建 Namespace 对象
        for (NamespaceCreationModel model : models) {
            NamespaceDTO namespace = model.getNamespace();
            // 校验相关参数非空
            RequestPrecondition.checkArgumentsNotEmpty(model.getEnv(), namespace.getAppId(), namespace.getClusterName(), namespace.getNamespaceName());
            // 创建 Namespace 对象
            try {
                namespaceService.createNamespace(Env.valueOf(model.getEnv()), namespace);
            } catch (Exception e) {
                logger.error("create namespace fail.", e);
                Tracer.logError(String.format("create namespace fail. (env=%s namespace=%s)", model.getEnv(), namespace.getNamespaceName()), e);
            }
        }
        // 授予 Namespace Role 给当前管理员
        assignNamespaceRoleToOperator(appId, namespaceName);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize(value = "@permissionValidator.hasDeleteNamespacePermission(#appId)")
    @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName:.+}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteNamespace(@PathVariable String appId, @PathVariable String env,
                                                @PathVariable String clusterName, @PathVariable String namespaceName) {

        namespaceService.deleteNamespace(appId, Env.valueOf(env), clusterName, namespaceName);

        return ResponseEntity.ok().build();
    }

    /**
     * 创建 AppNamespace
     *
     * @param appId        App 编号
     * @param appNamespace AppNamespace
     * @return 创建的 AppNamespace
     */
    @PreAuthorize(value = "@permissionValidator.hasCreateAppNamespacePermission(#appId, #appNamespace)")
    @RequestMapping(value = "/apps/{appId}/appnamespaces", method = RequestMethod.POST)
    public AppNamespace createAppNamespace(@PathVariable String appId, @RequestBody AppNamespace appNamespace) {
        // 校验 AppNamespace 的 `appId` 和 `name` 非空。
        RequestPrecondition.checkArgumentsNotEmpty(appNamespace.getAppId(), appNamespace.getName());
        // 校验 AppNamespace 的 `name` 格式正确。
        if (!InputValidator.isValidAppNamespace(appNamespace.getName())) {
            throw new BadRequestException(String.format("Namespace格式错误: %s",
                    InputValidator.INVALID_CLUSTER_NAMESPACE_MESSAGE + " & "
                            + InputValidator.INVALID_NAMESPACE_NAMESPACE_MESSAGE));
        }
        // 保存 AppNamespace 对象到数据库
        AppNamespace createdAppNamespace = appNamespaceService.createAppNamespaceInLocal(appNamespace);
        // 赋予权限，若满足如下任一条件：
        // 1. 公开类型的 AppNamespace 。
        // 2. 私有类型的 AppNamespace ，并且允许 App 管理员创建私有类型的 AppNamespace 。
        if (portalConfig.canAppAdminCreatePrivateNamespace() || createdAppNamespace.isPublic()) {
            //  授予 Namespace Role
            assignNamespaceRoleToOperator(appId, appNamespace.getName());
        }
        // 发布 AppNamespaceCreationEvent 创建事件
        publisher.publishEvent(new AppNamespaceCreationEvent(createdAppNamespace));
        // 返回创建的 AppNamespace 对象
        return createdAppNamespace;
    }

    /**
     * env -> cluster -> cluster has not published namespace?
     * Example:
     * dev ->
     * default -> true   (default cluster has not published namespace)
     * customCluster -> false (customCluster cluster's all namespaces had published)
     */
    @RequestMapping(value = "/apps/{appId}/namespaces/publish_info", method = RequestMethod.GET)
    public Map<String, Map<String, Boolean>> getNamespacesPublishInfo(@PathVariable String appId) {
        return namespaceService.getNamespacesPublishInfo(appId);
    }

    @RequestMapping(value = "/envs/{env}/appnamespaces/{publicNamespaceName}/namespaces", method = RequestMethod.GET)
    public List<NamespaceDTO> getPublicAppNamespaceAllNamespaces(@PathVariable String env,
                                                                 @PathVariable String publicNamespaceName,
                                                                 @RequestParam(name = "page", defaultValue = "0") int page,
                                                                 @RequestParam(name = "size", defaultValue = "10") int size) {

        return namespaceService.getPublicAppNamespaceAllNamespaces(Env.fromString(env), publicNamespaceName, page, size);

    }

    private void assignNamespaceRoleToOperator(String appId, String namespaceName) {
        // default assign modify、release namespace role to namespace creator
        String operator = userInfoHolder.getUser().getUserId();
        // 授予 Namespace 修改和发布的 Role 给管理员
        rolePermissionService.assignRoleToUsers(RoleUtils.buildNamespaceRoleName(appId, namespaceName, RoleType.MODIFY_NAMESPACE), Sets.newHashSet(operator), operator);
        rolePermissionService.assignRoleToUsers(RoleUtils.buildNamespaceRoleName(appId, namespaceName, RoleType.RELEASE_NAMESPACE), Sets.newHashSet(operator), operator);
    }

}