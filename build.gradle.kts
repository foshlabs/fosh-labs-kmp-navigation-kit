// Load gradle-secrets.properties so publishing credentials are available (file is gitignored)
allprojects {
    rootProject.file("gradle-secrets.properties").takeIf { it.exists() }?.let { secretsFile ->
        val props = java.util.Properties().apply { load(secretsFile.inputStream()) }
        props.forEach { k, v ->
            if (findProperty(k.toString()) == null) {
                ext.set(k.toString(), v.toString())
            }
        }
        // vanniktech uses mavenCentralUsername/Password; map from ossrh if needed
        if (findProperty("mavenCentralUsername") == null && props.containsKey("ossrhUsername")) {
            ext.set("mavenCentralUsername", props.getProperty("ossrhUsername"))
        }
        if (findProperty("mavenCentralPassword") == null && props.containsKey("ossrhPassword")) {
            ext.set("mavenCentralPassword", props.getProperty("ossrhPassword"))
        }
        // vanniktech uses signingInMemoryKey/Id/Password
        if (findProperty("signingInMemoryKey") == null && props.containsKey("signing.key")) {
            ext.set("signingInMemoryKey", props.getProperty("signing.key"))
        }
        if (findProperty("signingInMemoryKeyId") == null && props.containsKey("signing.keyId")) {
            ext.set("signingInMemoryKeyId", props.getProperty("signing.keyId"))
        }
        if (findProperty("signingInMemoryKeyPassword") == null && props.containsKey("signing.password")) {
            ext.set("signingInMemoryKeyPassword", props.getProperty("signing.password"))
        }
    }
}

plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.kmpNativeCoroutines) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.vanniktechMavenPublish) apply false
}

tasks.register("checkSigningConfiguration") {
    group = "publishing"
    description = "Verifies that Maven Central and PGP signing credentials are configured"
    doLast {
        val keyFile = findProperty("signing.secretKeyRingFile") as String? ?: System.getenv("SIGNING_SECRET_KEY_RING_FILE")
        val hasKeyFile = keyFile != null && rootProject.file(keyFile).exists()
        val hasKeyInline = (findProperty("signing.key") ?: System.getenv("SIGNING_KEY"))?.toString()?.isNotBlank() == true

        val required = mapOf(
            "ossrhUsername" to (findProperty("ossrhUsername") ?: findProperty("mavenCentralUsername") ?: System.getenv("OSSRH_USERNAME") ?: System.getenv("MAVEN_CENTRAL_USERNAME")),
            "ossrhPassword" to (findProperty("ossrhPassword") ?: findProperty("mavenCentralPassword") ?: System.getenv("OSSRH_PASSWORD") ?: System.getenv("MAVEN_CENTRAL_PASSWORD")),
            "signing.keyId" to (findProperty("signing.keyId") ?: System.getenv("SIGNING_KEY_ID")),
            "signing.password" to (findProperty("signing.password") ?: System.getenv("SIGNING_PASSWORD")),
            "signing key (signing.secretKeyRingFile or signing.key)" to (if (hasKeyFile || hasKeyInline) "set" else null)
        )
        val missing = required.filter { (_, value) -> value == null || value.toString().isBlank() }.keys
        if (missing.isNotEmpty()) {
            throw GradleException(
                "Missing signing credentials. Add these to gradle-secrets.properties or set as env vars:\n" +
                    missing.joinToString("\n") { "  - $it" }
            )
        }
        val keyId = required["signing.keyId"]!!.toString()
        if (keyId.length != 8) {
            throw GradleException(
                "signing.keyId should be the last 8 characters of your PGP key ID, got ${keyId.length} characters"
            )
        }
        logger.lifecycle("All signing credentials are configured.")
    }
}
