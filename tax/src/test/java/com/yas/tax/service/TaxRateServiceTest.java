package com.yas.tax.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.tax.model.TaxClass;
import com.yas.tax.model.TaxRate;
import com.yas.tax.repository.TaxClassRepository;
import com.yas.tax.repository.TaxRateRepository;
import com.yas.tax.viewmodel.location.StateOrProvinceAndCountryGetNameVm;
import com.yas.tax.viewmodel.taxrate.TaxRateListGetVm;
import com.yas.tax.viewmodel.taxrate.TaxRatePostVm;
import com.yas.tax.viewmodel.taxrate.TaxRateVm;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class TaxRateServiceTest {

    @Mock
    private TaxRateRepository taxRateRepository;

    @Mock
    private TaxClassRepository taxClassRepository;

    @Mock
    private LocationService locationService;

    @InjectMocks
    private TaxRateService taxRateService;

    private TaxRatePostVm taxRatePostVm;
    private TaxRate taxRate;
    private TaxClass taxClass;

    @BeforeEach
    void setUp() {
        taxRatePostVm = mock(TaxRatePostVm.class);
        lenient().when(taxRatePostVm.taxClassId()).thenReturn(1L);
        lenient().when(taxRatePostVm.rate()).thenReturn(10.0);
        lenient().when(taxRatePostVm.zipCode()).thenReturn("70000");
        lenient().when(taxRatePostVm.stateOrProvinceId()).thenReturn(2L);
        lenient().when(taxRatePostVm.countryId()).thenReturn(3L);

        taxClass = new TaxClass();
        taxClass.setId(1L);
        taxClass.setName("Standard Tax");

        taxRate = new TaxRate();
        taxRate.setId(10L);
        taxRate.setRate(10.0);
        taxRate.setZipCode("70000");
        taxRate.setStateOrProvinceId(2L);
        taxRate.setCountryId(3L);
        taxRate.setTaxClass(taxClass);
    }

    // ==========================================
    // 1. TESTS CHO HÀM: createTaxRate
    // ==========================================

    @Test
    void createTaxRate_WhenTaxClassNotFound_ShouldThrowNotFoundException() {
        when(taxClassRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> taxRateService.createTaxRate(taxRatePostVm));
    }

    @Test
    void createTaxRate_Success() {
        when(taxClassRepository.existsById(1L)).thenReturn(true);
        when(taxClassRepository.getReferenceById(1L)).thenReturn(taxClass);
        when(taxRateRepository.save(any(TaxRate.class))).thenReturn(taxRate);

        TaxRate result = taxRateService.createTaxRate(taxRatePostVm);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        verify(taxRateRepository).save(any(TaxRate.class));
    }

    // ==========================================
    // 2. TESTS CHO HÀM: updateTaxRate
    // ==========================================

    @Test
    void updateTaxRate_WhenTaxRateNotFound_ShouldThrowNotFoundException() {
        when(taxRateRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> taxRateService.updateTaxRate(taxRatePostVm, 10L));
    }

    @Test
    void updateTaxRate_WhenTaxClassNotFound_ShouldThrowNotFoundException() {
        when(taxRateRepository.findById(10L)).thenReturn(Optional.of(taxRate));
        when(taxClassRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> taxRateService.updateTaxRate(taxRatePostVm, 10L));
    }

    @Test
    void updateTaxRate_Success() {
        when(taxRateRepository.findById(10L)).thenReturn(Optional.of(taxRate));
        when(taxClassRepository.existsById(1L)).thenReturn(true);
        when(taxClassRepository.getReferenceById(1L)).thenReturn(taxClass);

        taxRateService.updateTaxRate(taxRatePostVm, 10L);

        verify(taxRateRepository).save(taxRate);
        assertEquals(10.0, taxRate.getRate());
    }

    // ==========================================
    // 3. TESTS CHO HÀM: delete
    // ==========================================

    @Test
    void delete_WhenTaxRateNotFound_ShouldThrowNotFoundException() {
        when(taxRateRepository.existsById(10L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> taxRateService.delete(10L));
    }

    @Test
    void delete_Success() {
        when(taxRateRepository.existsById(10L)).thenReturn(true);

        taxRateService.delete(10L);

        verify(taxRateRepository).deleteById(10L);
    }

    // ==========================================
    // 4. TESTS CHO HÀM: findById & findAll
    // ==========================================

    @Test
    void findById_WhenNotFound_ShouldThrowNotFoundException() {
        when(taxRateRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> taxRateService.findById(10L));
    }

    @Test
    void findById_Success() {
        when(taxRateRepository.findById(10L)).thenReturn(Optional.of(taxRate));

        TaxRateVm result = taxRateService.findById(10L);

        assertNotNull(result);
        assertEquals(10L, result.id());
    }

    @Test
    void findAll_Success() {
        when(taxRateRepository.findAll()).thenReturn(List.of(taxRate));

        List<TaxRateVm> result = taxRateService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ==========================================
    // 5. TESTS CHO HÀM: getPageableTaxRates
    // ==========================================

    @Test
    void getPageableTaxRates_WithEmptyList_Success() {
        Page<TaxRate> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(taxRateRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        TaxRateListGetVm result = taxRateService.getPageableTaxRates(0, 10);

        assertNotNull(result);
        assertEquals(0, result.totalElements());
    }

    @Test
    void getPageableTaxRates_WithData_Success() {
        Page<TaxRate> page = new PageImpl<>(List.of(taxRate), PageRequest.of(0, 10), 1);
        when(taxRateRepository.findAll(any(Pageable.class))).thenReturn(page);

        StateOrProvinceAndCountryGetNameVm locationMock = mock(StateOrProvinceAndCountryGetNameVm.class);
        when(locationMock.stateOrProvinceId()).thenReturn(2L);
        when(locationMock.stateOrProvinceName()).thenReturn("Binh Duong");
        when(locationMock.countryName()).thenReturn("Vietnam");

        when(locationService.getStateOrProvinceAndCountryNames(List.of(2L)))
            .thenReturn(List.of(locationMock));

        TaxRateListGetVm result = taxRateService.getPageableTaxRates(0, 10);

        assertNotNull(result);
        assertEquals(1, result.totalElements());
        assertEquals(1, result.taxRateGetDetailContent().size());
        assertEquals("Binh Duong", result.taxRateGetDetailContent().get(0).stateOrProvinceName());
    }

    // ==========================================
    // 6. TESTS CHO CÁC HÀM TÍNH PERCENT & BATCH
    // ==========================================

    @Test
    void getTaxPercent_WhenFound_ShouldReturnPercent() {
        when(taxRateRepository.getTaxPercent(3L, 2L, "70000", 1L)).thenReturn(10.5);

        double percent = taxRateService.getTaxPercent(1L, 3L, 2L, "70000");

        assertEquals(10.5, percent);
    }

    @Test
    void getTaxPercent_WhenNotFound_ShouldReturnZero() {
        when(taxRateRepository.getTaxPercent(3L, 2L, "70000", 1L)).thenReturn(null);

        double percent = taxRateService.getTaxPercent(1L, 3L, 2L, "70000");

        assertEquals(0.0, percent);
    }

    @Test
    void getBulkTaxRate_Success() {
        when(taxRateRepository.getBatchTaxRates(eq(3L), eq(2L), eq("70000"), any(Set.class)))
            .thenReturn(List.of(taxRate));

        List<TaxRateVm> result = taxRateService.getBulkTaxRate(List.of(1L), 3L, 2L, "70000");

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}