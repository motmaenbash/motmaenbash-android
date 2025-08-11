package nu.milad.motmaenbash.utils

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.core.database.sqlite.transaction
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.consts.AppConstants.COLUMN_HASH
import nu.milad.motmaenbash.consts.AppConstants.DATABASE_NAME
import nu.milad.motmaenbash.consts.AppConstants.DATABASE_VERSION
import nu.milad.motmaenbash.consts.AppConstants.STAT_FLAGGED_APP_DETECTED
import nu.milad.motmaenbash.consts.AppConstants.STAT_FLAGGED_LINK_DETECTED
import nu.milad.motmaenbash.consts.AppConstants.STAT_FLAGGED_SMS_DETECTED
import nu.milad.motmaenbash.consts.AppConstants.STAT_TOTAL_SCANNED_APP
import nu.milad.motmaenbash.consts.AppConstants.STAT_TOTAL_SCANNED_LINK
import nu.milad.motmaenbash.consts.AppConstants.STAT_TOTAL_SCANNED_SMS
import nu.milad.motmaenbash.consts.AppConstants.STAT_VERIFIED_GATEWAY
import nu.milad.motmaenbash.consts.AppConstants.TABLE_ALERT_HISTORY
import nu.milad.motmaenbash.consts.AppConstants.TABLE_FLAGGED_APPS
import nu.milad.motmaenbash.consts.AppConstants.TABLE_FLAGGED_MESSAGES
import nu.milad.motmaenbash.consts.AppConstants.TABLE_FLAGGED_SENDERS
import nu.milad.motmaenbash.consts.AppConstants.TABLE_FLAGGED_URLS
import nu.milad.motmaenbash.consts.AppConstants.TABLE_FLAGGED_WORDS
import nu.milad.motmaenbash.consts.AppConstants.TABLE_SIDELOAD_TRUSTED_APPS
import nu.milad.motmaenbash.consts.AppConstants.TABLE_STATS
import nu.milad.motmaenbash.consts.AppConstants.TABLE_TIPS
import nu.milad.motmaenbash.consts.AppConstants.TABLE_UPDATE_HISTORY
import nu.milad.motmaenbash.models.Alert
import nu.milad.motmaenbash.models.Stats
import nu.milad.motmaenbash.utils.SmsUtils.generateNormalizedMessageHash
import nu.milad.motmaenbash.utils.UrlUtils.extractDomain
import nu.milad.motmaenbash.utils.UrlUtils.removeQueryStringAndFragment
import org.json.JSONArray

