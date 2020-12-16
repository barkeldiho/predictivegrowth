package de.tse.predictivegrowth.dao.extern;

import de.tse.predictivegrowth.entity.extern.IexStockDayData;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;

public interface IexCloudDao {

    @GET("stock/{stockIdentifier}/chart/{timespan}")
    Call<List<IexStockDayData>> getListOfIexStockDayData(@Path("stockIdentifier") final String stockIdentifier,
                                                         @Path("timespan") final String timespan,
                                                         @Query("token") final String token);
}
