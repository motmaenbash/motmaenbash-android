package nu.milad.motmaenbash.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import dalvik.system.DexFile
import nu.milad.motmaenbash.models.ApkStatus
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipInputStream

/**
 * Advanced APK classes extraction utility with consistent SHA256 output
 */
object ApkClassesExtractor {
    private const val TAG = "ApkClassesExtractor"
    private const val BUFFER_SIZE = 8192

    /**
     * Extracts and calculates code hash using multiple methods
     */
    fun extractAndCalculateClassesHash(
        context: Context,
        applicationInfo: ApplicationInfo,
        apkFile: File
    ): ApkExtractionResult {

        Log.d(TAG, "Processing package: ${applicationInfo.packageName}")

        val apkStatus = checkApkEncryption(apkFile)

        Log.d(TAG, "APK Status: $apkStatus")
        Log.d(TAG, "APK Path: ${applicationInfo.sourceDir}")
        Log.d(TAG, "APK Size: ${apkFile.length() / 1024}KB")

        // Choose the best method based on APK type
        return when (apkStatus) {
            ApkStatus.READABLE -> {
                // For normal APKs: direct method is faster
                tryDirectApkExtraction(applicationInfo)
                    ?: trySplitApkExtraction(applicationInfo)
                    ?: tryDexFileMethod(applicationInfo)
                    ?: tryAlternativeApkPaths(context, applicationInfo)
                    ?: ApkExtractionResult.Error("All extraction methods failed")
            }

            ApkStatus.ENCRYPTED -> {
                // For encrypted APKs: DexFile API works better
                tryDexFileMethod(applicationInfo)
                    ?: tryAlternativeApkPaths(context, applicationInfo)
                    ?: trySplitApkExtraction(applicationInfo)
                    ?: ApkExtractionResult.Error("All extraction methods failed for encrypted APK")
            }

            else -> {
                // For corrupted or inaccessible APKs
                tryDexFileMethod(applicationInfo)
                    ?: tryAlternativeApkPaths(context, applicationInfo)
                    ?: ApkExtractionResult.Error("APK is corrupted or inaccessible")
            }
        }
    }

    /**
     * Fast APK status check
     */
    private fun checkApkEncryption(apkFile: File): ApkStatus {
        if (apkFile.length() == 0L) return ApkStatus.EMPTY

        return try {
            FileInputStream(apkFile).use { fileInput ->
                ZipInputStream(fileInput).use { zipInput ->
                    if (zipInput.nextEntry != null) ApkStatus.READABLE else ApkStatus.EMPTY
                }
            }
        } catch (e: ZipException) {
            if (e.message?.contains("encrypted", ignoreCase = true) == true) {
                ApkStatus.ENCRYPTED
            } else {
                ApkStatus.CORRUPTED
            }
        } catch (_: Exception) {
            ApkStatus.ERROR
        }
    }

    /**
     * Method 1: Direct APK extraction (fastest for normal APKs)
     */
    private fun tryDirectApkExtraction(applicationInfo: ApplicationInfo): ApkExtractionResult? {
        return try {
            val apkFile = File(applicationInfo.sourceDir)

            if (!apkFile.exists() || !apkFile.canRead()) {
                Log.w(TAG, "APK file not accessible: ${apkFile.absolutePath}")
                return null
            }

            Log.d(TAG, "Trying direct extraction from: ${apkFile.absolutePath}")

            FileInputStream(apkFile).use { fileInput ->
                ZipInputStream(fileInput).use { zipInput ->
                    var entry: ZipEntry?
                    val dexDataList = mutableListOf<ByteArray>()

                    while (zipInput.nextEntry.also { entry = it } != null) {
                        val entryName = entry!!.name

                        if (entryName.matches(Regex("classes\\d*\\.dex"))) {
                            Log.d(TAG, "Found DEX file: $entryName (${entry!!.size} bytes)")

                            try {
                                val dexBytes = readEntryBytes(zipInput)
                                dexDataList.add(dexBytes)
                                Log.d(
                                    TAG,
                                    "Successfully read DEX file: $entryName (${dexBytes.size} bytes)"
                                )
                            } catch (e: Exception) {
                                Log.w(TAG, "Failed to read DEX: $entryName", e)
                            }
                        }
                        zipInput.closeEntry()
                    }

                    if (dexDataList.isNotEmpty()) {
                        // Create combined SHA256 from all DEX files
                        val combinedHash = calculateCombinedDexHash(dexDataList)

                        Log.d(
                            TAG,
                            "Direct extraction successful: found ${dexDataList.size} DEX files"
                        )
                        Log.d(TAG, "Combined DEX SHA256: $combinedHash")

                        return ApkExtractionResult.Success(
                            primaryHash = combinedHash,
                            allDexHashes = mapOf("direct_dex" to combinedHash),
                            method = "Direct APK (${dexDataList.size} DEX files)"
                        )
                    }
                }
            }
            null
        } catch (e: ZipException) {
            Log.w(TAG, "ZIP error (encrypted or corrupted): ${e.message}")
            null
        } catch (e: SecurityException) {
            Log.w(TAG, "Security error accessing APK: ${e.message}")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in direct extraction", e)
            null
        }
    }

