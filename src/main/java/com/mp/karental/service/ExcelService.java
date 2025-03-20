package com.mp.karental.service;

import lombok.AccessLevel;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for get data from Excel file.
 *
 * @author QuangPM20
 * @version 1.0
 */
@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExcelService {
    // Map to store car brands and their corresponding models
    Map<String, Set<String>> brandModelMap = new HashMap<>();

    // Sets to store unique wards, districts, and cities
    private final Set<String> wards = new HashSet<>();
    private final Set<String> districts = new HashSet<>();
    private final Set<String> cities = new HashSet<>();

    // Maps to store relationships between districts and wards, and cities and districts
    private final Map<String, Set<String>> wardsByDistrict = new HashMap<>();
    private final Map<String, Set<String>> districtsByCity = new HashMap<>();

    // Constructor to initialize and load Excel data
    public ExcelService() {
        try {
            loadExcelDataCar("xls/Car Rentals_Value list_Brand and model.xlsx");
            loadExcelDataAddress("xls/Address value list.xls");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Loads car brand and model data from an Excel file and populates the brandModelMap.
     * @param filePath Path to the Excel file.
     * @throws IOException If there is an issue reading the file.
     */
    public void loadExcelDataCar(String filePath) throws IOException {
        // Retrieve the file from the classpath (resource folder in the application)
//        File file = new ClassPathResource(filePath).getFile();
//
//        // Read the Excel file using FileInputStream and XSSFWorkbook for .xlsx format
//        try (FileInputStream fis = new FileInputStream(file);
//             Workbook workbook = new XSSFWorkbook(fis)) {

        // Đọc file từ classpath dưới dạng InputStream
        try (InputStream inputStream = new ClassPathResource(filePath).getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // Get the first sheet in the Excel file

            // Iterate through each row in the sheet
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip the header row

                // Retrieve values from the "Brand" (column 1) and "Model" (column 2)
                String brand = getCellValue(row.getCell(1)).trim();
                String model = getCellValue(row.getCell(2)).trim();

                // Only add to the map if both brand and model are not empty
                if (!brand.isEmpty() && !model.isEmpty()) {
                    // If the brand is not in the map, add it with a HashSet to store unique models
                    brandModelMap.computeIfAbsent(brand, k -> new HashSet<>()).add(model);
                }
            }
        }
    }

    /**
     * Loads address data (wards, districts, cities) from an Excel file and populates related collections.
     * @param filePath Path to the Excel file.
     * @throws IOException If there is an issue reading the file.
     */
    public void loadExcelDataAddress(String filePath) throws IOException {
//        // Retrieve the file from the classpath
//        File file = new ClassPathResource(filePath).getFile();

//        // Read the Excel file using FileInputStream
//        try (FileInputStream fis = new FileInputStream(file)) {

        // Đọc file từ classpath dưới dạng InputStream
        try (InputStream inputStream = new ClassPathResource(filePath).getInputStream();
             Workbook workbook = filePath.endsWith(".xls")
                     ? new HSSFWorkbook(inputStream)
                     : new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // Get the first sheet in the Excel file

            // Iterate through each row in the sheet
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip the header row

                // Extract data from the corresponding columns
                String ward = getCellValue(row.getCell(1)).trim(); // Ward column
                String district = getCellValue(row.getCell(3)).trim(); // District column
                String cityProvince = getCellValue(row.getCell(5)).trim(); // City/Province column

                // Add extracted values to the respective sets
                wards.add(ward);
                districts.add(district);
                cities.add(cityProvince);

                // Build a map grouping districts by city/province
                if (!district.isEmpty() && !cityProvince.isEmpty()) {
                    districtsByCity.computeIfAbsent(cityProvince, k -> new HashSet<>()).add(district);
                }

                // Build a map grouping wards by district
                if (!ward.isEmpty() && !district.isEmpty()) {
                    wardsByDistrict.computeIfAbsent(district, k -> new HashSet<>()).add(ward);
                }
            }
        }
    }

    /**
     * Retrieves the value of a given Excel cell as a String.
     * This method handles different cell types and converts them into a String format.
     *
     * @param cell The Excel cell to extract the value from.
     * @return A String representation of the cell's value. Returns an empty string if the cell is null or its type is unsupported.
     */
    public String getCellValue(Cell cell) {
        // Return an empty string if the cell is null (to avoid NullPointerException)
        if (cell == null) return "";

        // Use a switch statement to determine the cell type and extract the appropriate value
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim(); // Return string value, removing leading/trailing spaces
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue()); // Convert numeric value to integer and then to String
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue()); // Convert boolean value to String ("true" or "false")
            case FORMULA -> cell.getCellFormula(); // Return the formula itself as a String
            default -> ""; // Return an empty string for unsupported or blank cell types
        };
    }




    /**
     * Retrieves a list of all wards.
     * @return List of all wards.
     */
    public List<String> getAllWards() {
        return new ArrayList<>(wards);
    }

    /**
     * Retrieves a list of all districts.
     * @return List of all districts.
     */
    public List<String> getAllDistricts() {
        return new ArrayList<>(districts);
    }

    /**
     * Retrieves a list of all cities.
     * @return List of all cities.
     */
    public List<String> getAllCities() {
        return new ArrayList<>(cities);
    }

    /**
     * Retrieves all wards belonging to a specific district.
     * @param district The district name.
     * @return Set of wards in the specified district.
     */
    public Set<String> getWardsByDistrict(String district) {
        return wardsByDistrict.getOrDefault(district, Collections.emptySet());
    }

    /**
     * Retrieves all districts belonging to a specific city.
     * @param city The city name.
     * @return Set of districts in the specified city.
     */
    public Set<String> getDistrictsByCity(String city) {
        return districtsByCity.getOrDefault(city, Collections.emptySet());
    }

    /**
     * Retrieves the entire brand-model mapping.
     * @return A map where the key is the brand name and the value is a set of model names.
     */
    public Map<String, Set<String>> getBrandModelMap() {
        return brandModelMap;
    }

    /**
     * Retrieves a sorted list of all car brands.
     * @return List of car brands sorted alphabetically.
     */
    public List<String> getAllBrands() {
        return brandModelMap.keySet().stream().sorted().collect(Collectors.toList());
    }

    /**
     * Retrieves a sorted list of all car models.
     * @return List of car models sorted alphabetically.
     */
    public List<String> getAllModels() {
        return brandModelMap.values().stream().flatMap(Set::stream).sorted().collect(Collectors.toList());
    }

    /**
     * Retrieves all models associated with a specific brand.
     * @param brand The brand name.
     * @return List of models under the specified brand.
     */
    public List<String> getModelsByBrand(String brand) {
        return new ArrayList<>(brandModelMap.getOrDefault(brand, Collections.emptySet()));
    }

    /**
     * Retrieves all brands that contain a specific model.
     * @param model The model name.
     * @return List of brands that offer the specified model.
     */
    public List<String> getBrandsByModel(String model) {
        List<String> brands = new ArrayList<>();

        for (Map.Entry<String, Set<String>> entry : brandModelMap.entrySet()) {
            if (entry.getValue().contains(model)) {  // Check if the model exists in the set
                brands.add(entry.getKey());          // Add the brand to the list
            }
        }

        return brands; // Return all brands that contain this model
    }

}
