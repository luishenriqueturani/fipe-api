package com.exemplo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public class FipeDataDtos {

    public static class FipeDataRequest {
        public List<Car> cars;
        public List<Truck> trucks;
        public List<MotorCycle> motorCycles;
    }

    public static class Car {
        public Long id;
        public String name;
        public List<Model> models;
    }

    public static class Truck {
        public Long id;
        public String name;
        public List<Model> models;
    }

    public static class MotorCycle {
        public Long id;
        public String name;
        public List<Model> models;
    }

    public static class Model {
        public Long id;
        public String name;
        public List<Year> years;
    }

    public static class Year {
        @JsonProperty("referenceMonth")
        public String referenceMonth;
        
        @JsonProperty("fipeCode")
        public String fipeCode;
        
        public String brand;
        public String model;
        
        @JsonProperty("modelYear")
        public String modelYear;
        
        public String authentication;
        
        @JsonProperty("queryDate")
        public String queryDate;
        
        @JsonProperty("averagePrice")
        public AveragePrice averagePrice;
    }

    public static class AveragePrice {
        public BigDecimal value;
        
        @JsonProperty("formattedValue")
        public String formattedValue;
    }
}