    /**
     * Method 2: Split APKs extraction
     */
    private fun trySplitApkExtraction(applicationInfo: ApplicationInfo): ApkExtractionResult? {
        return try {
            val splitApkPaths = applicationInfo.splitSourceDirs

            if (splitApkPaths.isNullOrEmpty()) {
                Log.d(TAG, "No split APKs found")
                return null
            }

            Log.d(TAG, "Found ${splitApkPaths.size} split APKs")

            val allDexData = mutableListOf<ByteArray>()

            splitApkPaths.forEach { splitPath ->
                val splitFile = File(splitPath)
                Log.d(TAG, "Checking split APK: ${splitFile.name}")

                try {
                    FileInputStream(splitFile).use { fileInput ->
                        ZipInputStream(fileInput).use { zipInput ->
                            var entry: ZipEntry?

                            while (zipInput.nextEntry.also { entry = it } != null) {
                                val entryName = entry!!.name

                                if (entryName.matches(Regex("classes\\d*\\.dex"))) {
                                    val dexBytes = readEntryBytes(zipInput)
                                    allDexData.add(dexBytes)
                                    Log.d(
                                        TAG,
                                        "Split DEX found: ${splitFile.name}:$entryName (${dexBytes.size} bytes)"
                                    )
                                }
                                zipInput.closeEntry()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to process split APK: ${splitFile.name}", e)
                }
            }

            if (allDexData.isNotEmpty()) {
                val combinedHash = calculateCombinedDexHash(allDexData)

                Log.d(TAG, "Split extraction successful: found ${allDexData.size} DEX files")
                Log.d(TAG, "Combined DEX SHA256: $combinedHash")

                return ApkExtractionResult.Success(
                    primaryHash = combinedHash,
                    allDexHashes = mapOf("split_dex" to combinedHash),
                    method = "Split APKs (${allDexData.size} DEX files)"
                )
            }

            null
        } catch (e: Exception) {
            Log.e(TAG, "Error processing split APKs", e)
            null
        }
    }

    /**
     * Method 3: DexFile API (for encrypted APKs)
     */
    private fun tryDexFileMethod(applicationInfo: ApplicationInfo): ApkExtractionResult? {
        return try {
            Log.d(TAG, "Trying DexFile API method")

            val dexFile = DexFile(applicationInfo.sourceDir)
            val entries = dexFile.entries()
            val classList = mutableListOf<String>()

            while (entries.hasMoreElements()) {
                classList.add(entries.nextElement())
            }

            dexFile.close()

            if (classList.isNotEmpty()) {
                val classListString = classList.sorted().joinToString("\n")
                // Create SHA256 from sorted class list for consistency
                val hash = HashUtils.generateSHA256(classListString)
                Log.d(TAG, "DexFile method: found ${classList.size} classes")
                Log.d(TAG, "Class-based SHA256: $hash")

                Log.d(TAG, "Joined class list for hashing:\n$classListString")
                return ApkExtractionResult.Success(
                    primaryHash = hash,
                    allDexHashes = mapOf("class_fingerprint" to hash),
                    method = "DexFile API (${classList.size} classes)"
                )
            }

            null
        } catch (e: Exception) {
            Log.w(TAG, "DexFile API failed", e)
            null
        }
    }

    /**
     * Method 4: Alternative APK paths
     */
    private fun tryAlternativeApkPaths(
        context: Context,
        applicationInfo: ApplicationInfo
    ): ApkExtractionResult? {
        return try {
            val packageManager = context.packageManager
            var packageName = applicationInfo.packageName

            val freshAppInfo = packageManager.getApplicationInfo(packageName, 0)

            val alternativePaths = listOf(
                freshAppInfo.sourceDir,
                freshAppInfo.publicSourceDir,
                "/data/app/$packageName/base.apk",
                "/data/app/${packageName}-1/base.apk",
                "/data/app/${packageName}-2/base.apk"
            ).distinct()

            Log.d(TAG, "Trying ${alternativePaths.size} alternative paths")

            alternativePaths.forEach { path ->
                val file = File(path)
                if (file.exists() && file != File(applicationInfo.sourceDir)) {
                    Log.d(TAG, "Trying alternative path: $path")

                    val tempAppInfo = ApplicationInfo().apply {
                        sourceDir = path
                        packageName = applicationInfo.packageName
                    }

                    val result = tryDirectApkExtraction(tempAppInfo)

                    if (result != null && result is ApkExtractionResult.Success) {
                        return result.copy(method = "Alternative path: $path")
                    }
                }
            }

            null
        } catch (e: Exception) {
            Log.e(TAG, "Error trying alternative paths", e)
            null
        }
    }

    /**
     * Calculates combined SHA256 from multiple DEX files
     * For single DEX file: returns direct SHA256 of the file
     * For multiple DEX files: combines individual hashes consistently
     */
    private fun calculateCombinedDexHash(dexDataList: List<ByteArray>): String {
        return try {

            if (dexDataList.isEmpty()) {
                return "NO_DEX_FILES"

            }

            // For single file, return direct hash
            if (dexDataList.size == 1) {
                return HashUtils.generateHashFromBytes(dexDataList[0], HashAlgorithms.SHA256)
            }


            // For multiple files, return combined hash
            val individualHashes = dexDataList.map { dexData ->
                HashUtils.generateHashFromBytes(dexData, HashAlgorithms.SHA256)
            }.sorted()

            val combinedString = individualHashes.joinToString("")
            return HashUtils.generateSHA256(combinedString)

        } catch (e: Exception) {
            Log.e(TAG, "Error calculating combined DEX hash", e)
            "HASH_ERROR"
        }
    }

    /**
     * Reads zip entry bytes using buffered approach
     */
    private fun readEntryBytes(zipInput: ZipInputStream): ByteArray {
        val output = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(BUFFER_SIZE)
        var bytesRead: Int

        while (zipInput.read(buffer).also { bytesRead = it } != -1) {
            output.write(buffer, 0, bytesRead)
        }

        return output.toByteArray()
    }
}

/**
 * Result class for APK classes extraction
 */
sealed class ApkExtractionResult {
    data class Success(
        val primaryHash: String,
        val allDexHashes: Map<String, String>,
        val method: String
    ) : ApkExtractionResult()

    data class Error(val message: String) : ApkExtractionResult()
}