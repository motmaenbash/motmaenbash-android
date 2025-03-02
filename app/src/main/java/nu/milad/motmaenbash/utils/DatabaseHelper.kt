package nu.milad.motmaenbash.utils

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.consts.AppConstants.COLUMN_HASH
import nu.milad.motmaenbash.consts.AppConstants.DATABASE_NAME
import nu.milad.motmaenbash.consts.AppConstants.DATABASE_VERSION
import nu.milad.motmaenbash.consts.AppConstants.STAT_FLAGGED_APP_DETECTED
import nu.milad.motmaenbash.consts.AppConstants.STAT_FLAGGED_LINK_DETECTED
import nu.milad.motmaenbash.consts.AppConstants.STAT_FLAGGED_SMS_DETECTED
import nu.milad.motmaenbash.consts.AppConstants.TABLE_FLAGGED_APPS
import nu.milad.motmaenbash.consts.AppConstants.TABLE_FLAGGED_LINKS
import nu.milad.motmaenbash.consts.AppConstants.TABLE_FLAGGED_MESSAGES
import nu.milad.motmaenbash.consts.AppConstants.TABLE_FLAGGED_SENDERS
import nu.milad.motmaenbash.consts.AppConstants.TABLE_FLAGGED_WORDS
import nu.milad.motmaenbash.consts.AppConstants.TABLE_STATS
import nu.milad.motmaenbash.consts.AppConstants.TABLE_TIPS
import nu.milad.motmaenbash.utils.SmsUtils.generateNormalizedMessageHash
import org.json.JSONObject

