package de.tse.predictivegrowth.dao;

import de.tse.predictivegrowth.model.StockData;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;

public interface IexCloudDao {

    @GET("/stock")
    Call<List<StockData>> getStockHistory(@Query("token") final String token,
                                          @Query("") final String stock);
}
