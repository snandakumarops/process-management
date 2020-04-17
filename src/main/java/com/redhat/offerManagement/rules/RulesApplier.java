package com.redhat.offerManagement.rules;

import com.google.gson.Gson;
import com.redhat.offerManagement.CustomerOfferModel;
import com.redhat.offerManagement.EventStreamModel;
import org.kie.dmn.api.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;


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




        DMNRuntime dmnRuntime = RulesSessionFactory.createDMNRuntime();
        System.out.println(dmnRuntime);
        String namespace = "https://kiegroup.org/dmn/_394B8012-7D73-4D4E-B6A3-088C14D828D9";
        String modelName = "OfferPredictionDMN";
        DMNModel dmnModel = dmnRuntime.getModel(namespace, modelName);
        System.out.println(key.replace("\"",""));

        CustomerOfferModel customerOfferModel = fetchCustomerContext(key.replace("\"",""),eventModel.getEventValue());

        DMNContext dmnContext = dmnRuntime.newContext();
        dmnContext.set("Customer Segmentation", customerOfferModel.getCustomerSegmentation());
        dmnContext.set("Customer Class",customerOfferModel.getCustClass());
        dmnContext.set("Qualified Purchases",customerOfferModel.getQualifiedPurchases());
        dmnContext.set("Current Event",eventModel.getEventValue());

        DMNResult dmnResult = dmnRuntime.evaluateAll(dmnModel, dmnContext);

        DMNDecisionResult resultOffer = dmnResult.getDecisionResultByName("Offer");
        String resultOfferPayload = (String)resultOffer.getResult();

        System.out.println("Before Evaluation"+customerOfferModel);

            if(!resultOfferPayload.equals("No Active Offers")) {
                customerOfferModel.setActiveOffers(resultOfferPayload);
                System.out.println("After Evaluation"+customerOfferModel);
                return new Gson().toJson(customerOfferModel);
            }else{
                return null;
            }



    }

    public CustomerOfferModel fetchCustomerContext(String custId, String eventTyoe) {


        String urlString = "http://predictionservice-customer-event-context.apps.cluster-flrda-91e7.flrda-91e7.example.opentlc.com/camel/customer-context?custId="+custId+"&eventType="+eventTyoe;
        System.out.println(urlString);
        CustomerOfferModel customerOfferModel = new CustomerOfferModel();
        try {

            URL url = new URL(urlString);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode()+conn.getErrorStream());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }

            Map map = new Gson().fromJson(output, Map.class);

            customerOfferModel.setCustId((String) map.get("custId"));
            customerOfferModel.setCustomerSegmentation((String) map.get("prediction"));
            customerOfferModel.setQualifiedPurchases((String) map.get("qualifiedPurchases"));
            customerOfferModel.setCustClass((String) map.get("customerClass"));
            customerOfferModel.setCustAge((Double) map.get("age"));


            conn.disconnect();

        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }
        return customerOfferModel;

    }


}
