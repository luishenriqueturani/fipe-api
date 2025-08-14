
const a = document.querySelector(`[veiculos]`)

const carAccordion = a.querySelector(`[data-slug="carro"]`)
const truckAccordion = a.querySelector(`[data-slug="caminhao"]`)
const motorCycle = a.querySelector(`[data-slug="moto"]`)

const carBrand = document.getElementById(`selectMarcacarro`)
const carModel = document.getElementById(`selectAnoModelocarro`)
const carYear = document.getElementById(`selectAnocarro`)

const btnSearchCar = document.getElementById(`buttonPesquisarcarro`)
const resultCar = document.getElementById(`resultadoConsultacarroFiltros`)


const truckBrand = document.getElementById(`selectMarcacaminhao`)
const truckModel = document.getElementById(`selectAnoModelocaminhao`)
const truckYear = document.getElementById(`selectAnocaminhao`)

const btnSearchTruck = document.getElementById(`buttonPesquisarcaminhao`)
const resultTruck = document.getElementById(`resultadoConsultacaminhaoFiltros`)


const motorCycleBrand = document.getElementById(`selectMarcamoto`)
const motorCycleModel = document.getElementById(`selectAnoModelomoto`)
const motorCycleYear = document.getElementById(`selectAnomoto`)

const btnSearchMotorCycle = document.getElementById(`buttonPesquisarmoto`)
const resultMotorCycle = document.getElementById(`resultadoConsultamotoFiltros`)


const brands = []
const vehicleData = [] // Array to store all collected data

// Entity mappers following Java project structure
const entityMappers = {
  // Map scraped data to VehicleType entity format
  mapVehicleType: (vehicleTypeName) => ({
    name: vehicleTypeName,
    createdAt: new Date().toISOString(),
    updatedAt: null,
    deletedAt: null
  }),

  // Map scraped data to Brand entity format
  mapBrand: (brandName, vehicleTypeId, externalCode) => ({
    vehicleType: { id: vehicleTypeId },
    externalCode: externalCode || brandName.toLowerCase().replace(/\s+/g, '_'),
    name: brandName,
    createdAt: new Date().toISOString(),
    updatedAt: null,
    deletedAt: null
  }),

  // Map scraped data to Model entity format
  mapModel: (modelName, brandId, fipeCode) => ({
    brand: { id: brandId },
    fipeCode: fipeCode,
    name: modelName,
    createdAt: new Date().toISOString(),
    updatedAt: null,
    deletedAt: null
  }),

  // Map scraped data to ModelYear entity format
  mapModelYear: (modelId, yearModel, fuelCode, fuelName, yearCode) => ({
    model: { id: modelId },
    yearModel: parseInt(yearModel),
    fuelCode: fuelCode || 'G',
    fuelName: fuelName || 'Gasolina',
    yearCode: yearCode || yearModel.toString(),
    createdAt: new Date().toISOString(),
    updatedAt: null,
    deletedAt: null
  }),

  // Map scraped data to Price entity format
  mapPrice: (modelYearId, referenceMonth, value, consultedAt) => ({
    modelYear: { id: modelYearId },
    referenceMonth: referenceMonth, // Format: YYYY-MM
    value: parseFloat(value),
    currency: 'BRL',
    consultedAt: consultedAt || new Date().toISOString(),
    createdAt: new Date().toISOString(),
    updatedAt: null,
    deletedAt: null
  })
};

