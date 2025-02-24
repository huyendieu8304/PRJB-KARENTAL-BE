package com.mp.karental.service;

import com.mp.karental.constant.EAdditionalFunctions;
import com.mp.karental.constant.EColors;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AllowedValuesService {

    public List<String> getAllowedValuesOfColor() {
        return Arrays.stream(EColors.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }
}
