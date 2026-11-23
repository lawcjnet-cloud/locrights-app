package com.locrights.app.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: String,
    val title: String,
    val summary: String,
    val contentHtml: String,
    val thumbnailUrl: String?,
    val publishedIso: String,
    val formattedDate: String,
    val authorName: String,
    val categoriesCsv: String,
    val url: String,
    val readingTimeMinutes: Int,
    val savedAtEpochMs: Long
)

@Entity(tableName = "cached_articles")
data class CachedArticleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val summary: String,
    val contentHtml: String,
    val thumbnailUrl: String?,
    val publishedIso: String,
    val formattedDate: String,
    val authorName: String,
    val categoriesCsv: String,
    val url: String,
    val readingTimeMinutes: Int,
    val cachedAtEpochMs: Long
)

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY savedAtEpochMs DESC")
    fun observeAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT id FROM bookmarks")
    fun observeBookmarkIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE id = :id)")
    fun observeIsBookmarked(id: String): Flow<Boolean>

    @Query("SELECT * FROM bookmarks WHERE id = :id LIMIT 1")
    suspend fun getBookmarkById(id: String): BookmarkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: String)
}

@Dao
interface CachedArticleDao {
    @Query("SELECT * FROM cached_articles ORDER BY publishedIso DESC")
    suspend fun getAllCached(): List<CachedArticleEntity>

    @Query("SELECT * FROM cached_articles WHERE id = :id LIMIT 1")
    suspend fun getCachedById(id: String): CachedArticleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(articles: List<CachedArticleEntity>)

    @Query("DELETE FROM cached_articles WHERE id NOT IN (SELECT id FROM cached_articles ORDER BY publishedIso DESC LIMIT 60)")
    suspend fun pruneOldCache()
}

@Database(
    entities = [BookmarkEntity::class, CachedArticleEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LocRightsDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun cachedArticleDao(): CachedArticleDao

    companion object {
        @Volatile
        private var INSTANCE: LocRightsDatabase? = null

        fun getInstance(context: Context): LocRightsDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    LocRightsDatabase::class.java,
                    "loc_rights.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
