package com.github.romansorokin.resultset.custom;

import com.github.romansorokin.resultset.annotations.ResultSetType;
import lombok.ToString;

@ToString
@ResultSetType(ignoreCase = true, mapAllFields = true)
public class CustomItem {
    String name;
}
