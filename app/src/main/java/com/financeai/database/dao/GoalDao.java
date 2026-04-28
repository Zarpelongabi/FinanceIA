package com.financeai.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.financeai.models.Goal;
import java.util.List;

@Dao
public interface GoalDao {
    @Insert
    void insert(Goal goal);

    @Update
    void update(Goal goal);

    @Delete
    void delete(Goal goal);

    @Query("SELECT * FROM goals")
    LiveData<List<Goal>> getAllGoals();
}
