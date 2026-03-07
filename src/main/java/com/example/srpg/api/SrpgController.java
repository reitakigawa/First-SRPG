package com.example.srpg.api;

import com.example.srpg.service.BattleResult;
import com.example.srpg.service.ScenarioLoader;
import com.example.srpg.service.SrpgEngineService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/srpg")
public class SrpgController {

    private final ScenarioLoader scenarioLoader;
    private final SrpgEngineService srpgEngineService;

    public SrpgController(ScenarioLoader scenarioLoader, SrpgEngineService srpgEngineService) {
        this.scenarioLoader = scenarioLoader;
        this.srpgEngineService = srpgEngineService;
    }

    @GetMapping("/simulate")
    public BattleResult simulate(@RequestParam(defaultValue = "stage1") String scenario) {
        return srpgEngineService.simulate(scenarioLoader.load(scenario));
    }
}
