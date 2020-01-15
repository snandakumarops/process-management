package com.redhat.offerManagement.rules;

import com.google.gson.Gson;


import com.myspace.offermanagement.customerModel.CustomerModel;
import com.myspace.offermanagement.customerModel.PastHistoryModel;
import com.redhat.offerManagement.EventStreamModel;
import com.redhat.offerManagement.JdgCustomerRepository;
import com.redhat.offermanagement.CustomerRepository;
import com.redhat.offermanagement.JdgPastHistRepository;
import com.redhat.offermanagement.PastHistoryRepository;
import org.kie.api.runtime.ClassObjectFilter;
import org.kie.api.runtime.KieSession;
import org.kie.dmn.api.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;


public class RulesApplier {


    private static final Logger LOGGER = LoggerFactory.getLogger(RulesApplier.class);


    /**
     * Applies the loaded Drools rules to a given String.
     *
     * @param
     * @return the String after the rule has been applied
     */



    public String processTransactionDMN(String key,String value) {

        EventStreamModel eventModel = new Gson().fromJson(value,EventStreamModel.class);

         CustomerRepository customerRepository = new JdgCustomerRepository();
         PastHistoryRepository pastHistoryRepository = new JdgPastHistRepository();


        DMNRuntime dmnRuntime = RulesSessionFactory.createDMNRuntime("");
        String namespace = "https://kiegroup.org/dmn/_394B8012-7D73-4D4E-B6A3-088C14D828D9";
        String modelName = "OfferPredictionDMN";
        DMNModel dmnModel = dmnRuntime.getModel(namespace, modelName);

        CustomerModel customerModel = customerRepository.getCustomer(key);
        PastHistoryModel pastHistoryModel = pastHistoryRepository.getPastHist(key);

        DMNContext dmnContext = dmnRuntime.newContext();
        dmnContext.set("Age", customerModel.getAge());
        dmnContext.set("Income",customerModel.getIncome());
        dmnContext.set("Customer Class",customerModel.getCustomerClass());
        dmnContext.set("Qualified Purchases",pastHistoryModel.getQualifiedPurchases());
        dmnContext.set("Last Offer Response",pastHistoryModel.getLastOfferResponse());
        dmnContext.set("Current Event",eventModel.getEventValue());

        DMNResult dmnResult = dmnRuntime.evaluateAll(dmnModel, dmnContext);
        DMNDecisionResult resultOffer = dmnResult.getDecisionResultById("Offer");
        String resultOfferPayload = (String)resultOffer.getResult();
        System.out.println("Results::"+dmnResult.getDecisionResults()+":::" +resultOfferPayload);
        return resultOfferPayload;
    }


}
