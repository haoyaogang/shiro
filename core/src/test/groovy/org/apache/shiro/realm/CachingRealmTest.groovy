package org.apache.shiro.realm

import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.cache.Cache
import org.apache.shiro.cache.CacheManager
import org.apache.shiro.subject.PrincipalCollection
import static org.easymock.EasyMock.*

/**
 * Unit tests for the {@link CachingRealm} implementation.
 */
class CachingRealmTest extends GroovyTestCase {

    void testCachingEnabled() {

        CachingRealm realm = new TestCachingRealm()

        assertTrue realm.cachingEnabled
        realm.cachingEnabled = false
        assertFalse realm.cachingEnabled
    }

    void testSetName() {

        CachingRealm realm = new TestCachingRealm()

        assertTrue realm.name.contains(TestCachingRealm.class.getName())

        realm.name = "testRealm"
        assertEquals "testRealm", realm.name
    }


    void testNewInstanceWithCacheManager() {

        def cacheManager = createStrictMock(CacheManager)

        CachingRealm realm = new TestCachingRealm()
        realm.cacheManager = cacheManager

        assertNotNull realm.cacheManager
        assertTrue realm.templateMethodCalled
    }

    void testOnLogout() {

        def realmName = "testRealm"

        def cacheManager = createStrictMock(CacheManager)
        def cache = createStrictMock(Cache)
        def principals = createStrictMock(PrincipalCollection)

        expect(principals.isEmpty()).andReturn(false).anyTimes()

        replay cacheManager, cache, principals

        CachingRealm realm = new TestCachingRealm()

        realm.cacheManager = cacheManager
        realm.name = realmName

        realm.onLogout(principals)

        assertTrue realm.doClearCacheCalled

        verify cacheManager, cache, principals
    }

    void testGetAvailablePrincipalWithRealmPrincipals() {

        def realmName = "testRealm"
        def username = "foo"

        def principals = createStrictMock(PrincipalCollection)

        expect(principals.isEmpty()).andReturn false
        expect(principals.fromRealm(eq(realmName))).andReturn([username])

        replay principals

        CachingRealm realm = new TestCachingRealm()
        realm.name = realmName

        Object principal = realm.getAvailablePrincipal(principals)

        assertEquals username, principal

        verify principals
    }

    void testGetAvailablePrincipalWithoutRealmPrincipals() {

        def realmName = "testRealm"
        def username = "foo"

        def principals = createStrictMock(PrincipalCollection)

        expect(principals.isEmpty()).andReturn false
        expect(principals.fromRealm(eq(realmName))).andReturn null
        expect(principals.getPrimaryPrincipal()).andReturn username

        replay principals

        CachingRealm realm = new TestCachingRealm()
        realm.name = realmName

        Object principal = realm.getAvailablePrincipal(principals)

        assertEquals username, principal

        verify principals
    }

    private static final class TestCachingRealm extends CachingRealm {

        def info;

        boolean templateMethodCalled = false
        boolean doClearCacheCalled = false

        boolean supports(AuthenticationToken token) {
            return true
        }

        AuthenticationInfo getAuthenticationInfo(AuthenticationToken token) {
            return info;
        }

        @Override
        protected void afterCacheManagerSet() {
            super.afterCacheManagerSet()
            templateMethodCalled = true
        }

        @Override
        protected void doClearCache(PrincipalCollection principals) {
            super.doClearCache(principals)
            doClearCacheCalled = true
        }
    }
}
