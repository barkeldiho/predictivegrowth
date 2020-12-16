package de.tse.predictivegrowth.dao.extern;

import de.tse.predictivegrowth.entity.extern.AlphaVantageStockWrapper;
import de.tse.predictivegrowth.enumeration.AlphaVantageFunction;
import de.tse.predictivegrowth.enumeration.AlphaVantageOutputSize;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AlphaVantageDao {

    @GET("query")
    Call<AlphaVantageStockWrapper> getStockHistory(@Query("function") final AlphaVantageFunction function,
                                                   @Query("symbol") final String stockIdentifier,
                                                   @Query("outputsize") final AlphaVantageOutputSize outputSize,
                                                   @Query("apikey") final String apiKey);
}
