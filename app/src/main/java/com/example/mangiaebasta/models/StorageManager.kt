package com.example.mangiaebasta.models

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@Entity(
    tableName = "menu_images",
    primaryKeys = ["menuId", "imageVersion"]
)
data class MenuImageEntity(
    val menuId: Int,
    val base64: String,
    val imageVersion: Int
)

@Dao
interface MenuImageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuImage(menuImage: MenuImageEntity)

    @Query("SELECT * FROM menu_images WHERE menuId = :menuId AND imageVersion = :imageVersion LIMIT 1")
    suspend fun getMenuImage(menuId: Int, imageVersion: Int): MenuImageEntity?

    @Query("SELECT MAX(imageVersion) FROM menu_images WHERE menuId = :menuId")
    suspend fun getLatestImageVersion(menuId: Int): Int?

    @Transaction
    suspend fun insertOrUpdateMenuImage(newImage: MenuImageEntity) {
        val existing = getMenuImage(newImage.menuId, newImage.imageVersion)
        if (existing == null) {
            insertMenuImage(newImage)
        }
    }

    @Query("SELECT * FROM menu_images")
    fun getAllImages(): Flow<List<MenuImageEntity>>

    @Delete
    suspend fun deleteMenuImage(menuImage: MenuImageEntity)

    @Query("SELECT COUNT(*) FROM menu_images")
    suspend fun getImageCount(): Int
}

@Database(entities = [MenuImageEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun menuImageDao(): MenuImageDao
}

object StorageManager {

    private var database: AppDatabase? = null

    fun initialize(context: Context) {
        if (database == null) {
            database = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "menu_images"
            ).build()
        }
    }

    private val dao: MenuImageDao
        get() {
            return database?.menuImageDao()
                ?: throw IllegalStateException("StorageManager not initialized. Call initialize() first.")
        }

    suspend fun saveMenuImage(menuId: Int, base64Image: String, imageVersion: Int) {
        val menuImage = MenuImageEntity(menuId, base64Image, imageVersion)
        dao.insertOrUpdateMenuImage(menuImage)
    }

    suspend fun getMenuImage(menuId: Int, imageVersion: Int): MenuImageEntity? {
        return dao.getMenuImage(menuId, imageVersion)
    }

    suspend fun getMenuImageBase64(menuId: Int, imageVersion: Int): String? {
        return dao.getMenuImage(menuId, imageVersion)?.base64
    }

    suspend fun getLatestImageVersion(menuId: Int): Int? {
        return dao.getLatestImageVersion(menuId)
    }

    suspend fun hasMenuImage(menuId: Int, imageVersion: Int): Boolean {
        return dao.getMenuImage(menuId, imageVersion) != null
    }

    suspend fun needsUpdate(menuId: Int, newVersion: Int): Boolean {
        val currentVersion = dao.getLatestImageVersion(menuId)
        return currentVersion == null || newVersion > currentVersion
    }

    suspend fun forceInsertMenuImage(menuId: Int, base64Image: String, imageVersion: Int) {
        val menuImage = MenuImageEntity(menuId, base64Image, imageVersion)
        dao.insertMenuImage(menuImage)
    }

    fun getAllImagesFlow(): Flow<List<MenuImageEntity>> {
        return dao.getAllImages()
    }

    suspend fun getAllImages(): List<MenuImageEntity> {
        return dao.getAllImages().first()
    }

    suspend fun deleteMenuImage(menuId: Int, imageVersion: Int) {
        val image = dao.getMenuImage(menuId, imageVersion)
        image?.let { dao.deleteMenuImage(it) }
    }

    suspend fun getImageCount(): Int {
        return dao.getImageCount()
    }

    suspend fun isEmpty(): Boolean {
        return getImageCount() == 0
    }
}