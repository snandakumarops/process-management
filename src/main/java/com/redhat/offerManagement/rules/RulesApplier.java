package com.redhat.offerManagement.rules;

import com.google.gson.Gson;


import com.myspace.offermanagement.customerModel.CustomerModel;
import com.myspace.offermanagement.customerModel.PastHistoryModel;
import com.redhat.offerManagement.CustomerOfferModel;
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



    public String processTransactionDMN(String key, String value) {

        EventStreamModel eventModel = new Gson().fromJson(value,EventStreamModel.class);

         CustomerRepository customerRepository = new JdgCustomerRepository();
         PastHistoryRepository pastHistoryRepository = new JdgPastHistRepository();


        DMNRuntime dmnRuntime = RulesSessionFactory.createDMNRuntime();
        System.out.println(dmnRuntime);
        String namespace = "https://kiegroup.org/dmn/_394B8012-7D73-4D4E-B6A3-088C14D828D9";
        String modelName = "OfferPredictionDMN";
        DMNModel dmnModel = dmnRuntime.getModel(namespace, modelName);
        System.out.println(key.replace("\"",""));
        CustomerModel customerModel = customerRepository.getCustomer(key.replace("\"",""));
        System.out.println(customerModel);
        PastHistoryModel pastHistoryModel = pastHistoryRepository.getPastHist(key.replace("\"",""));

        DMNContext dmnContext = dmnRuntime.newContext();
        dmnContext.set("Age", customerModel.getAge());
        dmnContext.set("Income",customerModel.getIncome());
        dmnContext.set("Customer Class",customerModel.getCustomerClass());
        dmnContext.set("Qualified Purchases",pastHistoryModel.getQualifiedPurchases());
        dmnContext.set("Last Offer Response",pastHistoryModel.getLastOfferResponse());
        dmnContext.set("Current Event",eventModel.getEventValue());

        DMNResult dmnResult = dmnRuntime.evaluateAll(dmnModel, dmnContext);

        DMNDecisionResult resultOffer = dmnResult.getDecisionResultByName("Offer");
        String resultOfferPayload = (String)resultOffer.getResult();

        DMNDecisionResult resultOffer1 = dmnResult.getDecisionResultByName("Customer Segmentation");
        String resultOfferPayload1 = (String)resultOffer1.getResult();

        System.out.println("Customer::"+key+customerModel.getAge()+"::"+customerModel.getIncome()+"::"
                +customerModel.getCustomerClass()+"::"+pastHistoryModel.getQualifiedPurchases()+"::"
                +eventModel.getEventValue()+"::"+pastHistoryModel.getLastOfferResponse()+ resultOfferPayload+"::"+resultOfferPayload1);


            if(!resultOfferPayload.equals("No Active Offers")) {
                CustomerOfferModel customerOfferModel = new CustomerOfferModel();
                customerOfferModel.setCustId(customerModel.getCustId());
                customerOfferModel.setCustAge(customerModel.getAge());
                customerOfferModel.setCustIncome(customerModel.getIncome());
                customerOfferModel.setCustClass(customerModel.getCustomerClass());
                customerOfferModel.setQualifiedPurchases(pastHistoryModel.getQualifiedPurchases());
                customerOfferModel.setLastOfferResponse(pastHistoryModel.getLastOfferResponse());
                customerOfferModel.setCustomerSegmentation(resultOfferPayload1);
                customerOfferModel.setActiveOffers(resultOfferPayload);
                return new Gson().toJson(customerOfferModel);
            }else{
                return null;
            }



    }


}
