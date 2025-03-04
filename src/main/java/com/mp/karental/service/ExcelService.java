package com.mp.karental.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExcelService {
    Map<String, Set<String>> brandModelMap = new HashMap<>();
    private final List<String> addressList = new ArrayList<>();

    private final Set<String> wards = new HashSet<>();
    private final Set<String> districts = new HashSet<>();
    private final Set<String> cities = new HashSet<>();

    private final Map<String, Set<String>> wardsByDistrict = new HashMap<>();
    private final Map<String, Set<String>> districtsByCity = new HashMap<>();
    public ExcelService() {
        try {
            loadExcelDataCar("xls/Car Rentals_Value list_Brand and model.xlsx");
            loadExcelDataAddress("xls/Address value list.xls");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void loadExcelDataCar(String filePath) throws IOException {
        File file = new ClassPathResource(filePath).getFile();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                String brand = getCellValue(row.getCell(1)).trim(); // Column Brand
                String model = getCellValue(row.getCell(2)).trim(); // Column Model

                if (!brand.isEmpty() && !model.isEmpty()) {
                    brandModelMap.computeIfAbsent(brand, k -> new HashSet<>()).add(model); // Brand → Models (Unique)
                }
            }
        }
    }
    private void loadExcelDataAddress(String filePath) throws IOException {
        File file = new ClassPathResource(filePath).getFile();

        try (FileInputStream fis = new FileInputStream(file)) {
            Workbook workbook;
            if (file.getName().endsWith(".xls")) {
                workbook = new HSSFWorkbook(fis); // For .xls files
            } else {
                workbook = new XSSFWorkbook(fis); // For .xlsx files
            }

            Sheet sheet = workbook.getSheetAt(0); // Get first sheet
            Scanner scanner = new Scanner(System.in); // For user input

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                // Extract data from Excel
                String ward = getCellValue(row.getCell(1)).trim();
                String district = getCellValue(row.getCell(3)).trim();
                String cityProvince = getCellValue(row.getCell(5)).trim();

                // Combine into a full address
                String fullAddress = String.join(",", cityProvince, district, ward);

                //addressList.add(fullAddress);

                if (!ward.isEmpty()) wards.add(ward);
                if (!district.isEmpty()) districts.add(district);
                if (!cityProvince.isEmpty()) cities.add(cityProvince);

                if (!district.isEmpty() && !cityProvince.isEmpty()) {
                    districtsByCity.computeIfAbsent(cityProvince, k -> new HashSet<>()).add(district);
                }
                if (!ward.isEmpty() && !district.isEmpty()) {
                    wardsByDistrict.computeIfAbsent(district, k -> new HashSet<>()).add(ward);
                }
            }
        }

    }

    public List<String> addressList() {
        return addressList;
    }

    public List<String> getAllWards() {
        return new ArrayList<>(wards);
    }

    // Method to get all districts as a list
    public List<String> getAllDistricts() {
        return new ArrayList<>(districts);
    }

    // Method to get all cities as a list
    public List<String> getAllCities() {
        return new ArrayList<>(cities);
    }

    public Set<String> getWardsByDistrict(String district) {
        return wardsByDistrict.getOrDefault(district, Collections.emptySet());
    }

    public Set<String> getDistrictsByCity(String city) {
        return districtsByCity.getOrDefault(city, Collections.emptySet());
    }

    public Map<String, Set<String>> getBrandModelMap() {
        return brandModelMap;
    }

    public List<String> getAllBrands() {
        return brandModelMap.keySet().stream().sorted().collect(Collectors.toList());
    }

    public List<String> getAllModels() {
        return brandModelMap.values().stream().flatMap(Set::stream).sorted().collect(Collectors.toList());
    }

    public List<String> getModelsByBrand(String brand) {
        return new ArrayList<>(brandModelMap.getOrDefault(brand, Collections.emptySet()));
    }
    public List<String> getBrandsByModel(String model) {
        List<String> brands = new ArrayList<>();

        for (Map.Entry<String, Set<String>> entry : brandModelMap.entrySet()) {
            if (entry.getValue().contains(model)) {  // Check if model exists in the set
                brands.add(entry.getKey());          // Add the brand to the list
            }
        }

        return brands; // Return all brands that contain this model
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }
}