// Function to process extracted data and convert to entity format
function processScrapedDataToEntities(extractedData, vehicleTypeConfig, brandIndex, modelIndex, yearIndex) {
  if (!extractedData) return null;

  try {
    // Generate IDs (in real application, these would come from database)
    const vehicleTypeId = vehicleTypeConfig.name === 'cars' ? 1 : vehicleTypeConfig.name === 'trucks' ? 2 : 3;
    const brandId = vehicleTypeId * 1000 + brandIndex; // Simple ID generation
    const modelId = brandId * 1000 + modelIndex;
    const modelYearId = modelId * 1000 + yearIndex;

    // Extract fuel information from modelYear string
    const yearString = extractedData.modelYear || '';
    const yearMatch = yearString.match(/(\d{4})/);
    const fuelMatch = yearString.match(/(Flex|Diesel|Gasolina|Álcool|GNV)/i);
    
    const yearModel = yearMatch ? parseInt(yearMatch[1]) : new Date().getFullYear();
    const fuelName = fuelMatch ? fuelMatch[1] : 'Gasolina';
    const fuelCode = getFuelCode(fuelName);

    // Parse reference month from the extracted data
    const referenceMonth = parseReferenceMonth(extractedData.referenceMonth);

    // Create entities following Java structure
    const entities = {
      vehicleType: entityMappers.mapVehicleType(vehicleTypeConfig.displayName),
      brand: entityMappers.mapBrand(
        extractedData.brand, 
        vehicleTypeId, 
        `${vehicleTypeConfig.name}_${extractedData.brand?.toLowerCase().replace(/\s+/g, '_')}`
      ),
      model: entityMappers.mapModel(
        extractedData.model,
        brandId,
        extractedData.fipeCode
      ),
      modelYear: entityMappers.mapModelYear(
        modelId,
        yearModel,
        fuelCode,
        fuelName,
        `${yearModel}-${fuelCode}`
      ),
      price: entityMappers.mapPrice(
        modelYearId,
        referenceMonth,
        extractedData.averagePrice?.value || 0,
        extractedData.queryDate ? parseQueryDate(extractedData.queryDate) : new Date().toISOString()
      )
    };

    // Add generated IDs to entities
    entities.vehicleType.id = vehicleTypeId;
    entities.brand.id = brandId;
    entities.model.id = modelId;
    entities.modelYear.id = modelYearId;
    entities.price.id = modelYearId * 100 + new Date().getMonth() + 1; // Include month in price ID

    // Add original scraped data for reference
    entities.originalData = extractedData;
    entities.scrapingMetadata = {
      vehicleType: vehicleTypeConfig.name,
      brandIndex,
      modelIndex,
      yearIndex,
      collectionTimestamp: new Date().toISOString()
    };

    return entities;
  } catch (error) {
    console.error('Error processing scraped data to entities:', error);
    return null;
  }
}

// Helper function to get fuel code
function getFuelCode(fuelName) {
  const fuelMap = {
    'Gasolina': 'G',
    'Álcool': 'A',
    'Diesel': 'D',
    'Flex': 'F',
    'GNV': 'N'
  };
  return fuelMap[fuelName] || 'G';
}

// Helper function to parse reference month to YYYY-MM format
function parseReferenceMonth(referenceString) {
  if (!referenceString) return new Date().toISOString().slice(0, 7);
  
  try {
    // Expected format: "agosto de 2025" 
    const monthNames = {
      'janeiro': '01', 'fevereiro': '02', 'março': '03', 'abril': '04',
      'maio': '05', 'junho': '06', 'julho': '07', 'agosto': '08',
      'setembro': '09', 'outubro': '10', 'novembro': '11', 'dezembro': '12'
    };
    
    const parts = referenceString.toLowerCase().split(' ');
    const monthName = parts[0];
    const year = parts[2];
    
    const monthNumber = monthNames[monthName] || '01';
    return `${year}-${monthNumber}`;
  } catch (error) {
    return new Date().toISOString().slice(0, 7);
  }
}

// Helper function to parse query date
function parseQueryDate(queryDateString) {
  if (!queryDateString) return new Date().toISOString();
  
  try {
    // Expected format: "quinta-feira, 14 de agosto de 2025 17:25"
    const dateMatch = queryDateString.match(/(\d{1,2}) de (\w+) de (\d{4}) (\d{1,2}):(\d{2})/);
    if (dateMatch) {
      const [, day, monthName, year, hour, minute] = dateMatch;
      const monthNames = {
        'janeiro': 0, 'fevereiro': 1, 'março': 2, 'abril': 3,
        'maio': 4, 'junho': 5, 'julho': 6, 'agosto': 7,
        'setembro': 8, 'outubro': 9, 'novembro': 10, 'dezembro': 11
      };
      
      const month = monthNames[monthName.toLowerCase()];
      const date = new Date(parseInt(year), month, parseInt(day), parseInt(hour), parseInt(minute));
      return date.toISOString();
    }
  } catch (error) {
    console.error('Error parsing query date:', error);
  }
  
  return new Date().toISOString();
}

// Vehicle types configuration
const vehicleTypes = [
  {
    name: 'cars',
    displayName: 'Carros',
    brandSelect: carBrand,
    modelSelect: carModel,
    yearSelect: carYear,
    searchButton: btnSearchCar,
    resultContainer: resultCar
  },
  {
    name: 'trucks',
    displayName: 'Caminhões',
    brandSelect: truckBrand,
    modelSelect: truckModel,
    yearSelect: truckYear,
    searchButton: btnSearchTruck,
    resultContainer: resultTruck
  },
  {
    name: 'motorcycles',
    displayName: 'Motos',
    brandSelect: motorCycleBrand,
    modelSelect: motorCycleModel,
    yearSelect: motorCycleYear,
    searchButton: btnSearchMotorCycle,
    resultContainer: resultMotorCycle
  }
];

