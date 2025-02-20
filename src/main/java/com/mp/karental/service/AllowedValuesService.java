package com.mp.karental.service;

import com.mp.karental.constant.EAdditionalFunctions;
import com.mp.karental.constant.EColors;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AllowedValuesService {

    public List<String> getAllowedValuesOfAdditionalFunction() {
        return Arrays.stream(EAdditionalFunctions.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }
    public List<String> getAllowedValuesOfColor() {
        return Arrays.stream(EColors.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }
    public Map<String, List<String>> getAllAllowedValues() {
        return Map.of(
                "allowedColors", getAllowedValuesOfColor(),
                "allowedFunctions", getAllowedValuesOfAdditionalFunction()
        );
    }


}
