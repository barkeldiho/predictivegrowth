package de.tse.predictivegrowth.controller;

import de.tse.predictivegrowth.enumeration.DataProvider;
import de.tse.predictivegrowth.model.StockHistory;
import de.tse.predictivegrowth.model.StockHistorySummary;
import de.tse.predictivegrowth.service.api.StockDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(StockController.STOCK_PATH)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Tag(name = "Stockdata", description = "Endpoint to control stock data.")
public class StockController {

    public static final String STOCK_PATH = "/v1/stocks";

    private final StockDataService stockDataService;

    @Operation(summary = "Method returns a list of summarized information about available stock data.",
            description = "Method returns a list of summarized information about available stock data.")
    @GetMapping(path = StringUtils.EMPTY)
    public List<StockHistorySummary> findAllStockHistorySummaries() {
        return this.stockDataService.findAllStockHistorySummaries();
    }

    @Operation(summary = "Method returns a stock history for a given technical id.",
            description = "Method returns a stock history for a given technical id.")
    @GetMapping(path = "/{id}")
    public StockHistory getStockHistory(@PathVariable final Long id) {
        return this.stockDataService.getStockHistoryById(id);
    }

    @Operation(summary = "Method request a full daily stock history from a provider. Returns a summary on success.",
            description = "Method request a full daily stock history from a provider. Returns a summary on success.")
    @PostMapping(path = StringUtils.EMPTY)
    public StockHistorySummary requestStockHistoryFromProvider(@RequestParam final String stockIdentifier,
                                               @RequestParam final String dataProvider) {
        return this.stockDataService.requestStockHistoryFromProvider(stockIdentifier, DataProvider.valueOf(dataProvider.toUpperCase()));
    }

    @Operation(summary = "Method deletes a stock history with a given technical id.",
            description = "Method deletes a stock history with a given technical id.")
    @DeleteMapping(path = "/{id}")
    @ResponseStatus( code = HttpStatus.NO_CONTENT)
    public void deleteStockHistory(@PathVariable final Long id) {
        this.stockDataService.deleteStockHistory(id);
    }

    // calculate model


    //POST request new stockhistory

    //DELETE stockhistory

    // create new trainingmodel -> triggers analysis
}