// Auto-save configuration
const SAVE_INTERVAL = 10; // Save every 10 models collected
let collectionActive = false;
let modelCounter = 0;
let collectionProgress = {
  currentVehicleType: 0, // 0=cars, 1=trucks, 2=motorcycles
  currentBrand: 0,
  currentModel: 0,
  currentYear: 0,
  totalCollected: 0,
  startDate: null,
  lastSaveDate: null
};

// Function to extract data from results table
function extractTableData(container = null) {
  // If container is specified, search within it, otherwise use global search
  const resultTable = container ? 
    container.querySelector('table tbody') : 
    document.querySelector('table tbody');
  
  if (!resultTable) {
    console.warn('Tabela de resultados não encontrada');
    return null;
  }

  const rows = resultTable.querySelectorAll('tr');
  const data = {};

  rows.forEach(row => {
    const cells = row.querySelectorAll('td');
    if (cells.length >= 2) {
      const label = cells[0].querySelector('p')?.textContent?.trim().replace(':', '');
      const value = cells[1].querySelector('p')?.textContent?.trim();
      
      if (label && value) {
        // Mapeia os labels para propriedades mais amigáveis
        switch (label) {
          case 'Mês de referência':
            data.referenceMonth = value;
            break;
          case 'Código Fipe':
            data.fipeCode = value;
            break;
          case 'Marca':
            data.brand = value;
            break;
          case 'Modelo':
            data.model = value;
            break;
          case 'Ano Modelo':
            data.modelYear = value;
            break;
          case 'Autenticação':
            data.authentication = value;
            break;
          case 'Data da consulta':
            data.queryDate = value;
            break;
          case 'Preço Médio':
            // Remove "R$ " and convert to number
            const cleanPrice = value.replace('R$ ', '').replace(/\./g, '').replace(',', '.');
            data.averagePrice = {
              value: parseFloat(cleanPrice),
              formattedValue: value
            };
            break;
        }
      }
    }
  });

  return data;
}

// Function to wait for results table to appear
function waitForResultTable(container = null, timeout = 5000) {
  return new Promise((resolve, reject) => {
    const startTime = Date.now();
    
    function checkTable() {
      const table = container ? 
        container.querySelector('table tbody') : 
        document.querySelector('table tbody');
      if (table && table.querySelectorAll('tr').length > 0) {
        resolve(table);
      } else if (Date.now() - startTime > timeout) {
        reject(new Error('Timeout waiting for results table'));
      } else {
        setTimeout(checkTable, 100);
      }
    }
    
    checkTable();
  });
}

// Function to save data to localStorage
function saveProgress() {
  try {
    const dataToSave = {
      vehicleData: vehicleData,
      collectionProgress: collectionProgress,
      timestamp: new Date().toISOString()
    };
    
    localStorage.setItem('fipeScraperData', JSON.stringify(dataToSave));
    localStorage.setItem('fipeScraperProgress', JSON.stringify(collectionProgress));
    
    collectionProgress.lastSaveDate = new Date().toISOString();
    
    console.log(`💾 Progress saved! Data: ${vehicleData.length} vehicles | Position: Brand ${collectionProgress.currentBrand}, Model ${collectionProgress.currentModel}`);
  } catch (error) {
    console.error('❌ Error saving progress:', error);
  }
}

// Function to load data from localStorage
function loadProgress() {
  try {
    const savedData = localStorage.getItem('fipeScraperData');
    const savedProgress = localStorage.getItem('fipeScraperProgress');
    
    if (savedData && savedProgress) {
      const data = JSON.parse(savedData);
      const progress = JSON.parse(savedProgress);
      
      // Restore data
      vehicleData.length = 0; // Clear existing array
      vehicleData.push(...data.vehicleData);
      
      // Restore progress
      collectionProgress = { ...progress };
      
      console.log(`📁 Data loaded from localStorage:`);
      console.log(`   - ${vehicleData.length} vehicles already collected`);
      console.log(`   - Last position: Brand ${collectionProgress.currentBrand}, Model ${collectionProgress.currentModel}`);
      console.log(`   - Last save: ${new Date(collectionProgress.lastSaveDate).toLocaleString()}`);
      
      return true;
    }
  } catch (error) {
    console.error('❌ Error loading progress:', error);
  }
  
  return false;
}

// Function to clear saved data
function clearSavedData() {
  localStorage.removeItem('fipeScraperData');
  localStorage.removeItem('fipeScraperProgress');
  
  vehicleData.length = 0;
  collectionProgress = {
    currentVehicleType: 0,
    currentBrand: 0,
    currentModel: 0,
    currentYear: 0,
    totalCollected: 0,
    startDate: null,
    lastSaveDate: null
  };
  
  console.log('🗑️ Saved data cleared. Ready for new collection.');
}

