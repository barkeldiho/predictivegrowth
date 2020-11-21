package de.tse.predictivegrowth.controller;

import de.tse.predictivegrowth.service.api.DeepJavaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DeepJavaController {

    private final DeepJavaService deepJavaService;

    public String trainModelForStock(final Long id) {
        this.deepJavaService
    }
}
