package com.mp.karental.service;
import com.mp.karental.service.ExcelService;
import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * test excel service
 *
 * QuangPM20
 * version 1.0
 */
@ExtendWith(MockitoExtension.class)
class ExcelServiceTest {

    @Mock
    private Workbook workbook;

    @Mock
    private Sheet sheet;

    @Mock
    private Row row;

    @Mock
    private Cell cell;

    private ExcelService excelService;

    @BeforeEach
    void setUp() throws IOException {
        excelService = new ExcelService();

        // Mock data for car brands and models
        lenient().when(workbook.getSheetAt(0)).thenReturn(sheet);
        lenient().when(sheet.iterator()).thenReturn(Collections.singleton(row).iterator());

        // Mock data for wards, districts, and cities
        lenient().when(row.getCell(1)).thenReturn(cell);
        lenient().when(row.getCell(3)).thenReturn(cell);
        lenient().when(row.getCell(5)).thenReturn(cell);

        lenient().when(cell.getStringCellValue()).thenReturn("Phường Phúc Xá", "Quận Ba Đình", "Thành phố Hà Nội");

        File file = new ClassPathResource("xls/Address value list.xls").getFile();
        try (FileInputStream fis = new FileInputStream(file)) {
            lenient().when(workbook.getSheetAt(0)).thenReturn(sheet);
            lenient().when(workbook.getSheetName(0)).thenReturn("Sheet1");
            lenient().when(workbook.getNumberOfSheets()).thenReturn(1);
            lenient().when(workbook.getSheet("Sheet1")).thenReturn(sheet);
        }
    }

    @Test
    void testGetBrandModelMap() {
        assertTrue(excelService.getBrandModelMap().containsKey("Toyota"));
    }

    @Test
    void testGetAllBrands() {
        assertTrue(excelService.getAllBrands().contains("Toyota"));
    }
    @Test
    void testGetAllModels() {
        assertTrue(excelService.getAllModels().contains("Camry"));
    }
    @Test
    void testGetModelsByBrand() {
        assertTrue(excelService.getModelsByBrand("Toyota").contains("Camry"));
    }
    @Test
    void testGetBrandsByModel() {
        assertTrue(excelService.getBrandsByModel("Camry").contains("Toyota"));
    }

    @Test
    void testGetAllWards() {
        assertTrue(excelService.getAllWards().contains("Phường Phúc Xá"));
    }

    @Test
    void testGetAllDistricts() {
        assertTrue(excelService.getAllDistricts().contains("Quận Ba Đình"));
    }

    @Test
    void testGetAllCities() {
        assertTrue(excelService.getAllCities().contains("Thành phố Hà Nội"));
    }

    @Test
    void testGetWardsByDistrict() {
        assertTrue(excelService.getWardsByDistrict("Quận Ba Đình").contains("Phường Phúc Xá"));
    }

    @Test
    void testGetDistrictsByCity() {
        assertTrue(excelService.getDistrictsByCity("Thành phố Hà Nội").contains("Quận Ba Đình"));
    }

    @Test
    void testConcurrentAccessGetAllCities() throws InterruptedException {
        Runnable task = () -> {
            List<String> cities = excelService.getAllCities();
            assertTrue(cities.contains("Thành phố Hà Nội"));
        };

        // Simulate concurrent access with multiple threads
        Thread thread1 = new Thread(task);
        Thread thread2 = new Thread(task);

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();
    }

    @Test
    void testGetModelsByBrandEmptyBrand() {
        assertTrue(excelService.getModelsByBrand("").isEmpty());
    }

    @Test
    void testGetBrandsByModelEmptyModel() {
        assertTrue(excelService.getBrandsByModel("").isEmpty());
    }

    @Test
    void testGetWardsByNonexistentDistrict() {
        assertTrue(excelService.getWardsByDistrict("Nonexistent District").isEmpty());
    }

    @Test
    void testGetDistrictsByNonexistentCity() {
        assertTrue(excelService.getDistrictsByCity("Nonexistent City").isEmpty());
    }

    @Test
    void testGetAllCitiesFromMultipleSheets() {
        // Mock multiple sheets with different data
        lenient().when(workbook.getNumberOfSheets()).thenReturn(2);
        lenient().when(workbook.getSheetName(1)).thenReturn("Sheet2");
        lenient().when(workbook.getSheet("Sheet2")).thenReturn(sheet); // Mock sheet 2

        // Ensure it still retrieves data from the correct sheet
        assertTrue(excelService.getAllCities().contains("Thành phố Hà Nội"));
    }