// Function to continue collection from where it stopped
async function continueCollection() {
  if (collectionActive) {
    console.log('⚠️ Collection is already in progress!');
    return;
  }
  
  // Load saved progress
  const progressLoaded = loadProgress();
  
  if (!progressLoaded) {
    console.log('❌ No saved progress found. Use startNewCollection() to begin.');
    return;
  }
  
  console.log('🔄 Continuing collection from where it stopped...');
  
  // Restart sequential search function
  const searchSequentially = async (startFrom = null) => {
    // [Copy the logic from searchSequentially function here]
    // This function will be called directly
    console.log('Executing collection continuation...');
    
    // Reuse main function logic
    await executeCollection(startFrom);
  };
  
  await searchSequentially();
}

// Function to start new collection (clears previous data)
async function startNewCollection() {
  if (collectionActive) {
    console.log('⚠️ Collection is already in progress!');
    return;
  }
  
  const confirm = window.confirm('🗑️ This will clear all previously saved data. Continue?');
  if (!confirm) {
    console.log('❌ Operation cancelled.');
    return;
  }
  
  clearSavedData();
  console.log('🚀 Starting new collection...');
  
  await executeCollection();
}

// Function to stop current collection
function stopCollection() {
  if (!collectionActive) {
    console.log('ℹ️ No collection in progress.');
    return;
  }
  
  collectionActive = false;
  saveProgress();
  console.log('⏸️ Collection paused and progress saved. Use continueCollection() to resume.');
}

