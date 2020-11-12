package de.tse.predictivegrowth.service.impl;

import de.tse.predictivegrowth.config.data.IexCloudData;
import de.tse.predictivegrowth.dao.IexCloudDao;
import de.tse.predictivegrowth.service.api.IexCloudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IexCloudServiceImpl extends AbstractRestService<IexCloudDao> implements IexCloudService {

    private final IexCloudDao iexCloudDao;

    private final IexCloudData iexCloudData;

    @Autowired
    public IexCloudServiceImpl(final IexCloudData iexCloudData) {
        super(IexCloudDao.class);
        this.iexCloudData = iexCloudData;

        final String iexCloudUrl = iexCloudData.getBaseUrl() +
                iexCloudData.getApiVersion();

        this.iexCloudDao = this.getRestInterface(iexCloudUrl);
    }

    public String test() {
        this.iexCloudDao.getStockHistory();
    }
}
