package com.example.ROIIM;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ThreadLocalRandom;

@CrossOrigin
@RestController
public class Controller {

    @Autowired
    private UserRepository userRepository;

    //url link
    final String url="https://api.test.paysafe.com/paymenthub/v1/payments";
    RestTemplate restTemplate= new RestTemplate();

    //below method responsible for processing payment
    @PostMapping("/payment")
    public HttpStatus payment(@RequestBody RequestDetails requestDetails)
    {
        System.out.println("Ref num is "+requestDetails.getMerchantRefNum());
        System.out.println("Token is "+requestDetails.getPaymentHandleToken());

        Token token = new Token(requestDetails.getPaymentHandleToken(), requestDetails.getMerchantRefNum(),requestDetails.getAmount(),requestDetails.getCurrencyCode());

        //save card flow
        if(requestDetails.getCustomerOperation()!=null && requestDetails.getCustomerOperation().equals("ADD"))
        {
            String merchantCustomerId;
            //Below block will generate merchantCustomerId
            if(userRepository.findByEmail( requestDetails.getEmail() ) == null) {
                do{
                    long number = ThreadLocalRandom.current().nextLong(1000000);
                    merchantCustomerId = "ROIIMCustomer" + number;

                }
                while (userRepository.findByMerchantCustomerId(merchantCustomerId) !=  null);

                token.setMerchantCustomerId(merchantCustomerId);
            }

        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization","Basic cHJpdmF0ZS03NzUxOkItcWEyLTAtNWYwMzFjZGQtMC0zMDJkMDIxNDQ5NmJlODQ3MzJhMDFmNjkwMjY4ZDNiOGViNzJlNWI4Y2NmOTRlMjIwMjE1MDA4NTkxMzExN2YyZTFhODUzMTUwNWVlOGNjZmM4ZTk4ZGYzY2YxNzQ4");
        HttpEntity<Token> request = new HttpEntity<Token>(token, headers);
        ResponseEntity<UserEntity> result = restTemplate.postForEntity(url, request, UserEntity.class);

        //if user is registering for first time, below block will save their respective customer ID in db
        if(requestDetails.getCustomerOperation() != null && requestDetails.getCustomerOperation().equals("ADD") && token.getMerchantCustomerId() != null)
        {
            UserEntity userEntity = new UserEntity(requestDetails.getEmail(),result.getBody().getCustomerId(),token.getMerchantCustomerId());
            userRepository.save(userEntity);
        }

        return result.getStatusCode();
    }

    //below method will generate SingleUseCustomerToken
    @PostMapping(path="/token", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public SingleUseCustomerTokenRequest customerIdCheck(@RequestBody RequestDetails requestEmail)
    {
        String email= requestEmail.getEmail();
        if(userRepository.findByEmail(email) == null)
            return null;
        //else block will fetch singleUseCustomerToken from paysafe server
        else
        {
            String id= userRepository.findByEmail(email).getCustomerId();
            String url="https://api.test.paysafe.com/paymenthub/v1/customers/"+id+"/singleusecustomertokens";
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization","Basic cHJpdmF0ZS03NzUxOkItcWEyLTAtNWYwMzFjZGQtMC0zMDJkMDIxNDQ5NmJlODQ3MzJhMDFmNjkwMjY4ZDNiOGViNzJlNWI4Y2NmOTRlMjIwMjE1MDA4NTkxMzExN2YyZTFhODUzMTUwNWVlOGNjZmM4ZTk4ZGYzY2YxNzQ4");
            headers.add("Content-Type","application/json");
            String body= "{ \"paymentTypes\": [\"CARD\"] }";
            HttpEntity<String> request= new HttpEntity<>(body,headers);
            RestTemplate restTemplate= new RestTemplate();
            ResponseEntity<SingleUseCustomerTokenRequest>response=restTemplate.postForEntity(url,request, SingleUseCustomerTokenRequest.class);
            return response.getBody();
        }
    }
}