// Centralized function to execute collection
async function executeCollection(startFrom = null) {
  collectionActive = true;
  
  // If not specified, check if there's saved progress to continue
  if (startFrom === null && collectionProgress.currentVehicleType >= 0) {
    console.log(`🔄 Continuing collection from vehicle type ${collectionProgress.currentVehicleType}`);
  } else if (startFrom === null) {
    collectionProgress.currentVehicleType = 0;
    collectionProgress.currentBrand = 0;
    collectionProgress.currentModel = 0;
    collectionProgress.currentYear = 0;
    collectionProgress.startDate = new Date().toISOString();
    console.log(`🚀 Starting new complete collection`);
  }
  
  try {
    // Loop through each vehicle type: cars, trucks, motorcycles
    for (let typeIndex = collectionProgress.currentVehicleType; typeIndex < vehicleTypes.length && collectionActive; typeIndex++) {
      const vehicleType = vehicleTypes[typeIndex];
      collectionProgress.currentVehicleType = typeIndex;
      
      console.log(`\n🚗 Starting collection for ${vehicleType.displayName} (${typeIndex + 1}/${vehicleTypes.length})`);
      
      // Get brands length for current vehicle type
      const brandsLength = vehicleType.brandSelect.options.length;
      
      // Determine starting brand index
      let startBrandIndex = (typeIndex === collectionProgress.currentVehicleType && collectionProgress.currentBrand > 0) ? 
        collectionProgress.currentBrand : 0;
      
      // Loop through all brands for current vehicle type
      for (let i = startBrandIndex; i < brandsLength && collectionActive; i++) {
        // Update current brand progress
        collectionProgress.currentBrand = i;
        collectionProgress.currentModel = 0;
        collectionProgress.currentYear = 0;
        
        // Select current brand
        vehicleType.brandSelect.selectedIndex = i;
        vehicleType.brandSelect.dispatchEvent(new Event('change'));

        // Wait for models loading
        await new Promise(resolve => setTimeout(resolve, 500));

        const modelsLength = vehicleType.modelSelect.options.length;
        console.log(`  Brand ${i + 1}/${brandsLength}: ${vehicleType.brandSelect.options[i].text} - ${modelsLength} model(s) found`);

        // Determine initial model (to continue collection if necessary)
        let startModelIndex = (typeIndex === collectionProgress.currentVehicleType && 
                              i === startBrandIndex && 
                              collectionProgress.currentModel > 0) ? 
                              collectionProgress.currentModel : 0;

        // Loop through all models of current brand
        for (let j = startModelIndex; j < modelsLength && collectionActive; j++) {
          // Skip first item if it's a placeholder
          if (j === 0 && vehicleType.modelSelect.options[j].text.includes('Selecione')) {
            continue;
          }

          // Update current model progress
          collectionProgress.currentModel = j;
          collectionProgress.currentYear = 0;

          // Select current model
          vehicleType.modelSelect.selectedIndex = j;
          vehicleType.modelSelect.dispatchEvent(new Event('change'));

          // Wait for years loading
          await new Promise(resolve => setTimeout(resolve, 500));

          const yearsLength = vehicleType.yearSelect.options.length;
          console.log(`    Model ${j}/${modelsLength}: ${vehicleType.modelSelect.options[j].text} - ${yearsLength} year(s) found`);

          // Determine initial year (to continue collection if necessary)
          let startYearIndex = (typeIndex === collectionProgress.currentVehicleType && 
                               i === startBrandIndex && 
                               j === startModelIndex && 
                               collectionProgress.currentYear > 0) ? 
                               collectionProgress.currentYear : 0;

          // Loop through all years of current model
          for (let k = startYearIndex; k < yearsLength && collectionActive; k++) {
            // Skip first item if it's a placeholder
            if (k === 0 && vehicleType.yearSelect.options[k].text.includes('Selecione')) {
              continue;
            }

            try {
              // Update current year progress
              collectionProgress.currentYear = k;
              
              // Select current year
              vehicleType.yearSelect.selectedIndex = k;
              vehicleType.yearSelect.dispatchEvent(new Event('change'));

              // Wait a bit before clicking search button
              await new Promise(resolve => setTimeout(resolve, 300));

              // Click search button
              vehicleType.searchButton.click();

              // Wait for results table to appear
              await waitForResultTable(vehicleType.resultContainer);
              
              // Extract table data
              const rawData = extractTableData(vehicleType.resultContainer);
              
              if (rawData) {
                // Process raw data into entity format
                const entityData = processScrapedDataToEntities(rawData, vehicleType, i, j, k);
                
                if (entityData) {
                  // Add to collected data array
                  vehicleData.push(entityData);
                  collectionProgress.totalCollected = vehicleData.length;
                  
                  console.log(`      ✅ Year ${k}/${yearsLength}: ${vehicleType.yearSelect.options[k].text} - ${entityData.price.value ? `R$ ${entityData.price.value.toLocaleString('pt-BR', {minimumFractionDigits: 2})}` : 'Price not found'}`);
                } else {
                  console.log(`      ⚠️ Failed to process data for year ${k}/${yearsLength}: ${vehicleType.yearSelect.options[k].text}`);
                }
              }
            } catch (error) {
              console.error(`      ❌ Error in year ${k}: ${vehicleType.yearSelect.options[k]?.text}`, error);
            }

            // Small pause between years
            await new Promise(resolve => setTimeout(resolve, 200));
          }

          // Increment model counter and save if necessary
          modelCounter++;
          if (modelCounter % SAVE_INTERVAL === 0) {
            saveProgress();
          }

          // Pause between models
          await new Promise(resolve => setTimeout(resolve, 300));
        }

        // Pause between brands
        await new Promise(resolve => setTimeout(resolve, 500));
      }
      
      // Reset progress for next vehicle type
      collectionProgress.currentBrand = 0;
      collectionProgress.currentModel = 0;
      collectionProgress.currentYear = 0;
      
      // Pause between vehicle types
      await new Promise(resolve => setTimeout(resolve, 1000));
    }
  } catch (error) {
    console.error('❌ Error during collection:', error);
    saveProgress();
  } finally {
    collectionActive = false;
    // Save final progress
    saveProgress();
  }
}

// Function to check collection status
function getCollectionStatus() {
  console.log('\n📊 COLLECTION STATUS:');
  console.log(`   Collection active: ${collectionActive ? '✅ YES' : '❌ NO'}`);
  console.log(`   Total collected: ${vehicleData.length} vehicles`);
  
  const currentVehicleType = vehicleTypes[collectionProgress.currentVehicleType] || vehicleTypes[0];
  console.log(`   Current position: ${currentVehicleType.displayName} - Brand ${collectionProgress.currentBrand + 1}, Model ${collectionProgress.currentModel + 1}, Year ${collectionProgress.currentYear + 1}`);
  
  if (collectionProgress.startDate) {
    console.log(`   Start: ${new Date(collectionProgress.startDate).toLocaleString()}`);
  }
  
  if (collectionProgress.lastSaveDate) {
    console.log(`   Last save: ${new Date(collectionProgress.lastSaveDate).toLocaleString()}`);
  }
  
  // Check if there's saved data in localStorage
  const savedData = localStorage.getItem('fipeScraperData');
  console.log(`   Data in localStorage: ${savedData ? '✅ YES' : '❌ NO'}`);
}

// ❌ OLD CODE REMOVED - Use the new control functions instead
// The collection now works with all vehicle types: cars, trucks, and motorcycles

