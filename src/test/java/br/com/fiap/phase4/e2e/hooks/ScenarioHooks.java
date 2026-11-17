package br.com.fiap.phase4.e2e.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

public class ScenarioHooks {

    private long scenarioStartTime;

    @Before
    public void beforeScenario(Scenario scenario) {
        scenarioStartTime = System.currentTimeMillis();
        System.out.println("================================================================================");
        System.out.printf("[SCENARIO START] %s%n", scenario.getName());
        System.out.printf("[SCENARIO TAGS]  %s%n", scenario.getSourceTagNames());
        System.out.println("================================================================================");
    }

    @After
    public void afterScenario(Scenario scenario) {
        long durationMs = System.currentTimeMillis() - scenarioStartTime;
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("[SCENARIO END]   %s - Status: %s (Duration: %d ms)%n",
                scenario.getName(),
                scenario.getStatus(),
                durationMs);
        if (scenario.isFailed()) {
            System.err.printf("[SCENARIO FAILED] %s failed!%n", scenario.getName());
        }
        System.out.println("================================================================================");
    }
}
