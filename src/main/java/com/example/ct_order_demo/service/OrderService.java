package com.example.ct_order_demo.service;

import com.commercetools.api.client.ProjectApiRoot;
import com.commercetools.api.models.cart.*;
import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.payment.Payment;
import com.commercetools.api.models.common.Address;
import com.commercetools.api.models.payment.PaymentResourceIdentifier;
import com.commercetools.api.models.shipping_method.ShippingMethodResourceIdentifier;
import com.commercetools.api.models.product.Product;
import com.example.ct_order_demo.client.InventoryClient;
import com.example.ct_order_demo.model.OrderRequest;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OrderService {

    private final InventoryClient inventoryClient;
    private final ProjectApiRoot apiRoot;

    public OrderService(InventoryClient inventoryClient, ProjectApiRoot apiRoot) {
        this.inventoryClient = inventoryClient;
        this.apiRoot = apiRoot;
    }

    public Order createFullOrder(OrderRequest request) {

        Product product = apiRoot.products()
                .withId(request.productId)
                .get()
                .executeBlocking()
                .getBody();

        var productData = product.getMasterData().getCurrent();

        String sku;

        if (productData.getMasterVariant().getId().equals(request.variantId)) {
            sku = productData.getMasterVariant().getSku();
        } else {
            sku = productData.getVariants()
                    .stream()
                    .filter(v -> v.getId().equals(request.variantId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Variant not found"))
                    .getSku();
        }


        Map<String, Object> inventoryResponse =
                inventoryClient.checkStock(sku, Math.toIntExact(request.quantity));

        Map<String, Object> skuData =
                (Map<String, Object>) inventoryResponse.get(sku);

        Boolean inStock = (Boolean) skuData.get("inStock");

        if (!inStock) {
            throw new RuntimeException("Product is out of stock");
        }

        Cart createdCart = apiRoot.carts()
                .post(builder -> builder.currency(request.currency)
                        .country(request.country)
                        .customerId(request.customerId)
                        .customerEmail(request.email))
                .executeBlocking()
                .getBody();

        Cart cartWithLineItem = apiRoot.carts()
                .withId(createdCart.getId())
                .post(builder -> builder
                        .version(createdCart.getVersion())
                        .plusActions(
                                CartAddLineItemAction.builder()
                                        .productId(request.productId)
                                        .variantId(request.variantId)
                                        .quantity(request.quantity)
                                        .build()
                        ))
                .executeBlocking()
                .getBody();

        Cart cartWithShippingAddress = apiRoot.carts()
                .withId(cartWithLineItem.getId())
                .post(builder -> builder
                        .version(cartWithLineItem.getVersion())
                        .plusActions(
                                CartSetShippingAddressAction.builder()
                                        .address(
                                                Address.builder()
                                                        .country(request.country)
                                                        .firstName(request.firstName)
                                                        .lastName(request.lastName)
                                                        .streetName(request.street)
                                                        .city(request.city)
                                                        .postalCode(request.postalCode)
                                                        .build()
                                        )
                                        .build()
                        ))
                .executeBlocking()
                .getBody();

        Cart cartWithShippingMethod = apiRoot.carts()
                .withId(cartWithShippingAddress.getId())
                .post(builder -> builder
                        .version(cartWithShippingAddress.getVersion())
                        .plusActions(
                                CartSetShippingMethodAction.builder()
                                        .shippingMethod(
                                                ShippingMethodResourceIdentifier.builder()
                                                        .id(request.shippingMethodId)
                                                        .build()
                                        )
                                        .build()
                        ))
                .executeBlocking()
                .getBody();

        Payment payment = apiRoot.payments()
                .post(builder -> builder
                        .amountPlanned(m -> m
                                .currencyCode(request.currency)
                                .centAmount(cartWithShippingMethod.getTotalPrice().getCentAmount()))
                        .paymentMethodInfo(info -> info
                                .paymentInterface("Mock")
                                .method("CreditCard")))
                .executeBlocking()
                .getBody();

        Cart cartWithPayment = apiRoot.carts()
                .withId(cartWithShippingMethod.getId())
                .post(builder -> builder
                        .version(cartWithShippingMethod.getVersion())
                        .plusActions(
                                CartAddPaymentAction.builder()
                                        .payment(
                                                PaymentResourceIdentifier.builder()
                                                        .id(payment.getId())
                                                        .build()
                                        )
                                        .build()
                        ))
                .executeBlocking()
                .getBody();

        return apiRoot.orders()
                .post(builder -> builder
                        .cart(c -> c.id(cartWithPayment.getId()))
                        .version(cartWithPayment.getVersion()))
                .executeBlocking()
                .getBody();
    }
}