// Função para exibir resumo da coleta
function showCollectionSummary() {
  if (vehicleData.length === 0) {
    console.log('Nenhum dado foi coletado.');
    return;
  }

  console.log('\n=== RESUMO DA COLETA ===');
  console.log(`Total de registros: ${vehicleData.length}`);
  
  // Group by brand using entity structure
  const byBrand = vehicleData.reduce((acc, item) => {
    const brandName = item.brand?.name || 'Unknown';
    const modelName = item.model?.name || 'Unknown';
    
    if (!acc[brandName]) {
      acc[brandName] = {};
    }
    if (!acc[brandName][modelName]) {
      acc[brandName][modelName] = [];
    }
    acc[brandName][modelName].push(item);
    return acc;
  }, {});

  Object.entries(byBrand).forEach(([brand, models]) => {
    const totalVehicles = Object.values(models).reduce((sum, years) => sum + years.length, 0);
    console.log(`\n📋 ${brand}: ${Object.keys(models).length} modelo(s), ${totalVehicles} registro(s)`);
    
    Object.entries(models).forEach(([model, years]) => {
      console.log(`  📱 ${model}: ${years.length} ano(s)`);
      
      // Sort by year for better visualization
      years.sort((a, b) => {
        const yearA = a.modelYear?.yearModel || 0;
        const yearB = b.modelYear?.yearModel || 0;
        return yearB - yearA; // Most recent first
      });
      
      years.forEach(vehicle => {
        const yearModel = vehicle.modelYear?.yearModel || 'N/A';
        const fuelName = vehicle.modelYear?.fuelName || '';
        const price = vehicle.price?.value ? `R$ ${vehicle.price.value.toLocaleString('pt-BR', {minimumFractionDigits: 2})}` : 'N/A';
        console.log(`    💰 ${yearModel} ${fuelName} - ${price}`);
      });
    });
  });

  // General statistics using entity structure
  const prices = vehicleData.filter(v => v.price?.value).map(v => v.price.value);
  if (prices.length > 0) {
    const averagePrice = prices.reduce((sum, price) => sum + price, 0) / prices.length;
    const minPrice = Math.min(...prices);
    const maxPrice = Math.max(...prices);
    
    console.log('\n📊 ESTATÍSTICAS GERAIS:');
    console.log(`   Preço médio: R$ ${averagePrice.toLocaleString('pt-BR', {minimumFractionDigits: 2})}`);
    console.log(`   Preço mínimo: R$ ${minPrice.toLocaleString('pt-BR', {minimumFractionDigits: 2})}`);
    console.log(`   Preço máximo: R$ ${maxPrice.toLocaleString('pt-BR', {minimumFractionDigits: 2})}`);
  }
}

