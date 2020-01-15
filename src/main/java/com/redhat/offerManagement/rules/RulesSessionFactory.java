package com.redhat.offerManagement.rules;

import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.dmn.api.core.DMNRuntime;

public class RulesSessionFactory {


    protected static DMNRuntime createDMNRuntime(String sessionName) {
        KieServices kieServices = KieServices.Factory.get();
        ReleaseId releaseId = kieServices.newReleaseId("com.myspace","OfferPrediction","1.0.0-SNAPSHOT");
        KieContainer kieContainer = kieServices.newKieContainer(releaseId);
        DMNRuntime dmnRuntime = kieContainer.newKieSession().getKieRuntime(DMNRuntime.class);
        return dmnRuntime;
    }




}
