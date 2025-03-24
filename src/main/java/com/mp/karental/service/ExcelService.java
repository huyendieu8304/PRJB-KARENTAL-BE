package com.mp.karental.service;

import lombok.AccessLevel;
import lombok.Getter;
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
    /**
     * -- GETTER --
     *  Retrieves the entire brand-model mapping.
     *
     */
    // Map to store car brands and their corresponding models
    @Getter
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
        loadExcelData("xls/Car Rentals_Value list_Brand and model.xlsx","car");
        loadExcelData("xls/Address value list.xls","address");
    }
    /**
     * Loads Excel data based on the specified data type ("car" or "address").
     *
     * @param filePath Path to the Excel file.
     * @param dataType Type of data ("car" for car brands/models, "address" for location data).
     */
    public void loadExcelData(String filePath, String dataType) {
        try (InputStream inputStream = new ClassPathResource(filePath).getInputStream();
             //HSS for xls and XSS for xlsx
             Workbook workbook = filePath.endsWith(".xls") ? new HSSFWorkbook(inputStream) : new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // Get the first sheet in the Excel file

            // Iterate through each row in the sheet
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip the header row

                // Process car brand and model data
                if ("car".equalsIgnoreCase(dataType)) {
                    String brand = getCellValue(row.getCell(1)).trim(); // Get brand name from column 1
                    String model = getCellValue(row.getCell(2)).trim(); // Get model name from column 2

                    // Only add to the map if both brand and model are not empty
                    if (!brand.isEmpty() && !model.isEmpty()) {
                        brandModelMap.computeIfAbsent(brand, k -> new HashSet<>()).add(model);
                    }
                }
                // Process address data (wards, districts, cities)
                else if ("address".equalsIgnoreCase(dataType)) {
                    String ward = getCellValue(row.getCell(1)).trim(); // Get ward name from column 1
                    String district = getCellValue(row.getCell(3)).trim(); // Get district name from column 3
                    String cityProvince = getCellValue(row.getCell(5)).trim(); // Get city/province name from column 5

                    // Add extracted values to respective sets
                    wards.add(ward);
                    districts.add(district);
                    cities.add(cityProvince);

                    // Build a mapping of districts grouped by city/province
                    if (!district.isEmpty() && !cityProvince.isEmpty()) {
                        districtsByCity.computeIfAbsent(cityProvince, k -> new HashSet<>()).add(district);
                    }

                    // Build a mapping of wards grouped by district
                    if (!ward.isEmpty() && !district.isEmpty()) {
                        wardsByDistrict.computeIfAbsent(district, k -> new HashSet<>()).add(ward);
                    }
                }
                // Throw an exception if an invalid data type is provided
                else {
                    throw new IllegalArgumentException("Invalid dataType: " + dataType);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Excel data from file: " + filePath, e);
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
