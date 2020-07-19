function checkout() {
    let API_KEY="cHVibGljLTc3NTE6Qi1xYTItMC01ZjAzMWNiZS0wLTMwMmQwMjE1MDA4OTBlZjI2MjI5NjU2M2FjY2QxY2I0YWFiNzkwMzIzZDJmZDU3MGQzMDIxNDUxMGJjZGFjZGFhNGYwM2Y1OTQ3N2VlZjEzZjJhZjVhZDEzZTMwNDQ=";
    paysafe.checkout.setup(API_KEY, {
        "currency": "USD",
        "amount": 10000,
        "locale": "en_US",
        "customer": {
            "firstName": "John",
            "lastName": "Dee",
            "email": "johndee@paysafe.com",
            "phone": "1234567890",
            "dateOfBirth": {
                "day": 1,
                "month": 7,
                "year": 1990
            }
        },
        "billingAddress": {
            "nickName": "John Dee",
            "street": "20735 Stevens Creek Blvd",
            "street2": "Montessori",
            "city": "Cupertino",
            "zip": "95014",
            "country": "US",
            "state": "CA"
        },
        "environment": "TEST",
        "merchantRefNum": "1559900597607",
        "canEditAmount": true,
        "merchantDescriptor": {   
            "dynamicDescriptor": "XYZ",
            "phone": "1234567890"
            },
        "displayPaymentMethods":["card"],
        "paymentMethodDetails": {
            "paysafecard": {
                "consumerId": "1232323"
            },
            "paysafecash": {
                "consumerId": "123456"
            },
            "sightline": {
                "consumerId": "123456",
                "SSN": "123456789",
                "last4ssn": "6789",
                "accountId":"1009688222"
            },
            "vippreferred":{
                "consumerId": "550726575",
                "accountId":"1679688456"
            }
        }
    }, function(instance, error, result) {
        if (result && result.paymentHandleToken) {
            console.log(result.paymentHandleToken);
            // console.log(result.id);
            console.log(result);
            console.log(instance);
            // make AJAX call to Payments API
            let http = new XMLHttpRequest;
            http.open('GET',`https://api.test.paysafe.com/paymenthub/v1/payments/${result.id}`,true);
            http.send();

            http.onreadystatechange = function(){
                if(http.readyState==4 && http.status==200)
                    console.log(JSON.parse(http.response));
            }
        } else {
            console.error(error);
            // Handle the error
        }
    }, function(stage, expired) {
        switch(stage) {
            case "PAYMENT_HANDLE_NOT_CREATED": // Handle the scenario
            case "PAYMENT_HANDLE_CREATED": // Handle the scenario
            case "PAYMENT_HANDLE_REDIRECT": // Handle the scenario
            case "PAYMENT_HANDLE_PAYABLE": // Handle the scenario
            default: // Handle the scenario
        }
    });
}