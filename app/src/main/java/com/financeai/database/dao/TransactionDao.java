package com.financeai.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.financeai.models.Transaction;
import java.util.List;

@Dao
public interface TransactionDao {
    @Insert
    void insert(Transaction transaction);

    @Update
    void update(Transaction transaction);

    @Delete
    void delete(Transaction transaction);

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    LiveData<List<Transaction>> getAllTransactions();

    @Query("SELECT * FROM transactions WHERE date BETWEEN :start AND :end ORDER BY amount DESC")
    LiveData<List<com.financeai.models.Transaction>> getTransactionsByPeriod(long start, long end);

    @Query("SELECT * FROM transactions WHERE date BETWEEN :start AND :end")
    List<com.financeai.models.Transaction> getTransactionsByPeriodSync(long start, long end);

    @Query("SELECT * FROM transactions WHERE date >= :startDate")
    LiveData<List<Transaction>> getTransactionsSince(long startDate);

    @Query("SELECT SUM(amount) FROM transactions WHERE date BETWEEN :start AND :end AND isExpense = 0")
    double getTotalIncomeByPeriod(long start, long end);

    @Query("SELECT SUM(amount) FROM transactions WHERE date BETWEEN :start AND :end AND isExpense = 1")
    double getTotalExpenseByPeriod(long start, long end);

    @Query("SELECT SUM(amount) FROM transactions WHERE date BETWEEN :start AND :end AND isExpense = 1 AND categoryName NOT IN ('Poupança', 'Investimento')")
    double getTotalRealExpenseByPeriod(long start, long end);

    @Query("SELECT COUNT(*) FROM transactions WHERE date BETWEEN :start AND :end")
    int getExpenseCountByPeriod(long start, long end);

    @Query("SELECT * FROM transactions WHERE categoryName = :categoryName AND date BETWEEN :start AND :end LIMIT 1")
    com.financeai.models.Transaction getIncomeTransaction(String categoryName, long start, long end);

    @Query("SELECT categoryName as categoryName, SUM(amount) as total FROM transactions WHERE date BETWEEN :start AND :end AND isExpense = 1 GROUP BY categoryName ORDER BY total DESC")
    List<CategorySummary> getCategoryExpensesSummary(long start, long end);

    class CategorySummary {
        public String categoryName;
        public double total;
    }
}
