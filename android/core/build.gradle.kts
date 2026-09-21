plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.autodiag.core"
    compileSdk = 35
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.json:json:20250517")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.0.21")
}

// Mechanical guard for future production diagnostic decoders.
// If no DiagnosticDecoder implementation exists yet, the gate passes.
tasks.register("decoderEvidenceGate") {
    group = "verification"
    description = "Fails if any DiagnosticDecoder<T> lacks a positive and negative evidence test."

    doLast {
        val sourceRoots = listOf(
            file("src/main/java"),
            file("src/main/kotlin")
        ).filter { it.exists() }

        val testRoots = listOf(
            file("src/test/java"),
            file("src/test/kotlin")
        ).filter { it.exists() }

        val decoderFiles = sourceRoots
            .flatMap { root ->
                root.walkTopDown()
                    .filter { it.isFile && it.extension == "kt" }
                    .toList()
            }
            .filter { file -> file.readText().contains("DiagnosticDecoder<") }

        val failures = mutableListOf<String>()

        for (decoderFile in decoderFiles) {
            val content = decoderFile.readText()
            val decoderName = decoderFile.nameWithoutExtension

            // Only treat a file as a concrete decoder when it declares a class/object
            // that implements DiagnosticDecoder, rather than merely mentioning the API.
            val isConcreteDecoder = Regex(
                "(?:class|object)\\s+$decoderName[\\s\\S]*?:[\\s\\S]*?DiagnosticDecoder<"
            ).containsMatchIn(content)

            if (!isConcreteDecoder) continue

            val testFile = testRoots
                .flatMap { root -> root.walkTopDown().filter { it.isFile }.toList() }
                .firstOrNull { it.name == "${decoderName}Test.kt" }

            if (testFile == null) {
                failures += "$decoderName: missing ${decoderName}Test.kt"
                continue
            }

            val testContent = testFile.readText()
            val hasPositive = testContent.contains("DecodeResult.Success")
            val hasNegative = testContent.contains("DecodeResult.Rejected") &&
                testContent.contains("RejectReason.")

            if (!hasPositive) {
                failures += "$decoderName: missing positive decode test (DecodeResult.Success)"
            }
            if (!hasNegative) {
                failures += "$decoderName: missing negative decode test (DecodeResult.Rejected + RejectReason)"
            }
        }

        if (failures.isNotEmpty()) {
            throw GradleException(
                "Decoder Evidence Gate failed:\n" +
                    failures.joinToString("\n") { "  - $it" }
            )
        }

        println("Decoder Evidence Gate: PASS (${decoderFiles.size} decoder source file(s) inspected).")
    }
}

tasks.named("check") {
    dependsOn("decoderEvidenceGate")
}
