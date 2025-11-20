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
import java.util.Locale;
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
        // Obter o primeiro fipeCode dos years para usar no Model (já que cada year tem seu próprio fipeCode)
        String modelFipeCode = model.years != null && !model.years.isEmpty() 
            ? extractBaseFipeCode(model.years.get(0).fipeCode) 
            : model.id.toString();
        
        com.exemplo.entities.Model modelEntity = getOrCreateModel(brand, model.name, model.id.toString(), modelFipeCode);

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

  private com.exemplo.entities.Model getOrCreateModel(Brand brand, String name, String externalCode, String fipeCode) {
    // Buscar por brand + name (mais confiável que apenas name)
    com.exemplo.entities.Model model = com.exemplo.entities.Model.find(
        "brand.id = ?1 and name = ?2 and deletedAt is null", brand.id, name).firstResult();

    // Separar modelo e versão do nome completo
    ModelVersionParser.ParseResult parseResult = ModelVersionParser.parse(name);

    if (model == null) {
      // Verificar se já existe um modelo com o mesmo fipeCode (para evitar duplicação)
      com.exemplo.entities.Model existingByFipeCode = com.exemplo.entities.Model.find(
          "fipeCode = ?1 and deletedAt is null", fipeCode).firstResult();
      
      if (existingByFipeCode != null) {
        LOG.warn("Modelo com fipeCode " + fipeCode + " já existe, mas com nome diferente. Usando existente.");
        // Atualizar nome, modelo e versão mesmo se já existir
        boolean updated = false;
        if (!existingByFipeCode.name.equals(name)) {
          existingByFipeCode.name = name;
          updated = true;
        }
        if (existingByFipeCode.model == null || !existingByFipeCode.model.equals(parseResult.model)) {
          existingByFipeCode.model = parseResult.model;
          updated = true;
        }
        String versionToSet = parseResult.version.isEmpty() ? null : parseResult.version;
        if ((existingByFipeCode.version == null && versionToSet != null) || 
            (existingByFipeCode.version != null && !existingByFipeCode.version.equals(versionToSet))) {
          existingByFipeCode.version = versionToSet;
          updated = true;
        }
        if (updated) {
          existingByFipeCode.persist();
          LOG.info("Atualizado modelo existente (por fipeCode): " + name + " (model: " + parseResult.model + ", version: " + parseResult.version + ")");
        }
        return existingByFipeCode;
      }
      
      model = new com.exemplo.entities.Model();
      model.brand = brand;
      model.name = name;
      model.model = parseResult.model;
      model.version = parseResult.version.isEmpty() ? null : parseResult.version;
      model.fipeCode = fipeCode;
      model.persist();
      LOG.info("Criado novo modelo: " + name + " (model: " + parseResult.model + ", version: " + parseResult.version + ") com fipeCode: " + fipeCode);
    } else {
      // Atualizar nome, modelo, versão e fipeCode se necessário
      boolean updated = false;
      if (!model.name.equals(name)) {
        model.name = name;
        updated = true;
      }
      if (model.model == null || !model.model.equals(parseResult.model)) {
        model.model = parseResult.model;
        updated = true;
      }
      String versionToSet = parseResult.version.isEmpty() ? null : parseResult.version;
      if ((model.version == null && versionToSet != null) || 
          (model.version != null && !model.version.equals(versionToSet))) {
        model.version = versionToSet;
        updated = true;
      }
      if (!model.fipeCode.equals(fipeCode)) {
        model.fipeCode = fipeCode;
        updated = true;
      }
      if (updated) {
        model.persist();
        LOG.info("Atualizado modelo: " + name + " (model: " + parseResult.model + ", version: " + parseResult.version + ")");
      }
    }
    return model;
  }

  private ModelYear getOrCreateModelYear(com.exemplo.entities.Model model, Year year) {
    // Buscar por model + fipeCode (identificador único do ModelYear)
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
      LOG.info("Criado novo ano de modelo: " + year.modelYear + " com fipeCode: " + year.fipeCode);
    } else {
      // Atualizar dados se necessário
      boolean updated = false;
      
      if (!modelYear.authentication.equals(year.authentication)) {
        modelYear.authentication = year.authentication;
        updated = true;
      }
      
      // Verificar se os dados do ano/combustível mudaram
      Matcher matcher = MODEL_YEAR_PATTERN.matcher(year.modelYear);
      if (matcher.matches()) {
        int newYearModel = Integer.parseInt(matcher.group(1));
        String newFuelInfo = matcher.group(2);
        String newFuelCode = generateFuelCode(newFuelInfo);
        
        if (!modelYear.yearModel.equals(newYearModel)) {
          modelYear.yearModel = newYearModel;
          updated = true;
        }
        if (!modelYear.fuelName.equals(newFuelInfo)) {
          modelYear.fuelName = newFuelInfo;
          updated = true;
        }
        if (!modelYear.fuelCode.equals(newFuelCode)) {
          modelYear.fuelCode = newFuelCode;
          updated = true;
        }
        if (!modelYear.yearCode.equals(year.modelYear)) {
          modelYear.yearCode = year.modelYear;
          updated = true;
        }
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

  /**
   * Extrai o código base do fipeCode (remove sufixos como "-2", "-9" para usar no Model)
   * Exemplo: "038003-2" -> "038003"
   */
  private String extractBaseFipeCode(String fipeCode) {
    if (fipeCode == null || fipeCode.isEmpty()) {
      return fipeCode;
    }
    int dashIndex = fipeCode.indexOf('-');
    return dashIndex > 0 ? fipeCode.substring(0, dashIndex) : fipeCode;
  }

  private LocalDateTime parseQueryDate(String queryDate) {
    try {
      // Formato: "terça-feira, 2 de setembro de 2025 09:33"
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy HH:mm", new Locale("pt", "BR"));
      return LocalDateTime.parse(queryDate, formatter);
    } catch (Exception e) {
      LOG.warn("Erro ao fazer parse da data: " + queryDate + ", usando data atual", e);
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
