package nu.milad.motmaenbash.utils

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import org.json.JSONObject
import nu.milad.motmaenbash.R

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "motmaenbash.db"
        private const val DATABASE_VERSION = 2


        // Table names
        const val TABLE_SUSPICIOUS_LINKS = "suspicious_links"
        const val TABLE_SUSPICIOUS_SENDERS = "suspicious_senders"
        private const val TABLE_SUSPICIOUS_MESSAGES = "suspicious_messages"
        const val TABLE_SUSPICIOUS_KEYWORDS = "suspicious_keywords"
        private const val TABLE_SUSPICIOUS_APPS = "suspicious_apps"

        private const val TABLE_TIPS = "tips"
        private const val TABLE_USER_STATS = "user_stats"

        // Stats keys
        const val STAT_SUSPICIOUS_LINK_DETECTED = "suspicious_link_detected"
        const val STAT_SUSPICIOUS_SMS_DETECTED = "suspicious_sms_detected"
        const val STAT_SUSPICIOUS_APP_DETECTED = "suspicious_app_detected"
    }


    private val context = context

    override fun onCreate(db: SQLiteDatabase) {
        createTables(db)
        prepopulateData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        dropTables(db)
        createTables(db)
        prepopulateData(db)
    }

    private fun createTables(db: SQLiteDatabase) {
        // Creating phishing_links table
        db?.execSQL(
            """
            CREATE TABLE $TABLE_SUSPICIOUS_LINKS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                url TEXT NOT NULL UNIQUE,
                type INTEGER NOT NULL CHECK(type IN (0, 1, 2)), -- 0: phishing, 1: scam, 2: ponzi
                description TEXT,
                is_phishing INTEGER NOT NULL CHECK(is_phishing IN (0, 1)), -- 0: domain, 1: specific link
                detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """
        )

        // Creating phishing_sms_senders table
        db?.execSQL(
            """
            CREATE TABLE $TABLE_SUSPICIOUS_SENDERS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                sender_number TEXT NOT NULL UNIQUE,
                detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """
        )

        // Creating suspicious_messages table
        db?.execSQL(
            """
            CREATE TABLE $TABLE_SUSPICIOUS_MESSAGES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                sender_number TEXT NOT NULL,
                message TEXT NOT NULL,
                detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE(sender_number, message)
            );
        """
        )

        // Creating suspicious_words table
        db?.execSQL(
            """
            CREATE TABLE $TABLE_SUSPICIOUS_KEYWORDS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                word TEXT NOT NULL UNIQUE
            );
        """
        )


        // Creating suspicious_apps table
        db?.execSQL(
            """
            CREATE TABLE $TABLE_SUSPICIOUS_APPS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                package_name TEXT NOT NULL UNIQUE,
                sha1 TEXT UNIQUE,
                apk_sha1 TEXT UNIQUE,
                detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """
        )

        // Creating tips table if not exists
        db?.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_TIPS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                tip TEXT NOT NULL
            );
        """
        )

        // Creating user_stats table if not exists
        db?.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_USER_STATS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                stat_key TEXT NOT NULL UNIQUE,
                stat_count INTEGER NOT NULL
            );
        """
        )

    }

    private fun dropTables(db: SQLiteDatabase?) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_SUSPICIOUS_LINKS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_SUSPICIOUS_SENDERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_SUSPICIOUS_MESSAGES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_SUSPICIOUS_KEYWORDS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_SUSPICIOUS_APPS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TIPS")
    }

    private fun prepopulateData(db: SQLiteDatabase?) {
        db?.beginTransaction()
        try {
            val inputStream = context.resources.openRawResource(R.raw.data)
            val jsonData = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonData)

            insertData(db, TABLE_SUSPICIOUS_LINKS, jsonObject)
            insertData(db, TABLE_SUSPICIOUS_SENDERS, jsonObject)
            insertData(db, TABLE_SUSPICIOUS_MESSAGES, jsonObject)
            insertData(db, TABLE_SUSPICIOUS_KEYWORDS, jsonObject)
            insertData(db, TABLE_SUSPICIOUS_APPS, jsonObject)
            insertData(db, TABLE_TIPS, jsonObject)
            insertData(db, TABLE_USER_STATS, jsonObject)



            db?.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error prepopulating data: ${e.message}", e)  // Added logging
        } finally {
            db?.endTransaction()
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
                    TABLE_SUSPICIOUS_LINKS -> {
                        val jsonObject = jsonArray.getJSONObject(i)
                        contentValues.apply {
                            put("url", jsonObject.optString("url"))
                            put("type", jsonObject.optInt("type"))
                            put("description", jsonObject.optString("description"))
                            put("is_phishing", jsonObject.optInt("is_phishing"))
                        }
                    }

                    TABLE_SUSPICIOUS_SENDERS -> {
                        val jsonObject = jsonArray.getJSONObject(i)
                        contentValues.apply {
                            put("sender_number", jsonObject.optString("sender_number"))
                        }
                    }

                    TABLE_SUSPICIOUS_MESSAGES -> {
                        val jsonObject = jsonArray.getJSONObject(i)
                        contentValues.apply {
                            put("sender_number", jsonObject.optString("sender_number"))
                            put("message", jsonObject.optString("message"))
                        }
                    }

                    TABLE_SUSPICIOUS_KEYWORDS -> {
                        val word = jsonArray.getString(i)
                        contentValues.apply {
                            put("word", word)
                        }
                    }

                    TABLE_TIPS -> {
                        val word = jsonArray.getString(i)
                        contentValues.apply {
                            put("tip", word)
                        }
                    }

                    TABLE_SUSPICIOUS_APPS -> {
                        val jsonObject = jsonArray.getJSONObject(i)
                        contentValues.apply {
                            put("package_name", jsonObject.optString("package_name"))
                            put("sha1", jsonObject.optString("sha1"))
                            put("apk_sha1", jsonObject.optString("apk_sha1"))
                        }
                    }

                    TABLE_USER_STATS -> {
                        val jsonObject = jsonArray.getJSONObject(i)
                        contentValues.apply {
                            put("stat_key", jsonObject.optString("stat_key"))
                            put("stat_count", jsonObject.optInt("stat_count"))
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

    fun isAppSuspicious(packageName: String, sha1: String, apkSha1: String): Boolean {
        val selection = "package_name = ? OR sha1 = ? OR apk_sha1 = ?"
        val selectionArgs = arrayOf(packageName, sha1, apkSha1)
        val isSuspicious = countData(TABLE_SUSPICIOUS_APPS, selection, selectionArgs) > 0

        if (isSuspicious) {
            incrementUserStat(STAT_SUSPICIOUS_APP_DETECTED)
        }
        return isSuspicious
    }

    fun isUrlSuspicious(url: String): Boolean {
        val selection = "url = ?"
        val selectionArgs = arrayOf(url)
        val isSuspicious = countData(TABLE_SUSPICIOUS_LINKS, selection, selectionArgs) > 0

        if (isSuspicious) {
            incrementUserStat(STAT_SUSPICIOUS_APP_DETECTED)
        }

        return isSuspicious
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
        val cursor = db.rawQuery("SELECT tip FROM tips ORDER BY RANDOM() LIMIT 1", null)
        var randomTip = ""
        if (cursor.moveToFirst()) {
            randomTip = cursor.getString(cursor.getColumnIndexOrThrow("tip"))
        }
        cursor.close()
        return randomTip
    }

    fun getUserStats(): Map<String, Int> {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT stat_key, stat_count FROM user_stats", null)
        val statsMap = mutableMapOf<String, Int>()
        while (cursor.moveToNext()) {
            val statKey = cursor.getString(cursor.getColumnIndexOrThrow("stat_key"))
            val statValue = cursor.getInt(cursor.getColumnIndexOrThrow("stat_count"))
            statsMap[statKey] = statValue
        }
        cursor.close()
        return statsMap
    }


    fun incrementUserStat(statKey: String) {
        val db = writableDatabase
        val updateQuery =
            "UPDATE $TABLE_USER_STATS SET stat_count = stat_count + 1 WHERE stat_key = ?"
        db.execSQL(updateQuery, arrayOf(statKey))
    }

    fun clearDatabase() {
        val db = writableDatabase
        try {
            val tables = arrayOf(
                TABLE_SUSPICIOUS_LINKS,
                TABLE_SUSPICIOUS_SENDERS,
                TABLE_SUSPICIOUS_MESSAGES,
                TABLE_SUSPICIOUS_KEYWORDS,
                TABLE_SUSPICIOUS_APPS,
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