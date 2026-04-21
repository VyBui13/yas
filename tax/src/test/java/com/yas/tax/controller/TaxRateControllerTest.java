package com.yas.tax.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.yas.tax.model.TaxClass;
import com.yas.tax.model.TaxRate;
import com.yas.tax.service.TaxRateService;
import com.yas.tax.viewmodel.taxrate.TaxRateListGetVm;
import com.yas.tax.viewmodel.taxrate.TaxRatePostVm;
import com.yas.tax.viewmodel.taxrate.TaxRateVm;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

@ExtendWith(MockitoExtension.class)
class TaxRateControllerTest {

    @Mock
    private TaxRateService taxRateService;

    @InjectMocks
    private TaxRateController taxRateController;

    private TaxRate taxRate;
    private TaxRatePostVm taxRatePostVm;

    @BeforeEach
    void setUp() {
        TaxClass taxClass = new TaxClass();
        taxClass.setId(1L);
        taxClass.setName("Standard Tax");

        taxRate = new TaxRate();
        taxRate.setId(10L);
        taxRate.setRate(10.5);
        taxRate.setZipCode("70000");
        taxRate.setCountryId(1L);
        taxRate.setStateOrProvinceId(2L);
        taxRate.setTaxClass(taxClass);

        taxRatePostVm = mock(TaxRatePostVm.class);
    }

    @Test
    void getPageableTaxRates_Success() {
        TaxRateListGetVm mockList = mock(TaxRateListGetVm.class);
        when(taxRateService.getPageableTaxRates(0, 10)).thenReturn(mockList);

        ResponseEntity<TaxRateListGetVm> response = taxRateController.getPageableTaxRates(0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockList, response.getBody());
    }

    @Test
    void getTaxRate_Success() {
        TaxRateVm mockVm = mock(TaxRateVm.class);
        when(taxRateService.findById(10L)).thenReturn(mockVm);

        ResponseEntity<TaxRateVm> response = taxRateController.getTaxRate(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockVm, response.getBody());
    }

    @Test
    void createTaxRate_Success() {
        when(taxRateService.createTaxRate(taxRatePostVm)).thenReturn(taxRate);
        
        // Sử dụng một builder thực tế để test việc tạo URI
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();

        ResponseEntity<TaxRateVm> response = taxRateController.createTaxRate(taxRatePostVm, builder);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().id());
        
        // Kiểm tra xem Location Header có đúng với ID của TaxRate vừa tạo không
        assertTrue(response.getHeaders().getLocation().toString().endsWith("/tax-rates/10"));
    }

    @Test
    void updateTaxRate_Success() {
        doNothing().when(taxRateService).updateTaxRate(taxRatePostVm, 10L);

        ResponseEntity<Void> response = taxRateController.updateTaxRate(10L, taxRatePostVm);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(taxRateService).updateTaxRate(taxRatePostVm, 10L);
    }

    @Test
    void deleteTaxRate_Success() {
        doNothing().when(taxRateService).delete(10L);

        ResponseEntity<Void> response = taxRateController.deleteTaxRate(10L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(taxRateService).delete(10L);
    }

    @Test
    void getTaxPercentByAddress_Success() {
        when(taxRateService.getTaxPercent(1L, 2L, 3L, "70000")).thenReturn(10.5);

        ResponseEntity<Double> response = taxRateController.getTaxPercentByAddress(1L, 2L, 3L, "70000");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10.5, response.getBody());
    }

    @Test
    void getBatchTaxPercentsByAddress_Success() {
        TaxRateVm mockVm = mock(TaxRateVm.class);
        when(taxRateService.getBulkTaxRate(List.of(1L, 2L), 3L, 4L, "70000"))
            .thenReturn(List.of(mockVm));

        ResponseEntity<List<TaxRateVm>> response = taxRateController.getBatchTaxPercentsByAddress(
            List.of(1L, 2L), 3L, 4L, "70000"
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }
}