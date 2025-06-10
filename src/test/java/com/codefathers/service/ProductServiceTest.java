package com.codefathers.service;

import com.codefathers.model.dto.CreateProductDTO;
import com.codefathers.model.dto.UpdateProductDTO;
import com.codefathers.model.entity.Product;
import com.codefathers.repository.interfaces.ProductRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private Validator validator;

    @InjectMocks
    private ProductService productService;

    private CreateProductDTO createDto;
    private UpdateProductDTO updateDto;
    private Product existingProduct;

    @BeforeEach
    void setUp() {
        createDto = new CreateProductDTO("ABC123", "Mouse", "Mouse ótico", BigDecimal.valueOf(20.0), BigDecimal.valueOf(40.0));
        updateDto = new UpdateProductDTO("Mouse sem fio", BigDecimal.valueOf(25.0), BigDecimal.valueOf(45.0));
        existingProduct = Product.builder()
                .sku("ABC123")
                .name("Mouse")
                .description("Antigo")
                .buyPrice(BigDecimal.valueOf(20.0))
                .sellPrice(BigDecimal.valueOf(40.0))
                .build();
    }

    @Test
    void testCreateProduct_withValidDTO_savesSuccessfully() {
        when(validator.validate(createDto)).thenReturn(Collections.emptySet());

        productService.createProduct(createDto);

        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testCreateProduct_withViolations_throwsException() {
        ConstraintViolation<CreateProductDTO> violation = mock(ConstraintViolation.class);
        Set<ConstraintViolation<CreateProductDTO>> violations = Set.of(violation);

        when(validator.validate(createDto)).thenReturn(violations);

        assertThrows(jakarta.validation.ConstraintViolationException.class,
                () -> productService.createProduct(createDto));

        verify(productRepository, never()).save(any());
    }

    @Test
    void testUpdateProduct_withValidDTO_updatesSuccessfully() {
        when(productRepository.update("ABC123")).thenReturn(existingProduct);
        when(validator.validate(updateDto)).thenReturn(Collections.emptySet());

        productService.updateProduct("ABC123", updateDto);

        verify(productRepository).save(argThat(product ->
                product.getDescription().equals("Mouse sem fio")
                        && product.getBuyPrice().equals(BigDecimal.valueOf(25.0))
                        && product.getSellPrice().equals(BigDecimal.valueOf(45.0))
        ));
    }

    @Test
    void testUpdateProduct_whenNotFound_throwsRuntimeException() {
        when(productRepository.update("ABC123")).thenReturn(null);
        when(validator.validate(updateDto)).thenReturn(Collections.emptySet());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> productService.updateProduct("ABC123", updateDto));

        assertTrue(exception.getMessage().contains("Produto com SKU 'ABC123' não encontrado."));
    }

    @Test
    void testUpdateProduct_withViolations_throwsException() {
        when(productRepository.update("ABC123")).thenReturn(existingProduct);
        ConstraintViolation<UpdateProductDTO> violation = mock(ConstraintViolation.class);
        Set<ConstraintViolation<UpdateProductDTO>> violations = Set.of(violation);

        when(validator.validate(updateDto)).thenReturn(violations);

        assertThrows(jakarta.validation.ConstraintViolationException.class,
                () -> productService.updateProduct("ABC123", updateDto));

        verify(productRepository, never()).save(any());
    }

    @Test
    void testFindProductBySKU_returnsProduct() {
        when(productRepository.findBySKU("ABC123")).thenReturn(existingProduct);

        Product result = productService.findProductBySKU("ABC123");

        assertNotNull(result);
        assertEquals("ABC123", result.getSku());
    }

    @Test
    void testFindAllProducts_returnsList() {
        List<Product> productList = List.of(existingProduct);
        when(productRepository.listAllProducts()).thenReturn(productList);

        List<Product> result = productService.findAllProducts();

        assertEquals(1, result.size());
        assertEquals("Mouse", result.get(0).getName());
    }

    @Test
    void testCreateProduct_whenSaveThrowsConstraintViolationException_shouldLogError() {
        when(validator.validate(createDto)).thenReturn(Collections.emptySet());
        doThrow(new ConstraintViolationException("erro", Collections.emptySet()))
                .when(productRepository).save(any());

        assertDoesNotThrow(() -> productService.createProduct(createDto));

        verify(productRepository).save(any());
    }

    @Test
    void testUpdateProduct_whenSaveThrowsConstraintViolationException_shouldLogError() {
        when(productRepository.update("ABC123")).thenReturn(existingProduct);
        when(validator.validate(updateDto)).thenReturn(Collections.emptySet());
        doThrow(new ConstraintViolationException("erro", Collections.emptySet()))
                .when(productRepository).save(any());

        assertDoesNotThrow(() -> productService.updateProduct("ABC123", updateDto));
    }

    @Test
    void testCreateProduct_withNullDTO_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> productService.createProduct(null));
    }

    @Test
    void testFindProductBySKU_whenNotFound_returnsNull() {
        when(productRepository.findBySKU("XYZ999")).thenReturn(null);

        Product result = productService.findProductBySKU("XYZ999");

        assertNull(result);
    }

    @Test
    void testFindAllProducts_whenNoProducts_returnsEmptyList() {
        when(productRepository.listAllProducts()).thenReturn(Collections.emptyList());

        List<Product> result = productService.findAllProducts();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


}
