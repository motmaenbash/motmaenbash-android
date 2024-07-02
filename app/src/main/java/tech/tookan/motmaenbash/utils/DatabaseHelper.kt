package tech.tookan.motmaenbash.utils

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import org.json.JSONObject
import tech.tookan.motmaenbash.R
import java.io.BufferedReader
import java.io.InputStreamReader

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "phishing_detection.db"
        private const val DATABASE_VERSION = 2


        // Table names
        const val TABLE_SUSPICIOUS_LINKS = "suspicious_links"
        const val TABLE_SUSPICIOUS_SENDERS = "suspicious_senders"
        private const val TABLE_SUSPICIOUS_MESSAGES = "suspicious_messages"
        const val TABLE_SUSPICIOUS_KEYWORDS = "suspicious_keywords"
        const val TABLE_SUSPICIOUS_APPS = "suspicious_apps"

        private const val TABLE_USER_STATS = "user_stats"
    }


    private val context = context

    override fun onCreate(db: SQLiteDatabase?) {
        createTables(db)
        prepopulateData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        dropTables(db)
        createTables(db)
        prepopulateData(db)
    }

    private fun createTables(db: SQLiteDatabase?) {
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
                detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE(package_name)
            );
        """
        )

        // Creating user_stats table if not exists
        db?.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_USER_STATS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                stat_key TEXT NOT NULL,
                stat_count INTEGER NOT NULL,
                UNIQUE(stat_key)
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
    }

    private fun prepopulateData(db: SQLiteDatabase?) {
        db?.beginTransaction()
        try {
            val inputStream = context.resources.openRawResource(R.raw.data)
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            bufferedReader.forEachLine { line -> stringBuilder.append(line) }
            val jsonData = JSONObject(stringBuilder.toString())



            insertData(db, TABLE_SUSPICIOUS_LINKS, "phishing_links", jsonData)
            insertData(db, TABLE_SUSPICIOUS_SENDERS, "phishing_sms_senders", jsonData)
            insertData(db, TABLE_SUSPICIOUS_MESSAGES, "phishing_sms_messages", jsonData)
            insertData(db, TABLE_SUSPICIOUS_KEYWORDS, "suspicious_words", jsonData)
            insertData(db, TABLE_SUSPICIOUS_APPS, "suspicious_apps", jsonData)
            insertData(db, TABLE_USER_STATS, "user_stats", jsonData)



            db?.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error prepopulating data: ${e.message}", e)  // Added logging
        } finally {
            db?.endTransaction()
        }
    }

    private fun insertData(
        db: SQLiteDatabase?,
        tableName: String,
        jsonKey: String,
        jsonData: JSONObject
    ) {
        val jsonArray = jsonData.optJSONArray(jsonKey) ?: return
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

                    TABLE_SUSPICIOUS_APPS -> {
                        val jsonObject = jsonArray.getJSONObject(i)
                        contentValues.apply {
                            put("package_name", jsonObject.optString("package_name"))
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
                    tableName,
                    null,
                    contentValues,
                    SQLiteDatabase.CONFLICT_IGNORE
                )
            }
            db?.setTransactionSuccessful()
        } finally {
            db?.endTransaction()
        }
    }


    fun isAppSuspicious(packageName: String, sha1: String): Boolean {
        val selection = "package_name = ? OR sha1 = ?"
        val selectionArgs = arrayOf(packageName, sha1)
        return countData(TABLE_SUSPICIOUS_APPS, selection, selectionArgs) > 0
    }

    fun isUrlSuspicious(url: String): Boolean {
        val selection = "url = ?"
        val selectionArgs = arrayOf(url)
        return countData(TABLE_SUSPICIOUS_LINKS, selection, selectionArgs) > 0
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
}