// Função para exportar dados como JSON
function exportData() {
  const jsonData = JSON.stringify(vehicleData, null, 2);
  console.log('=== DADOS EM JSON ===');
  console.log(jsonData);
  
  // Cria um blob e link para download
  const blob = new Blob([jsonData], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `fipe-data-${new Date().toISOString().split('T')[0]}.json`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
  
  console.log('Arquivo JSON baixado!');
}

// Função para exportar dados como CSV
function exportCSV() {
  if (vehicleData.length === 0) {
    console.log('Nenhum dado para exportar.');
    return;
  }

  const headers = [
    // Entity IDs
    'Vehicle Type ID',
    'Brand ID',
    'Model ID',
    'Model Year ID',
    'Price ID',
    // Entity Data
    'Vehicle Type Name',
    'Brand Name',
    'Brand External Code',
    'Model Name',
    'Model Fipe Code',
    'Year Model',
    'Fuel Code',
    'Fuel Name',
    'Year Code',
    'Price Value',
    'Currency',
    'Reference Month',
    'Consulted At',
    // Timestamps
    'Vehicle Type Created At',
    'Brand Created At',
    'Model Created At',
    'Model Year Created At',
    'Price Created At',
    // Scraping Metadata
    'Vehicle Type Config',
    'Brand Index',
    'Model Index',
    'Year Index',
    'Collection Timestamp'
  ];

  const csvContent = [
    headers.join(','),
    ...vehicleData.map(item => [
      // Entity IDs
      item.vehicleType?.id || '',
      item.brand?.id || '',
      item.model?.id || '',
      item.modelYear?.id || '',
      item.price?.id || '',
      // Entity Data
      `"${item.vehicleType?.name || ''}"`,
      `"${item.brand?.name || ''}"`,
      `"${item.brand?.externalCode || ''}"`,
      `"${item.model?.name || ''}"`,
      `"${item.model?.fipeCode || ''}"`,
      item.modelYear?.yearModel || '',
      `"${item.modelYear?.fuelCode || ''}"`,
      `"${item.modelYear?.fuelName || ''}"`,
      `"${item.modelYear?.yearCode || ''}"`,
      item.price?.value || 0,
      `"${item.price?.currency || ''}"`,
      `"${item.price?.referenceMonth || ''}"`,
      `"${item.price?.consultedAt || ''}"`,
      // Timestamps
      `"${item.vehicleType?.createdAt || ''}"`,
      `"${item.brand?.createdAt || ''}"`,
      `"${item.model?.createdAt || ''}"`,
      `"${item.modelYear?.createdAt || ''}"`,
      `"${item.price?.createdAt || ''}"`,
      // Scraping Metadata
      `"${item.scrapingMetadata?.vehicleType || ''}"`,
      item.scrapingMetadata?.brandIndex || '',
      item.scrapingMetadata?.modelIndex || '',
      item.scrapingMetadata?.yearIndex || '',
      `"${item.scrapingMetadata?.collectionTimestamp || ''}"`
    ].join(','))
  ].join('\n');

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `fipe-data-${new Date().toISOString().split('T')[0]}.csv`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
  
  console.log('Arquivo CSV baixado!');
}

// Function to export entities in separate tables format (like database structure)
function exportEntitiesSeparately() {
  if (vehicleData.length === 0) {
    console.log('No data to export.');
    return;
  }

  // Extract unique entities
  const vehicleTypesMap = new Map();
  const brandsMap = new Map();
  const modelsMap = new Map();
  const modelYearsMap = new Map();
  const prices = [];

  vehicleData.forEach(item => {
    // Collect unique vehicle types
    if (item.vehicleType && !vehicleTypesMap.has(item.vehicleType.id)) {
      vehicleTypesMap.set(item.vehicleType.id, item.vehicleType);
    }

    // Collect unique brands
    if (item.brand && !brandsMap.has(item.brand.id)) {
      brandsMap.set(item.brand.id, item.brand);
    }

    // Collect unique models
    if (item.model && !modelsMap.has(item.model.id)) {
      modelsMap.set(item.model.id, item.model);
    }

    // Collect unique model years
    if (item.modelYear && !modelYearsMap.has(item.modelYear.id)) {
      modelYearsMap.set(item.modelYear.id, item.modelYear);
    }

    // Collect all prices
    if (item.price) {
      prices.push(item.price);
    }
  });

  // Export each entity type as separate CSV
  exportEntityTable('vehicle_types', Array.from(vehicleTypesMap.values()));
  exportEntityTable('brands', Array.from(brandsMap.values()));
  exportEntityTable('models', Array.from(modelsMap.values()));
  exportEntityTable('model_years', Array.from(modelYearsMap.values()));
  exportEntityTable('prices', prices);

  console.log('🎯 All entity tables exported successfully!');
}

// Helper function to export individual entity table
function exportEntityTable(tableName, entities) {
  if (entities.length === 0) return;

  const headers = Object.keys(entities[0]).filter(key => key !== null && key !== undefined);
  
  const csvContent = [
    headers.join(','),
    ...entities.map(entity => 
      headers.map(header => {
        const value = entity[header];
        if (typeof value === 'object' && value !== null) {
          return `"${JSON.stringify(value).replace(/"/g, '""')}"`;
        }
        return typeof value === 'string' ? `"${value.replace(/"/g, '""')}"` : (value || '');
      }).join(',')
    )
  ].join('\n');

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `fipe-${tableName}-${new Date().toISOString().split('T')[0]}.csv`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);

  console.log(`📊 ${tableName} table exported: ${entities.length} records`);
}

