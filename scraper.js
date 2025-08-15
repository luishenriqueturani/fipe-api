
const a = document.querySelector(`[veiculos]`)

const carAccordion = a.querySelector(`[data-slug="carro"]`)
const truckAccordion = a.querySelector(`[data-slug="caminhao"]`)
const motorCycle = a.querySelector(`[data-slug="moto"]`)


const vehicleData = []


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

async function collectCars() {
  carAccordion.click()
  await new Promise(resolve => setTimeout(resolve, 500))

  const carBrand = document.getElementById(`selectMarcacarro`)
  const carModel = document.getElementById(`selectAnoModelocarro`)
  const carYear = document.getElementById(`selectAnocarro`)

  const btnSearchCar = document.getElementById(`buttonPesquisarcarro`)


  let carsLength = carBrand.options.length

  let brands = []
  let models = []
  let years = []

  for (let i = 1; i < carsLength; i++) {
    carBrand.selectedIndex = i
    carBrand.dispatchEvent(new Event('change'))


    await new Promise(resolve => setTimeout(resolve, 500))

    let modelsLength = carModel.options.length

    for (let j = 1; j < modelsLength; j++) {
      carModel.selectedIndex = j
      carModel.dispatchEvent(new Event('change'))


      await new Promise(resolve => setTimeout(resolve, 500))

      let yearsLength = carYear.options.length

      for (let k = 1; k < yearsLength; k++) {
        carYear.selectedIndex = k
        carYear.dispatchEvent(new Event('change'))


        await new Promise(resolve => setTimeout(resolve, 500))

        btnSearchCar.click()

        await new Promise(resolve => setTimeout(resolve, 500))

        const resultCar = document.getElementById(`resultadoConsultacarroFiltros`)

        const tableData = extractTableData(resultCar)

        if (tableData) {
          //vehicleData.push(tableData)
          years.push(tableData)
        }
      }

      models.push({
        id: j,
        name: carModel.options[j].text || '',
        years: years
      })

      years = []
      await new Promise(resolve => setTimeout(resolve, 500))
    }


    brands.push({
      id: i,
      name: carBrand.options[i].text || '',
      models: models
    })

    models = []
    await new Promise(resolve => setTimeout(resolve, 500))
  }

  return brands
}

async function collectTrucks() {

  truckAccordion.click()

  await new Promise(resolve => setTimeout(resolve, 500))

  const truckBrand = document.getElementById(`selectMarcacaminhao`)
  const truckModel = document.getElementById(`selectAnoModelocaminhao`)
  const truckYear = document.getElementById(`selectAnocaminhao`)
  
  const btnSearchTruck = document.getElementById(`buttonPesquisarcaminhao`)


  let trucksLength = truckBrand.options.length

  let brands = []
  let models = []
  let years = []

  for (let i = 1; i < trucksLength; i++) {
    truckBrand.selectedIndex = i
    truckBrand.dispatchEvent(new Event('change'))

    await new Promise(resolve => setTimeout(resolve, 500))

    let modelsLength = truckModel.options.length

    for (let j = 1; j < modelsLength; j++) {
      truckModel.selectedIndex = j
      truckModel.dispatchEvent(new Event('change'))

      await new Promise(resolve => setTimeout(resolve, 500))

      let yearsLength = truckYear.options.length

      for (let k = 1; k < yearsLength; k++) {
        truckYear.selectedIndex = k
        truckYear.dispatchEvent(new Event('change'))

        await new Promise(resolve => setTimeout(resolve, 500))

        btnSearchTruck.click()

        await new Promise(resolve => setTimeout(resolve, 500))

        const resultTruck = document.getElementById(`resultadoConsultacaminhaoFiltros`)

        const tableData = extractTableData(resultTruck)

        if (tableData) {
          //vehicleData.push(tableData)
          years.push(tableData)
        }
      }
      models.push({
        id: j,
        name: truckModel.options[j].text || '',
        years: years
      })

      years = []
      await new Promise(resolve => setTimeout(resolve, 500))
    }

    brands.push({
      id: i,
      name: truckBrand.options[i].text || '',
      models: models
    })

    models = []
    await new Promise(resolve => setTimeout(resolve, 500))
  }

  return brands
}

async function collectMotorCycles() {

  motorCycle.click()

  await new Promise(resolve => setTimeout(resolve, 500))
  
  const motorCycleBrand = document.getElementById(`selectMarcamoto`)
  const motorCycleModel = document.getElementById(`selectAnoModelomoto`)
  const motorCycleYear = document.getElementById(`selectAnomoto`)
  
  const btnSearchMotorCycle = document.getElementById(`buttonPesquisarmoto`)


  let motorCyclesLength = motorCycleBrand.options.length

  let brands = []
  let models = []
  let years = []

  for (let i = 1; i < motorCyclesLength; i++) {
    motorCycleBrand.selectedIndex = i
    motorCycleBrand.dispatchEvent(new Event('change'))

    await new Promise(resolve => setTimeout(resolve, 500))

    let modelsLength = motorCycleModel.options.length

    for (let j = 1; j < modelsLength; j++) {
      motorCycleModel.selectedIndex = j
      motorCycleModel.dispatchEvent(new Event('change'))

      await new Promise(resolve => setTimeout(resolve, 500))

      let yearsLength = motorCycleYear.options.length

      for (let k = 1; k < yearsLength; k++) {
        motorCycleYear.selectedIndex = k
        motorCycleYear.dispatchEvent(new Event('change'))

        await new Promise(resolve => setTimeout(resolve, 500))

        btnSearchMotorCycle.click()

        await new Promise(resolve => setTimeout(resolve, 500))

        const resultMotorCycle = document.getElementById(`resultadoConsultamotoFiltros`)

        const tableData = extractTableData(resultMotorCycle)

        if (tableData) {
          //vehicleData.push(tableData)
          years.push(tableData)
        }
      }

      models.push({
        id: j,
        name: motorCycleModel.options[j].text || '',
        years: years
      })

      years = []
      await new Promise(resolve => setTimeout(resolve, 500))
    }

    brands.push({
      id: i,
      name: motorCycleBrand.options[i].text || '',
      models: models
    })

    models = []
    await new Promise(resolve => setTimeout(resolve, 500))
  }

  return brands
}

async function init() {
  const cars = await collectCars()
  const trucks = await collectTrucks()
  const motorCycles = await collectMotorCycles()

  const data = {
    cars: cars,
    trucks: trucks,
    motorCycles: motorCycles
  }

  const blob = new Blob([JSON.stringify(data)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `fipe-data-${new Date().toISOString().split('T')[0]}.json`
  a.click()

  URL.revokeObjectURL(url)

}

init()