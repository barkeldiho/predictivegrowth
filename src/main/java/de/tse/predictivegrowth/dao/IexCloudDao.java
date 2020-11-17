package de.tse.predictivegrowth.dao;

import de.tse.predictivegrowth.model.StockDataDay;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.time.LocalDate;
import java.util.List;

public interface IexCloudDao {

    @GET("/stock/{stockIdentifier}/chart/{timespan}")
    Call<List<StockDataDay>> getStockHistory(@Path("stockIdentifier") final String stockIdentifier,
                                             @Path("timespan") final String timespan,
                                             @Query("token") final String token);

    @GET("/stock/{stockIdentifier}/intraday-prices")
    Call<List<StockDataDay>> getCurrentIntradayPrices(@Path("stockIdentifier") final String stockIdentifier,
                                                      @Query("token") final String token);

    // date format yyyyMMdd
    @GET("/stock/{stockIdentifier}/chart/{date}")
    Call<List<StockDataDay>> getHistoricIntradayPrices(@Path("stockIdentifier") final String stockIdentifier,
                                                       @Path("date") final LocalDate date,
                                                       @Query("token") final String token);
}
