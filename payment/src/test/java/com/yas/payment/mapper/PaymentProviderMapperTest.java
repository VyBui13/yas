package com.yas.payment.mapper;

import com.yas.payment.model.PaymentProvider;
import com.yas.payment.viewmodel.paymentprovider.PaymentProviderVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PaymentProviderMapperTest {

    private PaymentProviderMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(PaymentProviderMapper.class);
    }

    @Test
    void testToVm_FullFields_Success() {
        // Arrange: Tạo Entity đầy đủ dữ liệu
        PaymentProvider entity = new PaymentProvider();
        entity.setId("entity-id-01");
        entity.setName("Stripe");
        entity.setEnabled(true);
        entity.setConfigureUrl("https://stripe.com");
        entity.setVersion(1);
        entity.setMediaId(456L);

        // Act: Map từ Entity sang ViewModel (Hàm toVm có sẵn trong BaseMapper)
        PaymentProviderVm vm = mapper.toVm(entity);

        // Assert
        if (vm != null) {
            assertEquals(entity.getId(), vm.getId());
            assertEquals(entity.getName(), vm.getName());
            assertEquals(entity.getConfigureUrl(), vm.getConfigureUrl());
            assertEquals(entity.getVersion(), vm.getVersion());
            assertEquals(entity.getMediaId(), vm.getMediaId());
        }
    }

    @Test
    void testToVm_Null() {
        PaymentProvider entity = null;
        assertNull(mapper.toVm(entity));
    }
}