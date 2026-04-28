package com.financeai.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.financeai.models.Meta;
import java.util.List;

@Dao
public interface MetaDao {
    @Insert
    void insert(Meta meta);

    @Update
    void update(Meta meta);

    @Delete
    void delete(Meta meta);

    @Query("SELECT * FROM metas")
    LiveData<List<Meta>> getAllMetas();

    @Query("SELECT * FROM metas")
    List<Meta> getAllMetasSync();

    @Query("SELECT SUM(valorAtual) FROM metas")
    double getTotalMetaAtual();
}
