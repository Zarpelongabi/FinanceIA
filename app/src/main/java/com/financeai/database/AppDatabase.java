package com.financeai.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.financeai.database.dao.CategoryDao;
import com.financeai.database.dao.MetaDao;
import com.financeai.database.dao.TransactionDao;
import com.financeai.models.Category;
import com.financeai.models.Meta;
import com.financeai.models.Transaction;
import com.financeai.utils.Converters;

@Database(entities = {Transaction.class, Category.class, Meta.class}, version = 6, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract TransactionDao transactionDao();
    public abstract MetaDao metaDao();
    public abstract CategoryDao categoryDao();

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "finance_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
