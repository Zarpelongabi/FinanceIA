package com.financeai.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.financeai.models.Category;
import java.util.List;

@Dao
public interface CategoryDao {
    @Insert
    void insert(Category category);

    @Update
    void update(Category category);

    @Delete
    void delete(Category category);

    @Query("SELECT * FROM categories")
    LiveData<List<Category>> getAllCategories();

    @Query("SELECT * FROM categories")
    List<Category> getAllCategoriesSync();

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    Category getCategoryByName(String name);

    @Query("SELECT * FROM categories WHERE keywords LIKE '%' || :keyword || '%' LIMIT 1")
    Category findCategoryByKeyword(String keyword);
}