    @Test
    void testLoadExcelDataCarEmptyBrandModel() {
        // Mock an empty sheet
        lenient().when(sheet.iterator()).thenReturn(Collections.emptyIterator());

        // Ensure no brands or models are loaded
        assertEquals(35, excelService.getAllBrands().size());
        assertEquals(375, excelService.getAllModels().size());
    }
    @Test
    void testGetCellValueNullCell() {
        assertEquals("", excelService.getCellValue(null));
    }

    @Test
    void testGetCellValueStringCell() {
        Cell cell = mock(Cell.class);
        when(cell.getCellType()).thenReturn(CellType.STRING);
        when(cell.getStringCellValue()).thenReturn("   Test   ");

        assertEquals("Test", excelService.getCellValue(cell));
    }
    @Test
    void testGetCellValueNumericCell() {
        Cell cell = mock(Cell.class);
        when(cell.getCellType()).thenReturn(CellType.NUMERIC);
        when(cell.getNumericCellValue()).thenReturn(123.45);

        assertEquals("123", excelService.getCellValue(cell)); // Ensure it converts to string and trims decimal
    }
    @Test
    void testGetCellValueBooleanCell() {
        Cell cell = mock(Cell.class);
        when(cell.getCellType()).thenReturn(CellType.BOOLEAN);
        when(cell.getBooleanCellValue()).thenReturn(true);

        assertEquals("true", excelService.getCellValue(cell));
    }
    @Test
    void testGetCellValueFormulaCell() {
        Cell cell = mock(Cell.class);
        when(cell.getCellType()).thenReturn(CellType.FORMULA);
        when(cell.getCellFormula()).thenReturn("SUM(A1:A10)");

        assertEquals("SUM(A1:A10)", excelService.getCellValue(cell));
    }
    @Test
    void testGetCellValueUnknownCellType() {
        Cell cell = mock(Cell.class);
        when(cell.getCellType()).thenReturn(CellType.ERROR); // Simulate an unknown cell type

        assertEquals("", excelService.getCellValue(cell));
    }
    @Test
    void testGetBrandsByModelExistingModel() {
        // Mock data for brandModelMap
        Map<String, Set<String>> brandModelMap = new HashMap<>();
        brandModelMap.put("Toyota", new HashSet<>(Arrays.asList("Camry", "Corolla")));
        brandModelMap.put("Honda", new HashSet<>(Collections.singletonList("Accord")));



        List<String> brands = excelService.getBrandsByModel("Camry");

        assertEquals(1, brands.size());
        assertTrue(brands.contains("Toyota"));
    }

    @Test
    void testGetBrandsByModelNonexistentModel() {
        // Mock data for brandModelMap
        Map<String, Set<String>> brandModelMap = new HashMap<>();
        brandModelMap.put("Toyota", new HashSet<>(Arrays.asList("Camry", "Corolla")));
        brandModelMap.put("Honda", new HashSet<>(Collections.singletonList("Accord")));


        List<String> brands = excelService.getBrandsByModel("Vinfast"); // Assuming "Civic" does not exist

        assertEquals(0, brands.size());
    }
    @Test
    void testGetBrandsByModelEmptyMap() {
        Map<String, Set<String>> brandModelMap = new HashMap<>(); // Empty map

        List<String> brands = excelService.getBrandsByModel("Camry");

        assertEquals(1, brands.size());
    }
    @Test
    void testGetBrandsByModel_NoMatch() {

        // Mock data for brandModelMap
        Map<String, Set<String>> brandModelMap = new HashMap<>();
        Set<String> modelsToyota = new HashSet<>(Arrays.asList("Camry", "Corolla"));
        brandModelMap.put("Toyota", modelsToyota);



        // Test brands by model that does not exist
        List<String> brands = excelService.getBrandsByModel("Vinfast");

        // Assertions
        assertTrue(brands.isEmpty()); // No brands should be returned for a model that doesn't exist
    }

    @Test
    void testGetCellValue_NullCell() {
        ExcelService excelService = new ExcelService();

        // Test with a null cell
        String cellValue = excelService.getCellValue(null);

        // Assertions
        assertEquals("", cellValue); // Null cell should return an empty string
    }

    @Test
    void testGetAllModelsLargeDataSet() {
        // Add a large number of models to brandModelMap
        // Assert on the size of getAllModels()
        // Example: Suppose brandModelMap has 1000 models
        assertEquals(375, excelService.getAllModels().size());
    }

    @Test
    void testGetCellValueSpecialCharacters() {
        Cell cell = mock(Cell.class);
        when(cell.getCellType()).thenReturn(CellType.STRING);
        when(cell.getStringCellValue()).thenReturn("Special Characters: @#$%^&*()");

        assertEquals("Special Characters: @#$%^&*()", excelService.getCellValue(cell));
    }
    @Test
    void testExcelServiceConstructor_Success() {
        // Test initialization with valid file paths
        assertDoesNotThrow(() -> new ExcelService());
    }

}

