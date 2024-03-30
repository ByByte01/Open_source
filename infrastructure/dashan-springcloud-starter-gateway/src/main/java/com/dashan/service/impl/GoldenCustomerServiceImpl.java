package com.dashan.service.impl;


import org.springframework.cloud.gateway.filter.factory.RetryGatewayFilterFactory;
import org.springframework.stereotype.Component;

@Component
public class GoldenCustomerServiceImpl {
    public boolean isGoldenCustomer(String customerId) {
      //  RetryGatewayFilterFactory
        // TODO: Add some AI logic to check is this customer deserves a "golden" status ;^)
        if ( "dashan".equalsIgnoreCase(customerId)) {
            return true;
        }
        else {
            return false;
        }
    }
}