// Função para analisar tendências de preços por modelo
function analyzeTrends(brand = null, model = null) {
  let filteredData = vehicleData;
  
  if (brand) {
    filteredData = filteredData.filter(v => v.brand?.name?.toLowerCase().includes(brand.toLowerCase()));
  }
  
  if (model) {
    filteredData = filteredData.filter(v => v.model?.name?.toLowerCase().includes(model.toLowerCase()));
  }
  
  if (filteredData.length === 0) {
    console.log('Nenhum dado encontrado para os filtros especificados.');
    return;
  }
  
  // Group by model and sort by year using entity structure
  const byModel = filteredData.reduce((acc, item) => {
    const modelKey = `${item.brand?.name} - ${item.model?.name}`;
    if (!acc[modelKey]) {
      acc[modelKey] = [];
    }
    acc[modelKey].push(item);
    return acc;
  }, {});
  
  console.log('\n📈 ANÁLISE DE TENDÊNCIAS DE PREÇOS:');
  
  Object.entries(byModel).forEach(([model, records]) => {
    // Sort by year using entity structure
    records.sort((a, b) => {
      const yearA = a.modelYear?.yearModel || 0;
      const yearB = b.modelYear?.yearModel || 0;
      return yearA - yearB; // Oldest first
    });
    
    console.log(`\n🚗 ${model}:`);
    
    records.forEach((record, index) => {
      const currentPrice = record.price?.value;
      const yearModel = record.modelYear?.yearModel;
      const fuelName = record.modelYear?.fuelName;
      const priceFormatted = currentPrice ? `R$ ${currentPrice.toLocaleString('pt-BR', {minimumFractionDigits: 2})}` : 'N/A';
      
      if (currentPrice && index > 0) {
        const previousPrice = records[index - 1].price?.value;
        if (previousPrice) {
          const variation = ((currentPrice - previousPrice) / previousPrice * 100);
          const symbol = variation > 0 ? '📈' : variation < 0 ? '📉' : '➡️';
          console.log(`   ${yearModel} ${fuelName}: ${priceFormatted} ${symbol} ${variation.toFixed(1)}%`);
        } else {
          console.log(`   ${yearModel} ${fuelName}: ${priceFormatted}`);
        }
      } else {
        console.log(`   ${yearModel} ${fuelName}: ${priceFormatted}`);
      }
    });
  });
}

// Função para buscar veículos por faixa de preço
function searchByPrice(minPrice, maxPrice) {
  const result = vehicleData.filter(v => {
    const price = v.price?.value;
    return price && price >= minPrice && price <= maxPrice;
  });
  
  if (result.length === 0) {
    console.log(`Nenhum veículo encontrado na faixa de R$ ${minPrice.toLocaleString()} a R$ ${maxPrice.toLocaleString()}`);
    return [];
  }
  
  // Sort by price using entity structure
  result.sort((a, b) => a.price.value - b.price.value);
  
  console.log(`\n💰 VEÍCULOS NA FAIXA DE R$ ${minPrice.toLocaleString()} a R$ ${maxPrice.toLocaleString()}:`);
  console.log(`Encontrados: ${result.length} veículo(s)\n`);
  
  result.forEach(vehicle => {
    const brand = vehicle.brand?.name || 'Unknown';
    const model = vehicle.model?.name || 'Unknown';
    const year = vehicle.modelYear?.yearModel || 'N/A';
    const fuel = vehicle.modelYear?.fuelName || '';
    const price = `R$ ${vehicle.price.value.toLocaleString('pt-BR', {minimumFractionDigits: 2})}`;
    console.log(`${brand} ${model} (${year} ${fuel}) - ${price}`);
  });
  
  return result;
}

// Add global functions for console usage
window.exportData = exportData;
window.exportCSV = exportCSV;
window.exportEntitiesSeparately = exportEntitiesSeparately;
window.analyzeTrends = analyzeTrends;
window.searchByPrice = searchByPrice;
window.vehicleData = vehicleData;

// Collection control functions
window.startNewCollection = startNewCollection;
window.continueCollection = continueCollection;
window.stopCollection = stopCollection;
window.getCollectionStatus = getCollectionStatus;
window.saveProgress = saveProgress;
window.loadProgress = loadProgress;
window.clearSavedData = clearSavedData;



console.log(`
=== FIPE SCRAPER WITH RECOVERY SYSTEM ===

🔄 ADVANCED COLLECTION: Collects ALL years of ALL models with backup system!

💾 RECOVERY SYSTEM:
- Auto-save every 10 models to localStorage
- Automatic recovery in case of interruption
- Total control over collection process

🎮 CONTROL FUNCTIONS:
- startNewCollection() - Start collection from scratch (clears previous data)
- continueCollection() - Continue from where it stopped
- stopCollection() - Pause and save progress
- getCollectionStatus() - Check current status
- loadProgress() - Load saved data
- clearSavedData() - Clear localStorage

📊 ANALYSIS FUNCTIONS:
- exportData() - Export as JSON
- exportCSV() - Export as expanded CSV (entity format)
- exportEntitiesSeparately() - Export separate entity tables (like database)
- analyzeTrends(brand?, model?) - Analyze price variations
- searchByPrice(min, max) - Search by price range

🚀 HOW TO USE:
1. startNewCollection() - To start new collection
2. continueCollection() - To resume interrupted collection
3. getCollectionStatus() - To check progress

💡 The system auto-saves and allows total recovery!
`);

// Check if there's saved data and inform user
const savedData = localStorage.getItem('fipeScraperData');
if (savedData) {
  console.log('⚠️ SAVED DATA FOUND! Use continueCollection() to resume or startNewCollection() to start from scratch.');
  loadProgress(); // Load automatically to show status
}


