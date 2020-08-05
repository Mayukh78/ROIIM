package com.example.ROIIM;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SingleUseCustomerTokenRequest {
    private String singleUseCustomerToken;

    public String getSingleUseCustomerToken() {
        return singleUseCustomerToken;
    }

    public void setSingleUseCustomerToken(String singleUseCustomerToken) {
        this.singleUseCustomerToken = singleUseCustomerToken;
    }
}
