package org.cloudfoundry.credhub.services

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import java.util.UUID
import org.apache.commons.lang3.StringUtils
import org.cloudfoundry.credhub.PermissionOperation
import org.cloudfoundry.credhub.auth.UserContext
import org.cloudfoundry.credhub.auth.UserContext.ActorResultWip.*
import org.cloudfoundry.credhub.auth.UserContextHolder
import org.cloudfoundry.credhub.data.PermissionDataService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
@SuppressFBWarnings(value = ["NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE"], justification = "Let's refactor this class into kotlin")
class DefaultPermissionCheckingService @Autowired
constructor(
    private val permissionDataService: PermissionDataService,
    private val userContextHolder: UserContextHolder
) : PermissionCheckingService {

    @Value("\${security.authorization.acls.enabled}")
    private val enforcePermissions: Boolean = false

    override fun hasPermission(user: String, credentialName: String, permission: PermissionOperation): Boolean {
        if (enforcePermissions) {
            val name = StringUtils.prependIfMissing(credentialName, "/")
            return permissionDataService.hasPermission(user, name, permission)
        }
        return true
    }

    override fun hasPermission(user: String, permissionGuid: UUID, permission: PermissionOperation): Boolean {
        if (enforcePermissions) {
            val permissionData = permissionDataService.getPermission(permissionGuid) ?: return false
            return permissionDataService.hasPermission(user, permissionData.path!!, permission)
        }
        return true
    }

    override fun hasPermissions(user: String, path: String, permissions: List<PermissionOperation>): Boolean {
        for (permission in permissions) {
            if (!permissionDataService.hasPermission(user, path, permission)) {
                return false
            }
        }
        return true
    }

    override fun userAllowedToOperateOnActor(actor: String?): Boolean {
        if (enforcePermissions) {
            val userContext = userContextHolder.userContext
            return when(val ucActor = userContext?.actor){
                is Actor -> actor != null && !StringUtils.equals(ucActor.value, actor)
                is UnsupportedGrantType -> false
                is UnsupportedAuthMethod -> false
                null -> false
            }
        } else {
            return true
        }
    }

    override fun userAllowedToOperateOnActor(guid: UUID): Boolean {
        if (enforcePermissions) {
            val userContext = userContextHolder.userContext
            val actor = permissionDataService.getPermission(guid)!!.actor
            return when(val ucActor = userContext?.actor){
                is Actor -> actor != null && !StringUtils.equals(ucActor.value, actor)
                is UnsupportedGrantType -> false
                is UnsupportedAuthMethod -> false
                null -> false
            }
        } else {
            return true
        }
    }

    override fun findAllPathsByActor(actor: String): Set<String> {
        return permissionDataService.findAllPathsByActor(actor)
    }
}
