package com.exemplo.services;

import com.exemplo.dto.FipeDataDtos;
import com.exemplo.dto.FipeDataDtos.*;
import com.exemplo.entities.*;
import com.exemplo.enums.Currency;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class FipeDataService {

  private static final Logger LOG = Logger.getLogger(FipeDataService.class);

  // Padrão para extrair ano e combustível do modelYear (ex: "1992 Gasolina",
  // "2022 Diesel")
  private static final Pattern MODEL_YEAR_PATTERN = Pattern.compile("^(\\d{4})\\s+(.+)$");

  @Transactional
  public void processFipeData(FipeDataRequest request) {
    LOG.info("Iniciando processamento dos dados da FIPE");

    // Processar carros
    if (request.cars != null) {
      List<VehicleData> carWrappers = request.cars.stream()
          .map(CarWrapper::new)
          .collect(java.util.stream.Collectors.toList());
      processVehicleType(carWrappers, "Carros");
    }

    // Processar caminhões
    if (request.trucks != null) {
      List<VehicleData> truckWrappers = request.trucks.stream()
          .map(TruckWrapper::new)
          .collect(java.util.stream.Collectors.toList());
      processVehicleType(truckWrappers, "Caminhões");
    }

    // Processar motocicletas
    if (request.motorCycles != null) {
      List<VehicleData> motorCycleWrappers = request.motorCycles.stream()
          .map(MotorCycleWrapper::new)
          .collect(java.util.stream.Collectors.toList());
      processVehicleType(motorCycleWrappers, "Motocicletas");
    }

    LOG.info("Processamento dos dados da FIPE concluído");
  }

  private void processVehicleType(List<? extends VehicleData> vehicles, String vehicleTypeName) {
    VehicleType vehicleType = getOrCreateVehicleType(vehicleTypeName);

    for (VehicleData vehicle : vehicles) {
      Brand brand = getOrCreateBrand(vehicleType, vehicle.getName(), vehicle.getId().toString());

      for (FipeDataDtos.Model model : vehicle.getModels()) {
        com.exemplo.entities.Model modelEntity = getOrCreateModel(brand, model.name, model.id.toString());

        for (Year year : model.years) {
          ModelYear modelYear = getOrCreateModelYear(modelEntity, year);
          createOrUpdatePrice(modelYear, year);
        }
      }
    }
  }

  private VehicleType getOrCreateVehicleType(String name) {
    VehicleType vehicleType = VehicleType.find("name = ?1 and deletedAt is null", name).firstResult();
    if (vehicleType == null) {
      vehicleType = new VehicleType();
      vehicleType.name = name;
      vehicleType.persist();
      LOG.info("Criado novo tipo de veículo: " + name);
    }
    return vehicleType;
  }

  private Brand getOrCreateBrand(VehicleType vehicleType, String name, String externalCode) {
    Brand brand = Brand.find("vehicleType.id = ?1 and externalCode = ?2 and deletedAt is null",
        vehicleType.id, externalCode).firstResult();
    if (brand == null) {
      brand = new Brand();
      brand.vehicleType = vehicleType;
      brand.externalCode = externalCode;
      brand.name = name;
      brand.persist();
      LOG.info("Criada nova marca: " + name);
    } else {
      // Atualizar nome se necessário
      if (!brand.name.equals(name)) {
        brand.name = name;
        brand.persist();
        LOG.info("Atualizada marca: " + name);
      }
    }
    return brand;
  }

  private com.exemplo.entities.Model getOrCreateModel(Brand brand, String name, String externalCode) {
    com.exemplo.entities.Model model = com.exemplo.entities.Model.find(
        "brand.id = ?1 and name = ?2 and deletedAt is null", brand.id, name).firstResult();

    if (model == null) {
      model = new com.exemplo.entities.Model();
      model.brand = brand;
      model.name = name;
      model.fipeCode = externalCode; // Usando o ID como fipeCode temporariamente
      model.persist();
      LOG.info("Criado novo modelo: " + name);
    } else {
      // Atualizar nome se necessário
      if (!model.name.equals(name)) {
        model.name = name;
        model.persist();
        LOG.info("Atualizado modelo: " + name);
      }
    }
    return model;
  }

  private ModelYear getOrCreateModelYear(com.exemplo.entities.Model model, Year year) {
    ModelYear modelYear = ModelYear.find("model.id = ?1 and fipeCode = ?2 and deletedAt is null",
        model.id, year.fipeCode).firstResult();

    if (modelYear == null) {
      modelYear = new ModelYear();
      modelYear.model = model;
      modelYear.fipeCode = year.fipeCode;
      modelYear.authentication = year.authentication;

      // Extrair ano e combustível do modelYear
      Matcher matcher = MODEL_YEAR_PATTERN.matcher(year.modelYear);
      if (matcher.matches()) {
        modelYear.yearModel = Integer.parseInt(matcher.group(1));
        String fuelInfo = matcher.group(2);
        modelYear.fuelName = fuelInfo;
        modelYear.fuelCode = generateFuelCode(fuelInfo);
        modelYear.yearCode = year.modelYear;
      } else {
        // Fallback se não conseguir extrair
        modelYear.yearModel = 0;
        modelYear.fuelName = year.modelYear;
        modelYear.fuelCode = "UNK";
        modelYear.yearCode = year.modelYear;
      }

      modelYear.persist();
      LOG.info("Criado novo ano de modelo: " + year.modelYear);
    } else {
      // Atualizar dados se necessário
      boolean updated = false;
      if (!modelYear.authentication.equals(year.authentication)) {
        modelYear.authentication = year.authentication;
        updated = true;
      }

      if (updated) {
        modelYear.persist();
        LOG.info("Atualizado ano de modelo: " + year.modelYear);
      }
    }
    return modelYear;
  }

  private void createOrUpdatePrice(ModelYear modelYear, Year year) {
    Price price = Price.find("modelYear.id = ?1 and referenceMonth = ?2 and deletedAt is null",
        modelYear.id, year.referenceMonth).firstResult();

    if (price == null) {
      price = new Price();
      price.modelYear = modelYear;
      price.referenceMonth = year.referenceMonth;
      price.value = year.averagePrice.value;
      price.currency = Currency.BRL;
      price.authentication = year.authentication;
      price.consultedAt = parseQueryDate(year.queryDate);
      price.persist();
      LOG.info("Criado novo preço para " + year.referenceMonth + ": R$ " + year.averagePrice.formattedValue);
    } else {
      // Atualizar preço e data de consulta
      boolean updated = false;
      if (!price.value.equals(year.averagePrice.value)) {
        price.value = year.averagePrice.value;
        updated = true;
      }
      if (!price.authentication.equals(year.authentication)) {
        price.authentication = year.authentication;
        updated = true;
      }
      LocalDateTime newConsultedAt = parseQueryDate(year.queryDate);
      if (!price.consultedAt.equals(newConsultedAt)) {
        price.consultedAt = newConsultedAt;
        updated = true;
      }

      if (updated) {
        price.persist();
        LOG.info("Atualizado preço para " + year.referenceMonth + ": R$ " + year.averagePrice.formattedValue);
      }
    }
  }

  private String generateFuelCode(String fuelName) {
    if (fuelName.toLowerCase().contains("gasolina")) {
      return "GAS";
    } else if (fuelName.toLowerCase().contains("diesel")) {
      return "DIE";
    } else if (fuelName.toLowerCase().contains("etanol")) {
      return "ETA";
    } else if (fuelName.toLowerCase().contains("flex")) {
      return "FLE";
    } else if (fuelName.toLowerCase().contains("elétrico")) {
      return "ELE";
    } else {
      return "UNK";
    }
  }

  private LocalDateTime parseQueryDate(String queryDate) {
    try {
      // Formato: "terça-feira, 2 de setembro de 2025 09:33"
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy HH:mm");
      return LocalDateTime.parse(queryDate, formatter);
    } catch (Exception e) {
      LOG.warn("Erro ao fazer parse da data: " + queryDate + ", usando data atual");
      return LocalDateTime.now();
    }
  }

  // Interface para unificar os tipos de veículo
  private interface VehicleData {
    Long getId();

    String getName();

    List<FipeDataDtos.Model> getModels();
  }

  // Implementações da interface
  private static class CarWrapper implements VehicleData {
    private final Car car;

    public CarWrapper(Car car) {
      this.car = car;
    }

    public Long getId() {
      return car.id;
    }

    public String getName() {
      return car.name;
    }

    public List<FipeDataDtos.Model> getModels() {
      return car.models;
    }
  }

  private static class TruckWrapper implements VehicleData {
    private final Truck truck;

    public TruckWrapper(Truck truck) {
      this.truck = truck;
    }

    public Long getId() {
      return truck.id;
    }

    public String getName() {
      return truck.name;
    }

    public List<FipeDataDtos.Model> getModels() {
      return truck.models;
    }
  }

  private static class MotorCycleWrapper implements VehicleData {
    private final MotorCycle motorCycle;

    public MotorCycleWrapper(MotorCycle motorCycle) {
      this.motorCycle = motorCycle;
    }

    public Long getId() {
      return motorCycle.id;
    }

    public String getName() {
      return motorCycle.name;
    }

    public List<FipeDataDtos.Model> getModels() {
      return motorCycle.models;
    }
  }
}