class DatabaseHelper(appContext: Context) :
    SQLiteOpenHelper(appContext, DATABASE_NAME, null, DATABASE_VERSION) {


    private val context = appContext

    override fun onCreate(db: SQLiteDatabase) {
        createTables(db)
        prepopulateData(db)
        prepopulateStatsData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        when (oldVersion) {
            1 -> {
                // Upgrade from version 1 to 2


            }

            2 -> {

            }
        }
        dropTables(db)
        createTables(db)
        prepopulateData(db)
    }

    private fun createTables(db: SQLiteDatabase) {


        // Creating flagged links table
        db.execSQL(
            """
                CREATE TABLE $TABLE_FLAGGED_LINKS (
                    hash TEXT NOT NULL UNIQUE,
                    type INTEGER NOT NULL CHECK(type IN (1, 2, 3)), -- 1: phishing, 2: scam, 3: ponzi
                    description TEXT,
                    check_type INTEGER NOT NULL CHECK(check_type IN (1, 2)), -- 1: domain, 2: specific link
                    alert_level INTEGER NOT NULL CHECK(alert_level IN (1,2,3))) -- '1:alert', '2:warning', '3:info'
            """
        )
        // Creating index
        db.execSQL("CREATE INDEX index_flagged_links_hash ON $TABLE_FLAGGED_LINKS(hash);")

        // Creating flagged sms senders table
        db.execSQL(
            """
                CREATE TABLE $TABLE_FLAGGED_SENDERS (
                    hash TEXT NOT NULL UNIQUE
                );
            """
        )
        // Creating index
        db.execSQL("CREATE INDEX index_flagged_senders_hash ON $TABLE_FLAGGED_SENDERS(hash);")


        // Creating flagged sms messages table
        db.execSQL(
            """
                CREATE TABLE $TABLE_FLAGGED_MESSAGES (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    hash TEXT NOT NULL UNIQUE
                );
            """
        )
        // Creating index
        db.execSQL("CREATE INDEX index_flagged_messages_hash ON $TABLE_FLAGGED_MESSAGES(hash);")


        // Creating flagged sms keywords table
        db.execSQL(
            """
                CREATE TABLE $TABLE_FLAGGED_WORDS (
                    hash TEXT NOT NULL UNIQUE
                );
            """
        )
        // Creating index
        db.execSQL("CREATE INDEX index_flagged_words ON $TABLE_FLAGGED_WORDS(hash);")


        // Creating flagged apps table
        db.execSQL(
            """
                CREATE TABLE $TABLE_FLAGGED_APPS (
                    package_name TEXT NOT NULL UNIQUE,
                    sha1 TEXT UNIQUE,
                    apk_sha1 TEXT UNIQUE
                );
            """
        )
        // Creating index //todo: add index
        db.execSQL("CREATE INDEX index_flagged_apps_package_name ON $TABLE_FLAGGED_APPS(package_name);")
        db.execSQL("CREATE INDEX index_flagged_apps_sha1 ON $TABLE_FLAGGED_APPS(sha1);")
        db.execSQL("CREATE INDEX index_flagged_apps_apk_sha1 ON $TABLE_FLAGGED_APPS(apk_sha1);")

        // Creating tips table if not exists
        db.execSQL(
            """
                CREATE TABLE IF NOT EXISTS $TABLE_TIPS (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    tip TEXT NOT NULL
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
        db.execSQL("CREATE INDEX index_stats_stat_key ON $TABLE_STATS(stat_key);")

    }

    private fun dropTables(db: SQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FLAGGED_LINKS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FLAGGED_SENDERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FLAGGED_MESSAGES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FLAGGED_WORDS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FLAGGED_APPS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TIPS")
    }

    private fun prepopulateData(db: SQLiteDatabase?) {
        db?.beginTransaction()
        try {
            val inputStream = context.resources.openRawResource(R.raw.data)
            val jsonData = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonData)

            insertData(db, TABLE_FLAGGED_LINKS, jsonObject)
            insertData(db, TABLE_FLAGGED_SENDERS, jsonObject)
            insertData(db, TABLE_FLAGGED_MESSAGES, jsonObject)
            insertData(db, TABLE_FLAGGED_WORDS, jsonObject)
            insertData(db, TABLE_FLAGGED_APPS, jsonObject)
            insertData(db, TABLE_TIPS, jsonObject)




            db?.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error prepopulating data: ${e.message}", e)  // Added logging
        } finally {
            db?.endTransaction()
        }
    }


    private fun prepopulateStatsData(db: SQLiteDatabase?) {

        val insertQuery = """
        INSERT OR IGNORE INTO $TABLE_STATS (stat_key, stat_count) VALUES
            ('$STAT_FLAGGED_LINK_DETECTED', 0),
            ('$STAT_FLAGGED_SMS_DETECTED', 0),
            ('$STAT_FLAGGED_APP_DETECTED', 0)
    """
        db?.execSQL(insertQuery)


    }


    fun populateDatabaseWithFetchedData(jsonObject: JSONObject) {
        val tables = arrayOf(
            TABLE_FLAGGED_LINKS,
            TABLE_FLAGGED_SENDERS,
            TABLE_FLAGGED_MESSAGES,
            TABLE_FLAGGED_WORDS,
            TABLE_FLAGGED_APPS,
            TABLE_TIPS
        )

        tables.forEach { tableName ->
            val jsonArray = jsonObject.optJSONArray(tableName)
            if (jsonArray != null && jsonArray.length() > 0) {
                insertData(writableDatabase, tableName, jsonObject)
            }
        }
    }

    private fun insertData(
        db: SQLiteDatabase?, tableName: String, jsonData: JSONObject
    ) {
        val jsonArray = jsonData.optJSONArray(tableName) ?: return
        db?.beginTransaction()
        try {
            for (i in 0 until jsonArray.length()) {


                val contentValues = ContentValues()

                when (tableName) {
                    TABLE_FLAGGED_LINKS -> {
                        val jsonObject = jsonArray.getJSONObject(i)
                        contentValues.apply {
                            put("hash", jsonObject.optString("hash"))
                            put("type", jsonObject.optInt("type"))
                            put("description", jsonObject.optString("description"))
                            put("check_type", jsonObject.optInt("check_type"))
                            put("alert_level", jsonObject.optInt("alert_level"))
                        }
                    }


                    TABLE_FLAGGED_SENDERS, TABLE_FLAGGED_WORDS, TABLE_FLAGGED_MESSAGES -> {

                        contentValues.put("hash", jsonArray.getString(i))
                    }


                    TABLE_TIPS -> {
                        val tip = jsonArray.getString(i)
                        contentValues.apply {
                            put("tip", tip)
                        }
                    }

                    TABLE_FLAGGED_APPS -> {
                        val jsonObject = jsonArray.getJSONObject(i)
                        contentValues.apply {
                            put("package_name", jsonObject.optString("package_name"))

                            put("sha1", jsonObject.optString("sha1", null))
                            put("apk_sha1", jsonObject.optString("apk_sha1", null))
                        }
                    }


                }

                db?.insertWithOnConflict(
                    tableName, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE
                )
            }
            db?.setTransactionSuccessful()
        } finally {
            db?.endTransaction()
        }
    }

    fun isAppFlagged(packageName: String, sha1: String, apkSha1: String): Boolean {
        val selection = "package_name = ? OR sha1 = ? OR apk_sha1 = ?"
        val selectionArgs = arrayOf(packageName, sha1, apkSha1)
        val isFlagged = countData(TABLE_FLAGGED_APPS, selection, selectionArgs) > 0

        if (isFlagged) {
            incrementUserStat(STAT_FLAGGED_APP_DETECTED)
        }
        return isFlagged
    }


    fun isSenderFlagged(sender: String): Boolean {


        val hash = HashUtils.generateSHA256(sender)

        val selection = "$COLUMN_HASH = ?"
        val selectionArgs = arrayOf(hash)
        val isFlagged = countData(TABLE_FLAGGED_SENDERS, selection, selectionArgs) > 0
        if (isFlagged) {
            incrementUserStat(STAT_FLAGGED_SMS_DETECTED) //Increment stat
        }

        return false
    }


    fun hasFlaggedWord(message: String): Boolean {

        val words = message.split("\\s+".toRegex()).map { it.trim() }

        Log.d("DatabaseHelper", "Words: $words")
        // Generate hashes for each word
        val wordHashes = words.map { HashUtils.generateSHA256(it) }
        Log.d("DatabaseHelper", "Word hashes: $wordHashes")

        // Prepare placeholders for SQL query
        val placeholders = wordHashes.joinToString(",") { "?" }


        val db = readableDatabase

        // Query to check if any word hash exists in the flagged words table
        val query =
            "SELECT COUNT(*) FROM $TABLE_FLAGGED_WORDS WHERE $COLUMN_HASH IN ($placeholders) LIMIT 1"


        Log.d("DatabaseHelper", "Query: $query")

        db.rawQuery(query, wordHashes.toTypedArray()).use { cursor ->
            if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                incrementUserStat(STAT_FLAGGED_SMS_DETECTED)
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
        if (isFlagged) {
            incrementUserStat(STAT_FLAGGED_SMS_DETECTED) //Increment stat
        }

        return isFlagged
    }

    fun hasFlaggedLink(links: List<String>): Boolean {
        val db = readableDatabase

        val fullUrlHashes = mutableListOf<String>()
        val domainHashes = mutableListOf<String>()

        links.forEach { link ->
            val cleanedLink = UrlUtils.removeUrlPrefixes(link)
            val domain = UrlUtils.extractDomain(link)

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
            "SELECT COUNT(*) FROM $TABLE_FLAGGED_LINKS WHERE ($COLUMN_HASH IN($fullUrlPlaceholders) AND check_type = 2) OR ($COLUMN_HASH IN($domainPlaceholders) AND check_type = 1) LIMIT 1"


        db.rawQuery(query, (fullUrlHashes + domainHashes).toTypedArray()).use { cursor ->
            if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                incrementUserStat(STAT_FLAGGED_SMS_DETECTED)
                incrementUserStat(STAT_FLAGGED_LINK_DETECTED)
                return true
            }
        }

        return false
    }

    fun isUrlFlagged(url: String): Boolean {

        val db = readableDatabase

        var cleanedLink = UrlUtils.removeUrlPrefixes(url).lowercase()
        cleanedLink = UrlUtils.removeQueryString(cleanedLink)
        val domain = UrlUtils.extractDomain(url)

        val fullUrlHash = HashUtils.generateSHA256(cleanedLink)
        val domainHash = HashUtils.generateSHA256(domain)

        val query = """
        SELECT COUNT(*) FROM $TABLE_FLAGGED_LINKS 
        WHERE (($COLUMN_HASH = ? AND check_type = 2) OR 
               ($COLUMN_HASH = ? AND check_type = 1)) 
        LIMIT 1
    """

        db.rawQuery(query, arrayOf(fullUrlHash, domainHash)).use { cursor ->
            if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                incrementUserStat(STAT_FLAGGED_LINK_DETECTED)
                return true
            }
        }

        return false
    }

    private fun countData(tableName: String, selection: String, selectionArgs: Array<String>): Int {
        val db = readableDatabase
        val query = "SELECT COUNT(*) FROM $tableName WHERE $selection LIMIT 1"

        var count = 0
        db.rawQuery(query, selectionArgs).use { cursor ->
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0)
            }
        }


        return count
    }

    fun getRandomTip(): String {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT tip FROM $TABLE_TIPS ORDER BY RANDOM() LIMIT 1", null)
        var randomTip = ""
        if (cursor.moveToFirst()) {
            randomTip = cursor.getString(cursor.getColumnIndexOrThrow("tip"))

        }
        cursor.close()
        
        return randomTip
    }

    fun getUserStats(): Map<String, Int> {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT stat_key, stat_count FROM $TABLE_STATS", null)
        val statsMap = mutableMapOf<String, Int>()
        while (cursor.moveToNext()) {
            val statKey = cursor.getString(cursor.getColumnIndexOrThrow("stat_key"))
            val statValue = cursor.getInt(cursor.getColumnIndexOrThrow("stat_count"))
            statsMap[statKey] = statValue
        }
        cursor.close()
        return statsMap
    }


    private fun incrementUserStat(statKey: String) {
        val db = writableDatabase
        val updateQuery = "UPDATE $TABLE_STATS SET stat_count = stat_count + 1 WHERE stat_key = ?"
        db.execSQL(updateQuery, arrayOf(statKey))
    }

    fun clearDatabase() {
        val db = writableDatabase
        try {
            val tables = arrayOf(
                TABLE_FLAGGED_LINKS,
                TABLE_FLAGGED_SENDERS,
                TABLE_FLAGGED_MESSAGES,
                TABLE_FLAGGED_WORDS,
                TABLE_FLAGGED_APPS,
                TABLE_TIPS
            )

            tables.forEach { tableName ->
                db.execSQL("DELETE FROM $tableName")
                // Reset auto-increment ID
                db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '$tableName'")

            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }
    }


}