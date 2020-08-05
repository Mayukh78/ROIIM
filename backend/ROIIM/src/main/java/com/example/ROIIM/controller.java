package com.example.ROIIM;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ThreadLocalRandom;

@CrossOrigin
@RestController
public class controller {

    @Autowired
    private UserRepository userRepository;

    final String url="https://api.test.paysafe.com/paymenthub/v1/payments";
    RestTemplate restTemplate= new RestTemplate();

    @PostMapping("/payment")
    public HttpStatus payment(@RequestBody RequestDetails requestDetails)
    {
        System.out.println("Ref num is "+requestDetails.getMerchantRefNum());
        System.out.println("Token is "+requestDetails.getPaymentHandleToken());
        System.out.println("Op is "+requestDetails.getCustomerOperation());
        Token token = new Token(requestDetails.getPaymentHandleToken(), requestDetails.getMerchantRefNum(),requestDetails.getAmount(),requestDetails.getCurrencyCode());

//        if(requestDetails.getCustomerOperation() == null)
//            ;
        if(requestDetails.getCustomerOperation()!=null && requestDetails.getCustomerOperation().equals("ADD"))
        {
            if(userRepository.findByEmail( requestDetails.getEmail() ) == null) {
                long number = ThreadLocalRandom.current().nextLong(1000000);
                String merchantCustomerId = "ROIIMCustomer" + number;
                token.setMerchantCustomerId(merchantCustomerId);
            }
            else
            {
                UserEntity userEntity= userRepository.findByEmail(requestDetails.getEmail());
                token.setCustomerId(userEntity.getCustomerId());
            }

        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization","Basic cHJpdmF0ZS03NzUxOkItcWEyLTAtNWYwMzFjZGQtMC0zMDJkMDIxNDQ5NmJlODQ3MzJhMDFmNjkwMjY4ZDNiOGViNzJlNWI4Y2NmOTRlMjIwMjE1MDA4NTkxMzExN2YyZTFhODUzMTUwNWVlOGNjZmM4ZTk4ZGYzY2YxNzQ4");
        HttpEntity<Token> request = new HttpEntity<Token>(token, headers);
        ResponseEntity<UserEntity> result = restTemplate.postForEntity(url, request, UserEntity.class);
        System.out.println(result);
        if(requestDetails.getCustomerOperation() != null && requestDetails.getCustomerOperation().equals("ADD") && token.getMerchantCustomerId() != null)
        {
            UserEntity userEntity = new UserEntity(requestDetails.getEmail(),result.getBody().getCustomerId(),token.getMerchantCustomerId());
            userRepository.save(userEntity);
        }

        return result.getStatusCode();
    }


    @PostMapping(path="/token", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public SingleUseCustomerTokenRequest customerIdCheck(@RequestBody Email requestEmail)
    {
        String email= requestEmail.getEmail();
        if(userRepository.findByEmail(email) == null)
            return null;
        else
        {
            String id= userRepository.findByEmail(email).getCustomerId();
            String url="https://api.test.paysafe.com/paymenthub/v1/customers/"+id+"/singleusecustomertokens";
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization","Basic cHJpdmF0ZS03NzUxOkItcWEyLTAtNWYwMzFjZGQtMC0zMDJkMDIxNDQ5NmJlODQ3MzJhMDFmNjkwMjY4ZDNiOGViNzJlNWI4Y2NmOTRlMjIwMjE1MDA4NTkxMzExN2YyZTFhODUzMTUwNWVlOGNjZmM4ZTk4ZGYzY2YxNzQ4");
            String body="  \"paymentTypes\": [\n" +
                    "    \"CARD\"\n" +
                    "  ]";
            HttpEntity<String> request= new HttpEntity<>(body,headers);
            RestTemplate restTemplate= new RestTemplate();
            ResponseEntity<SingleUseCustomerTokenRequest>response=restTemplate.postForEntity(url,request, SingleUseCustomerTokenRequest.class);
//            return response.getBody().getSingleUseCustomerToken();
            return response.getBody();
        }
    }
}
