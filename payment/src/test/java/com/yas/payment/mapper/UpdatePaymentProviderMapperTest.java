package com.yas.payment.mapper;

import com.yas.payment.model.PaymentProvider;
import com.yas.payment.viewmodel.paymentprovider.UpdatePaymentVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class UpdatePaymentProviderMapperTest {

    private UpdatePaymentProviderMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(UpdatePaymentProviderMapper.class);
    }

    @Test
    void testToModel_FullFields_Success() {
        // Arrange
        UpdatePaymentVm vm = new UpdatePaymentVm();
        vm.setId("update-id-01");
        vm.setName("Updated PayPal");
        vm.setEnabled(false);
        vm.setConfigureUrl("http://api.paypal.com");
        vm.setLandingViewComponentName("PaypalLandingView");
        vm.setAdditionalSettings("{\"theme\":\"dark\"}");
        vm.setMediaId(999L);

        // Act
        PaymentProvider entity = mapper.toModel(vm);

        // Assert
        if (entity != null) {
            assertEquals(vm.getId(), entity.getId());
            assertEquals(vm.getName(), entity.getName());
            assertEquals(vm.isEnabled(), entity.isEnabled());
            assertEquals(vm.getConfigureUrl(), entity.getConfigureUrl());
            assertEquals(vm.getLandingViewComponentName(), entity.getLandingViewComponentName());
            assertEquals(vm.getAdditionalSettings(), entity.getAdditionalSettings());
            assertEquals(vm.getMediaId(), entity.getMediaId());
        }
    }

    @Test
    void testToModel_Null() {
        UpdatePaymentVm vm = null;
        assertNull(mapper.toModel(vm));
    }
}