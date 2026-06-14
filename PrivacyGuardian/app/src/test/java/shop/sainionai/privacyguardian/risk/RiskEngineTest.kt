package shop.sainionai.privacyguardian.risk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import shop.sainionai.privacyguardian.model.PermissionCategory
import shop.sainionai.privacyguardian.model.ScannedPermission

/**
 * These tests pin the RiskEngine v1 behaviour. If you change the algorithm,
 * bump ENGINE_VERSION and update these expectations deliberately.
 */
class RiskEngineTest {

    private fun perm(name: String, cat: PermissionCategory, points: Int,
                     granted: Boolean = true, expected: Boolean = true) =
        ScannedPermission(name, granted, cat, points, expected)

    @Test
    fun emptyPermissions_isLow_andConfidenceMatchesAssessedWeight() {
        val score = RiskEngine.score(emptyList(), trackerCount = 0, reputationValue = null)
        assertEquals(0, score.overall)
        assertEquals(0.55, score.confidence, 0.001)
        assertEquals("1.3.0", score.engineVersion)
    }

    @Test
    fun unexpectedPermission_scoresHigherThanExpected() {
        val expected = RiskEngine.score(listOf(
            perm("android.permission.READ_SMS", PermissionCategory.SMS, 88, expected = true)
        ))
        val unexpected = RiskEngine.score(listOf(
            perm("android.permission.READ_SMS", PermissionCategory.SMS, 88, expected = false)
        ))
        assertTrue(unexpected.overall > expected.overall)
    }

    @Test
    fun reputationHit_isAssessed_raisesConfidenceAndScore() {
        val unknown = RiskEngine.score(emptyList(), 0, reputationValue = null)
        val flagged = RiskEngine.score(emptyList(), 0, reputationValue = 90, reputationNote = "x")
        assertEquals(0.75, flagged.confidence, 0.001)   // +0.20 reputation weight
        assertTrue(flagged.overall > unknown.overall)
        assertTrue(flagged.components.first { it.name == "Reputation" }.assessed)
    }

    @Test
    fun trackers_raiseScore_andAreAssessed() {
        val noTrackers = RiskEngine.score(emptyList(), trackerCount = 0)
        val manyTrackers = RiskEngine.score(emptyList(), trackerCount = 8)
        assertTrue(manyTrackers.overall > noTrackers.overall)
        assertTrue(manyTrackers.components.first { it.name == "Behavior" }.assessed)
    }

    @Test
    fun scoringIsDeterministic() {
        val perms = listOf(
            perm("android.permission.READ_SMS", PermissionCategory.SMS, 90),
            perm("android.permission.READ_CONTACTS", PermissionCategory.CONTACTS, 70)
        )
        val a = RiskEngine.score(perms)
        val b = RiskEngine.score(perms)
        assertEquals(a.overall, b.overall)
        assertEquals(a.components, b.components)
    }

    @Test
    fun smsPlusContacts_triggersComboBonus_andEscalatesLevel() {
        val withCombo = RiskEngine.score(listOf(
            perm("android.permission.READ_SMS", PermissionCategory.SMS, 90),
            perm("android.permission.READ_CONTACTS", PermissionCategory.CONTACTS, 70),
            perm("android.permission.INTERNET", PermissionCategory.NETWORK, 15)
        ))
        // Classic abusive-loan-app signature should land High or Critical.
        assertTrue(withCombo.overall >= 55)
    }

    @Test
    fun deniedPermissions_scoreLowerThanGranted() {
        val granted = RiskEngine.score(listOf(
            perm("android.permission.READ_SMS", PermissionCategory.SMS, 90, granted = true)
        ))
        val denied = RiskEngine.score(listOf(
            perm("android.permission.READ_SMS", PermissionCategory.SMS, 90, granted = false)
        ))
        assertTrue(granted.overall > denied.overall)
    }
}