class DatabaseHelper(appContext: Context) :
    SQLiteOpenHelper(appContext, DATABASE_NAME, null, DATABASE_VERSION) {


    private val context = appContext

    object DataIndex {
        const val FLAGGED_URLS = 0
        const val FLAGGED_SENDERS = 1
        const val FLAGGED_MESSAGES = 2
        const val FLAGGED_WORDS = 3
        const val FLAGGED_APPS = 4
        const val SIDELOAD_TRUSTED_APPS = 5
    }

    override fun onCreate(db: SQLiteDatabase) {
        createTables(db)
        prepopulateData(db)
        prepopulateStatsData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        if (oldVersion < 3) {
            db.execSQL(
                """
                CREATE TABLE alert_history_temp (
                   id INTEGER PRIMARY KEY AUTOINCREMENT,
                    type INTEGER NOT NULL,
                    timestamp INTEGER NOT NULL
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                INSERT INTO alert_history_temp (id, type, timestamp)
                SELECT id, type, timestamp FROM $TABLE_ALERT_HISTORY
                """.trimIndent()
            )

            db.execSQL("DROP TABLE $TABLE_ALERT_HISTORY")
            db.execSQL("ALTER TABLE alert_history_temp RENAME TO $TABLE_ALERT_HISTORY")
        }

        if (oldVersion < 4) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS $TABLE_SIDELOAD_TRUSTED_APPS (
                    hash TEXT NOT NULL UNIQUE
                );
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_sideload_trusted_apps_hash ON $TABLE_SIDELOAD_TRUSTED_APPS(hash);")
        }

        // dropTables(db)
        // createTables(db)
        // prepopulateData(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Call onUpgrade to handle the downgrade
        // This will effectively reset the database to the new version
        onUpgrade(db, oldVersion, newVersion)
    }

    private fun createTables(db: SQLiteDatabase) {

        // Creating flagged links table
        // Get the values from the enums for type constraints
        val threatTypeValues = Alert.ThreatType.entries
            .joinToString(", ") { it.value.toString() }

        val alertLevelValues = Alert.AlertLevel.entries
            .joinToString(", ") { it.value.toString() }

        db.execSQL(
            """
                CREATE TABLE $TABLE_FLAGGED_URLS (
                    hash TEXT NOT NULL UNIQUE,
                    type INTEGER NOT NULL CHECK(type IN ($threatTypeValues)), -- ${Alert.ThreatType.PHISHING.value}: phishing, ${Alert.ThreatType.SCAM.value}: scam, ${Alert.ThreatType.PONZI.value}: ponzi
                    match INTEGER NOT NULL CHECK(match IN (1, 2)), -- 1: domain, 2: specific url
                    level INTEGER NOT NULL CHECK(level IN ($alertLevelValues))) -- '${Alert.AlertLevel.ALERT.value}:alert', '${Alert.AlertLevel.WARNING.value}:warning', '${Alert.AlertLevel.INFO.value}:info'

            """
        )
        // Creating index
        db.execSQL("CREATE INDEX IF NOT EXISTS index_flagged_links_hash ON $TABLE_FLAGGED_URLS(hash);")

        // Creating flagged sms senders table
        db.execSQL(
            """
                CREATE TABLE $TABLE_FLAGGED_SENDERS (
                    hash TEXT NOT NULL UNIQUE
                );
            """
        )
        // Creating index
        db.execSQL("CREATE INDEX IF NOT EXISTS index_flagged_senders_hash ON $TABLE_FLAGGED_SENDERS(hash);")


        // Creating flagged sms messages table
        db.execSQL(
            """
                CREATE TABLE $TABLE_FLAGGED_MESSAGES (
                    hash TEXT NOT NULL UNIQUE
                );
            """
        )
        // Creating index
        db.execSQL("CREATE INDEX IF NOT EXISTS index_flagged_messages_hash ON $TABLE_FLAGGED_MESSAGES(hash);")


        // Creating flagged sms keywords table
        db.execSQL(
            """
                CREATE TABLE $TABLE_FLAGGED_WORDS (
                    hash TEXT NOT NULL UNIQUE
                );
            """
        )
        // Creating index
        db.execSQL("CREATE INDEX IF NOT EXISTS index_flagged_words ON $TABLE_FLAGGED_WORDS(hash);")


        // Creating flagged apps table
        db.execSQL(
            """
                CREATE TABLE $TABLE_FLAGGED_APPS (

                    hash TEXT NOT NULL UNIQUE,
                    type INTEGER NOT NULL CHECK(type IN (1, 2, 3)) -- 1: Package, 2: APK, 3: Sign
                );
            """
        )
        // Creating index
        db.execSQL("CREATE INDEX IF NOT EXISTS index_flagged_apps_hash ON $TABLE_FLAGGED_APPS(hash);")

        // Creating tips table if not exists
        db.execSQL(
            """
                CREATE TABLE IF NOT EXISTS $TABLE_TIPS (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    tip TEXT NOT NULL UNIQUE
                );
            """
        )

        // Creating user stats table if not exists
        db.execSQL(
            """
                CREATE TABLE IF NOT EXISTS $TABLE_STATS (
                    stat_key TEXT NOT NULL UNIQUE,
                    stat_count INTEGER NOT NULL
                );
            """
        )
        // Creating index
        db.execSQL("CREATE INDEX IF NOT EXISTS index_stats_stat_key ON $TABLE_STATS(stat_key);")


        // Creating database update history table if not exists
        db.execSQL(
            """
    CREATE TABLE IF NOT EXISTS $TABLE_UPDATE_HISTORY (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        type INTEGER NOT NULL, -- manual/auto
        timestamp INTEGER NOT NULL
    );
    """
        )

        // Creating alert history table if not exists
        db.execSQL(
            """
    CREATE TABLE IF NOT EXISTS $TABLE_ALERT_HISTORY (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        type INTEGER NOT NULL,
        timestamp INTEGER NOT NULL
    );
    """
        )
    }

    private fun dropTables(db: SQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FLAGGED_URLS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FLAGGED_SENDERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FLAGGED_MESSAGES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FLAGGED_WORDS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FLAGGED_APPS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TIPS")
    }

    private fun prepopulateData(db: SQLiteDatabase?) {
        db?.transaction {
            try {
                val inputStream = context.resources.openRawResource(R.raw.data)
                val jsonData = inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(jsonData)

                insertData(this, TABLE_FLAGGED_URLS, jsonArray.getJSONArray(DataIndex.FLAGGED_URLS))
                insertData(
                    this,
                    TABLE_FLAGGED_SENDERS,
                    jsonArray.getJSONArray(DataIndex.FLAGGED_SENDERS)
                )
                insertData(
                    this,
                    TABLE_FLAGGED_MESSAGES,
                    jsonArray.getJSONArray(DataIndex.FLAGGED_MESSAGES)
                )
                insertData(
                    this,
                    TABLE_FLAGGED_WORDS,
                    jsonArray.getJSONArray(DataIndex.FLAGGED_WORDS)
                )
                insertData(this, TABLE_FLAGGED_APPS, jsonArray.getJSONArray(DataIndex.FLAGGED_APPS))

                insertData(
                    this,
                    TABLE_SIDELOAD_TRUSTED_APPS,
                    jsonArray.getJSONArray(DataIndex.SIDELOAD_TRUSTED_APPS)
                )

                // Tips
                val tipsInputStream = context.resources.openRawResource(R.raw.tips)
                val tipsText = tipsInputStream.bufferedReader().use { it.readText() }
                val tipsArray = JSONArray(tipsText)
                insertData(this, TABLE_TIPS, tipsArray)


            } catch (e: Exception) {
                Log.e(
                    "DatabaseHelper",
                    "Error prepopulating data: ${e.message}",
                    e
                )
            } finally {
            }
        }
    }


    private fun prepopulateStatsData(db: SQLiteDatabase?) {

        val insertQuery = """
        INSERT OR IGNORE INTO $TABLE_STATS (stat_key, stat_count) VALUES
            ('$STAT_VERIFIED_GATEWAY', 0),
            ('$STAT_FLAGGED_LINK_DETECTED', 0),
            ('$STAT_FLAGGED_SMS_DETECTED', 0),
            ('$STAT_FLAGGED_APP_DETECTED', 0),
        
            ('$STAT_TOTAL_SCANNED_LINK', 0),
            ('$STAT_TOTAL_SCANNED_SMS', 0),
            ('$STAT_TOTAL_SCANNED_APP', 0)
    """
        db?.execSQL(insertQuery)


    }


    fun populateDatabaseWithFetchedData(dataJsonArray: JSONArray) {
        val tables = arrayOf(
            TABLE_FLAGGED_URLS,
            TABLE_FLAGGED_SENDERS,
            TABLE_FLAGGED_MESSAGES,
            TABLE_FLAGGED_WORDS,
            TABLE_FLAGGED_APPS,
            TABLE_SIDELOAD_TRUSTED_APPS,
            TABLE_TIPS
        )

        tables.forEachIndexed { index, tableName ->
            val jsonArray = dataJsonArray.getJSONArray(index)
            if (jsonArray != null && jsonArray.length() > 0) {
                insertData(writableDatabase, tableName, jsonArray)
            }
        }
    }

    private fun insertData(
        db: SQLiteDatabase?,
        tableName: String,
        jsonArray: JSONArray
    ) {
        db?.let {

            val insertValues = ContentValues()
            try {

                for (i in 0 until jsonArray.length()) {
                    it.transaction {

                        when (tableName) {

                            TABLE_FLAGGED_SENDERS, TABLE_FLAGGED_WORDS, TABLE_FLAGGED_MESSAGES -> {
                                val hash = jsonArray.getString(i)
                                if (hash.startsWith("-")) {
                                    // Delete record
                                    val originalHash = hash.substring(1)

                                    delete(tableName, "hash = ? LIMIT 1", arrayOf(originalHash))
                                } else {
                                    // Insert new record
                                    insertValues.clear()
                                    insertValues.put("hash", hash)

                                    insertWithOnConflict(
                                        tableName,
                                        null,
                                        insertValues,
                                        SQLiteDatabase.CONFLICT_IGNORE
                                    )
                                }
                            }

                            TABLE_FLAGGED_URLS -> {
                                val jsonObject = jsonArray.getJSONObject(i)
                                val threatType = jsonObject.getInt("type")
                                val urlMatch = jsonObject.getInt("match")
                                val alertLevel = jsonObject.getInt("level")
                                val hashesArray = jsonObject.getJSONArray("hashes")

                                for (j in 0 until hashesArray.length()) {
                                    val hash = hashesArray.getString(j)
                                    if (hash.startsWith("-")) {
                                        // Delete record
                                        val originalHash = hash.substring(1)
                                        delete(tableName, "hash = ?", arrayOf(originalHash))
                                    } else {
                                        // Insert new record
                                        insertValues.clear()
                                        insertValues.put("hash", hash)
                                        insertValues.put("type", threatType)
                                        insertValues.put("match", urlMatch)
                                        insertValues.put("level", alertLevel)

                                        insertWithOnConflict(
                                            tableName,
                                            null,
                                            insertValues,
                                            SQLiteDatabase.CONFLICT_IGNORE
                                        )

                                    }

                                }

                            }


                            TABLE_FLAGGED_APPS -> {
                                val jsonObject = jsonArray.getJSONObject(i)
                                val type = jsonObject.getInt("type")
                                val hashesArray = jsonObject.getJSONArray("hashes")
                                for (j in 0 until hashesArray.length()) {
                                    val hash = hashesArray.getString(j)
                                    if (hash.startsWith("-")) {
                                        // Delete record
                                        val originalHash = hash.substring(1)
                                        delete(tableName, "hash = ?", arrayOf(originalHash))
                                    } else {
                                        // Insert new record
                                        insertValues.clear()
                                        insertValues.put("hash", hash)
                                        insertValues.put("type", type)

                                        insertWithOnConflict(
                                            tableName,
                                            null,
                                            insertValues,
                                            SQLiteDatabase.CONFLICT_IGNORE
                                        )
                                    }

                                }


                            }

                            TABLE_SIDELOAD_TRUSTED_APPS -> {
                                val hash = jsonArray.getString(i)
                                if (hash.startsWith("-")) {
                                    // Delete record
                                    val originalHash = hash.substring(1)
                                    delete(tableName, "hash = ? LIMIT 1", arrayOf(originalHash))
                                } else {
                                    // Insert new record
                                    insertValues.clear()
                                    insertValues.put("hash", hash)
                                    insertWithOnConflict(
                                        tableName,
                                        null,
                                        insertValues,
                                        SQLiteDatabase.CONFLICT_IGNORE
                                    )
                                }
                            }

                            TABLE_TIPS -> {
                                insertValues.clear()
                                insertValues.put("tip", jsonArray.getString(i))

                                // Insert new record
                                insertWithOnConflict(
                                    tableName, null, insertValues, SQLiteDatabase.CONFLICT_IGNORE
                                )
                            }

                        }
                    }

                }
            } catch (_: Exception) {
            }
        }
    }


    fun isAppFlagged(packageName: String, apkHash: String, signHash: String): Boolean {
        val packageHash = HashUtils.generateSHA256(packageName.lowercase())
        val selection =
            "(hash = ? AND type = 1) OR (hash = ? AND type = 2) OR (hash = ? AND type = 3)"

        val selectionArgs = arrayOf(packageHash, apkHash, signHash)
        val isFlagged = countData(TABLE_FLAGGED_APPS, selection, selectionArgs) > 0

        return isFlagged
    }

    fun isTrustedSideloadApp(packageName: String, signatureHash: String): Boolean {
        val packageHash = HashUtils.generateSHA256(packageName.lowercase())
        val combinedHash = HashUtils.generateSHA256("$packageHash:$signatureHash")

        val selection = "$COLUMN_HASH = ?"
        val selectionArgs = arrayOf(combinedHash)
        val isTrusted = countData(TABLE_SIDELOAD_TRUSTED_APPS, selection, selectionArgs) > 0


        return isTrusted
    }


    fun isSenderFlagged(sender: String): Boolean {
        val hash = HashUtils.generateSHA256(sender)
        val selection = "$COLUMN_HASH = ?"
        val selectionArgs = arrayOf(hash)
        val isFlagged = countData(TABLE_FLAGGED_SENDERS, selection, selectionArgs) > 0

        return isFlagged
    }


    fun hasFlaggedWord(message: String): Boolean {

        // Split the message into words, trim them, and remove duplicates using a Set
        val words = message.split("\\s+".toRegex()).map { it.trim() }.toSet()

        Log.d("DatabaseHelper", "Words: $words")
        // Generate hashes for each word
        val wordHashes = words.map { HashUtils.generateSHA256(it) }
        Log.d("DatabaseHelper", "Word hashes: $wordHashes")

        // Prepare placeholders for SQL query
        val placeholders = wordHashes.joinToString(",") { "?" }


        // Query to check if any word hash exists in the flagged words table
        val query =
            "SELECT COUNT(*) FROM $TABLE_FLAGGED_WORDS WHERE $COLUMN_HASH IN ($placeholders) LIMIT 1"


        Log.d("DatabaseHelper", "Query: $query")

        readableDatabase.rawQuery(query, wordHashes.toTypedArray()).use { cursor ->
            if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                return true
            }
        }


        return false
    }

    fun isMessageFlagged(message: String): Boolean {

        val hash = generateNormalizedMessageHash(message) ?: return false
        val selection = "$COLUMN_HASH = ?"
        val selectionArgs = arrayOf(hash)
        val isFlagged = countData(TABLE_FLAGGED_MESSAGES, selection, selectionArgs) > 0

        return isFlagged
    }

    fun hasFlaggedLink(links: List<String>): Boolean {

        val fullUrlHashes = mutableListOf<String>()
        val domainHashes = mutableListOf<String>()

        links.forEach { link ->
            val cleanedLink = UrlUtils.removeUrlPrefixes(link)
            val domain = extractDomain(link)

            // Generate hashes for both cleaned URL and domain
            val fullUrlHash = HashUtils.generateSHA256(cleanedLink)
            val domainHash = HashUtils.generateSHA256(domain)

            fullUrlHashes.add(fullUrlHash)
            domainHashes.add(domainHash)
        }

        // Prepare placeholders for SQL query
        val fullUrlPlaceholders = fullUrlHashes.joinToString(",") { "?" }
        val domainPlaceholders = domainHashes.joinToString(",") { "?" }

        val query =
            "SELECT COUNT(*) FROM $TABLE_FLAGGED_URLS WHERE ($COLUMN_HASH IN($fullUrlPlaceholders) AND match = 2) OR ($COLUMN_HASH IN($domainPlaceholders) AND match = 1) LIMIT 1"


        readableDatabase.rawQuery(query, (fullUrlHashes + domainHashes).toTypedArray())
            .use { cursor ->
                if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                    return true
                }
            }

        return false
    }

    // This function returns a triple: (isFlagged, threatType, match)
    fun isUrlFlagged(url: String): Triple<Boolean, Alert.ThreatType?, Int> {

        var cleanedUrl = UrlUtils.removeUrlPrefixes(url).lowercase()
        cleanedUrl = removeQueryStringAndFragment(cleanedUrl)
        val domain = extractDomain(url)

        val urlHash = HashUtils.generateSHA256(cleanedUrl)
        val domainHash = HashUtils.generateSHA256(domain)


        // First check for domain
        val domainQuery =
            "SELECT type FROM $TABLE_FLAGGED_URLS WHERE $COLUMN_HASH = ? AND `match` = 1 LIMIT 1"
        readableDatabase.rawQuery(domainQuery, arrayOf(domainHash)).use { cursor ->
            if (cursor.moveToFirst()) {
                val typeValue = cursor.getInt(0)
                return Triple(true, Alert.ThreatType.fromInt(typeValue), 1)
            }
        }
        // Then check for specific URL
        val urlQuery =
            "SELECT type FROM $TABLE_FLAGGED_URLS WHERE $COLUMN_HASH = ? AND `match` = 2 LIMIT 1"
        readableDatabase.rawQuery(urlQuery, arrayOf(urlHash)).use { cursor ->
            if (cursor.moveToFirst()) {
                val typeValue = cursor.getInt(0)
                return Triple(true, Alert.ThreatType.fromInt(typeValue), 2)
            }
        }


        return Triple(false, null, 1)
    }

    private fun countData(
        tableName: String,
        selection: String,
        selectionArgs: Array<String>
    ): Int {
        val query = "SELECT COUNT(*) FROM $tableName WHERE $selection LIMIT 1"

        var count = 0
        readableDatabase.rawQuery(query, selectionArgs).use { cursor ->
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0)
            }
        }


        return count
    }

    fun getRandomTip(): String {
        val cursor =
            readableDatabase.rawQuery(
                "SELECT tip FROM $TABLE_TIPS ORDER BY RANDOM() LIMIT 1",
                null
            )
        var tip = ""
        if (cursor.moveToFirst()) {
            tip = cursor.getString(cursor.getColumnIndexOrThrow("tip"))

        }
        cursor.close()

        return tip
    }


    fun getUserStats(): Stats {
        val cursor =
            readableDatabase.rawQuery("SELECT stat_key, stat_count FROM $TABLE_STATS", null)

        val map = mutableMapOf<String, Int>()
        while (cursor.moveToNext()) {
            val key = cursor.getString(cursor.getColumnIndexOrThrow("stat_key"))
            val value = cursor.getInt(cursor.getColumnIndexOrThrow("stat_count"))
            map[key] = value
        }
        cursor.close()

        return Stats(
            suspiciousLinksDetected = map[STAT_FLAGGED_LINK_DETECTED] ?: 0,
            suspiciousSmsDetected = map[STAT_FLAGGED_SMS_DETECTED] ?: 0,
            suspiciousAppDetected = map[STAT_FLAGGED_APP_DETECTED] ?: 0,
            verifiedGatewayDetected = map[STAT_VERIFIED_GATEWAY] ?: 0
        )
    }

    fun incrementUserStat(statKey: String) {
        val updateQuery =
            "UPDATE $TABLE_STATS SET stat_count = stat_count + 1 WHERE stat_key = ?"
        writableDatabase.execSQL(updateQuery, arrayOf(statKey))
    }

    fun logUpdateHistory(updateType: Int) {
        val values = ContentValues().apply {
            put("type", updateType)
            put("timestamp", System.currentTimeMillis())
        }
        writableDatabase.insert(TABLE_UPDATE_HISTORY, null, values)
    }

    fun logAlertHistory(alertType: Alert.AlertType, param1: String?, param2: String?) {
        val values = ContentValues().apply {
            put("type", alertType.value)
            put("timestamp", System.currentTimeMillis())
        }
        writableDatabase.insert(TABLE_ALERT_HISTORY, null, values)
    }

    fun replaceDatabaseWithFetchedData(dataJsonArray: JSONArray) {
        writableDatabase.transaction {
            try {
                // Clear existing data within the transaction
                clearDatabaseTable(TABLE_TIPS)
                // Populate data
                populateDatabaseWithFetchedData(dataJsonArray)
            } catch (e: Exception) {
                Log.e("DatabaseHelper", "Transaction failed: ${e.message}", e)
                throw e // Roll back transaction
            }
        }
    }

    private fun clearAllDatabaseTables() {
        val tables = arrayOf(
            TABLE_FLAGGED_URLS,
            TABLE_FLAGGED_SENDERS,
            TABLE_FLAGGED_MESSAGES,
            TABLE_FLAGGED_WORDS,
            TABLE_FLAGGED_APPS,
            TABLE_SIDELOAD_TRUSTED_APPS,
            TABLE_TIPS
        )

        tables.forEach { tableName ->
            writableDatabase.execSQL("DELETE FROM $tableName")
            // Reset auto-increment ID
            writableDatabase.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '$tableName'")

        }
    }

    private fun clearDatabaseTable(tableName: String) {
        writableDatabase.execSQL("DELETE FROM $tableName")
        // Reset auto-increment ID
        writableDatabase.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '$tableName'")
    }


}