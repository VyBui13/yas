package com.yas.tax.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.yas.commonlibrary.exception.DuplicatedException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.tax.model.TaxClass;
import com.yas.tax.repository.TaxClassRepository;
import com.yas.tax.viewmodel.taxclass.TaxClassListGetVm;
import com.yas.tax.viewmodel.taxclass.TaxClassPostVm;
import com.yas.tax.viewmodel.taxclass.TaxClassVm;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class TaxClassServiceTest {

    @Mock
    private TaxClassRepository taxClassRepository;

    @InjectMocks
    private TaxClassService taxClassService;

    private TaxClass taxClass;
    private TaxClassPostVm taxClassPostVm;

    @BeforeEach
    void setUp() {
        taxClass = new TaxClass();
        taxClass.setId(1L);
        taxClass.setName("Standard Rate");

        // Dùng mock cho PostVm để tránh lỗi Constructor
        taxClassPostVm = mock(TaxClassPostVm.class);
        lenient().when(taxClassPostVm.name()).thenReturn("Standard Rate");
        lenient().when(taxClassPostVm.toModel()).thenReturn(taxClass);
    }

    // ==========================================
    // 1. TESTS CHO HÀM: findAllTaxClasses
    // ==========================================

    @Test
    void findAllTaxClasses_Success() {
        when(taxClassRepository.findAll(any(Sort.class))).thenReturn(List.of(taxClass));

        List<TaxClassVm> result = taxClassService.findAllTaxClasses();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Standard Rate", result.get(0).name());
    }

    // ==========================================
    // 2. TESTS CHO HÀM: findById
    // ==========================================

    @Test
    void findById_WhenNotFound_ShouldThrowNotFoundException() {
        when(taxClassRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> taxClassService.findById(1L));
    }

    @Test
    void findById_Success() {
        when(taxClassRepository.findById(1L)).thenReturn(Optional.of(taxClass));

        TaxClassVm result = taxClassService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
    }

    // ==========================================
    // 3. TESTS CHO HÀM: create
    // ==========================================

    @Test
    void create_WhenNameAlreadyExists_ShouldThrowDuplicatedException() {
        when(taxClassRepository.existsByName("Standard Rate")).thenReturn(true);

        assertThrows(DuplicatedException.class, () -> taxClassService.create(taxClassPostVm));
    }

    @Test
    void create_Success() {
        when(taxClassRepository.existsByName("Standard Rate")).thenReturn(false);
        when(taxClassRepository.save(any(TaxClass.class))).thenReturn(taxClass);

        TaxClass result = taxClassService.create(taxClassPostVm);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(taxClassRepository).save(any(TaxClass.class));
    }

    // ==========================================
    // 4. TESTS CHO HÀM: update
    // ==========================================

    @Test
    void update_WhenNotFound_ShouldThrowNotFoundException() {
        when(taxClassRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> taxClassService.update(taxClassPostVm, 1L));
    }

    @Test
    void update_WhenNameExistsForOtherId_ShouldThrowDuplicatedException() {
        when(taxClassRepository.findById(1L)).thenReturn(Optional.of(taxClass));
        when(taxClassRepository.existsByNameNotUpdatingTaxClass("Standard Rate", 1L)).thenReturn(true);

        assertThrows(DuplicatedException.class, () -> taxClassService.update(taxClassPostVm, 1L));
    }

    @Test
    void update_Success() {
        when(taxClassRepository.findById(1L)).thenReturn(Optional.of(taxClass));
        when(taxClassRepository.existsByNameNotUpdatingTaxClass("Standard Rate", 1L)).thenReturn(false);

        taxClassService.update(taxClassPostVm, 1L);

        // Kiểm tra xem tên có được set lại đúng không và có gọi lệnh save không
        ArgumentCaptor<TaxClass> captor = ArgumentCaptor.forClass(TaxClass.class);
        verify(taxClassRepository).save(captor.capture());
        assertEquals("Standard Rate", captor.getValue().getName());
    }

    // ==========================================
    // 5. TESTS CHO HÀM: delete
    // ==========================================

    @Test
    void delete_WhenNotFound_ShouldThrowNotFoundException() {
        when(taxClassRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> taxClassService.delete(1L));
    }

    @Test
    void delete_Success() {
        when(taxClassRepository.existsById(1L)).thenReturn(true);

        taxClassService.delete(1L);

        verify(taxClassRepository).deleteById(1L);
    }

    // ==========================================
    // 6. TESTS CHO HÀM: getPageableTaxClasses
    // ==========================================

    @Test
    void getPageableTaxClasses_Success() {
        Page<TaxClass> page = new PageImpl<>(List.of(taxClass), PageRequest.of(0, 10), 1);
        when(taxClassRepository.findAll(any(Pageable.class))).thenReturn(page);

        TaxClassListGetVm result = taxClassService.getPageableTaxClasses(0, 10);

        assertNotNull(result);
        assertEquals(1, result.totalElements());
        
        // Gọi thẳng taxClassContent() dựa trên file bạn đã gửi trước đó!
        assertEquals(1, result.taxClassContent().size());
        assertEquals("Standard Rate", result.taxClassContent().get(0).name());
    }
}