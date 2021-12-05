// Endpoints - Gross & Net balance
const getGrossBalanceByProduct = '/twOverall/getGrossBalanceByProduct';
const getNetBalanceByProduct = '/twSupply/getNetBalanceByProduct';

// Endpoints - Supply
const getPrimaryEnergySupply = '/twSupply/getPrimaryEnergySupply';
const getDistinctProducts = '/twOverall/getDistinctProducts';
const getTypeBreakdownOfProduct = '/twSupply/getTypeBreakdownOfProduct';

// Endpoints - Consumption
const getDomesticEnergyConsumption = 'twConsumption/getDomesticEnergyConsumption?'
const getProductBreakdown = 'twConsumption/getProductBreakdown?'
const getProductSectorBreakdown = '/twConsumption/getProductSectorBreakdown?'
const getSectorsSubSectors = '/twConsumption/getSectorSubSectorBreakdown?';
const getSectorProductBreakdown_endpoint = '/twConsumption/getProductBreakdown?'

const getDistinctSectorsSubSectors = '/twConsumption/getSectorsSubSectors'

// Chart objects - Gross & Net balance
let grossBalanceChart = null;
let netBalanceChart = null;

// Chart objects - Supply
let primaryEnergySupplyChart = null;
let typeBreakdownOfProductChart = null;

// Chart objects - Consumption
let productVolumeLineChart = null;
let productDoughnutBreakdown = null;
let sectorVolumeBarChart = null;
let subSectorBarCharts = null;
let sectorProductBarChart = null;

// Colors for charts
const colorsRGBA = [
    'rgba(0, 63, 92, 1.0)',
    'rgba(55, 76, 128, 1.0)',
    'rgba(122, 81, 149, 1.0)',
    'rgba(188, 80, 144, 1.0)',
    'rgba(239, 86, 117, 1.0)',
    'rgba(255, 118, 74, 1.0)',
    'rgba(255, 166, 0, 1.0)'
];

// Colors for charts
const colorsArr = [
    [0, 63, 92],
    [55, 76, 128],
    [122, 81, 149],
    [188, 80, 144],
    [239, 86, 117],
    [255, 118, 74],
    [255, 166, 0]
];

const months_conversion = {
    'Jan': 1,
    1: 'Jan',
    'Feb': 2,
    2: 'Feb',
    'Mar': 3,
    3: 'Mar',
    'Apr': 4,
    4: 'Apr',
    'May': 5,
    5: 'May',
    'Jun': 6,
    6: 'Jun',
    'Jul': 7,
    7: 'Jul',
    'Aug': 8,
    8: 'Aug',
    'Sep': 9,
    9: 'Sep',
    'Oct': 10,
    10: 'Oct',
    'Nov': 11,
    11: 'Nov',
    'Dec': 12,
    12: 'Dec'
}

async function makeAPIRequest(endpoint) {
    try {
        // Response object
        const response = await fetch(endpoint);
        // Response object's JSON body --> JavaScript Object
        const parsedJson = await response.json();

        return parsedJson;

    } catch(error) {
        console.log(error);
        return error;
    }
}

var year_chosen = null;

// Onload
async function createAllCharts() {
    // Gross & Net balance charts
    await createGrossOrNetBalanceChart(true, 'new'); // Gross Balance Chart
    await createGrossOrNetBalanceChart(false, 'new'); // Net Balance Chart

    // Supply
    await createPrimaryEnergySupplyChart('new');
    await createSupplyProductDropdown();
    let selectedProduct = document.getElementById('typesDropdown').value;
    await createTypeBreakdownOfProductChart('new', selectedProduct);

    // Consumption
    await createProductVolumeLineChart();
    await createProductBreakDownDoughnut();

    // Consumption dropdowns
    await getProductBreakdownSector();
    await getSectorProductBreakdown();

    // let c_dropdown = document.getElementById("c_typesDropdown");
}

/* isGross == true --> if referring to the Gross Balance chart
   type == 'new' --> if building new chart (onload)
   type == 'update' --> if updating chart (onclick or reset button)
   Optional default arguments are for all years/reset button cases
   Insert time == 'month' and year == 2019 for onclick case */
async function createGrossOrNetBalanceChart(isGross, type, time = 'year', year = null) {
    let endpoint = getNetBalanceByProduct;

    if (isGross) {
        endpoint = getGrossBalanceByProduct;
    }

    if (time == 'year') {
        endpoint += '?selectParam=year';
    } else {
        endpoint += '?selectParam=month&year=' + year;
    }

    let response = await makeAPIRequest(endpoint);

    let timeArr = response['time'];
    let products = response['products'];

    // Sorting the product names in alphabetical order
    let sortedProducts = Object.keys(products).sort()

    // List of datasets for chart object
    const datasets = [];

    for (let i = 0; i < sortedProducts.length; i++) {
        let productArray = products[sortedProducts[i]];

        for (let x = 0; x < productArray.length; x++) {
            // Round the values to be displayed on the graph
            productArray[x] = Math.ceil(productArray[x]);
        }

        datasets.push({
            label: sortedProducts[i],
            backgroundColor: colorsRGBA[i],
            borderColor: colorsRGBA[i],
            data: productArray,
            pointBorderColor: 'rgb(0, 0, 0)'
        });
    }

    // If data is in months, convert int months to string
    let yearsOrMonths = [];

    if (time == 'year') {
        yearsOrMonths = timeArr;
    } else {
        for (let i = 0; i < timeArr.length; i++) {
            yearsOrMonths.push(months_conversion[timeArr[i]]);
        }
    }

    const data = {
        labels: yearsOrMonths,
        datasets: datasets
    };

    // Plugins block
    const legendMargin = {
        id: 'legendMargin',
        beforeInit(chart, legend, options) {
            const fitValue = chart.legend.fit;

            chart.legend.fit = function fit() {
                fitValue.bind(chart.legend)();
                return this.height += 23;
            }
        }
    };

    // Config
    const config = {
        // Type of chart
        type: 'line',

        // Data for the dataset - defined above
        data: data,

        // Configuration options
        options: {
            plugins: {
                legend: {
                    labels: {
                        usePointStyle: true
                    },
                },
                title: {
                    display: false,
                }
            },
            scales: {
                y: {
                    title: {
                        display: true,
                        text: 'KBD',
                        padding: {top: 0, left: 0, right: 0, bottom: 0}
                    }
                }
            }
        },

        plugins: [legendMargin]
    };

    if (type == 'new') {
        if (isGross) {
            grossBalanceChart = new Chart(
                // Indicate which Canvas Element to draw chart
                document.getElementById('grossBalanceChart'),
                config
            );

        } else {
            netBalanceChart = new Chart(
                // Indicate which Canvas Element to draw chart
                document.getElementById('netBalanceChart'),
                config
            );
        }

    } else {
        let chartObject = netBalanceChart;

        if (isGross) {
            chartObject = grossBalanceChart;
        }

        chartObject.data = data;
        chartObject.update();
    }
}

// Onclick for grossBalanceChart
let grossBalance_count = 0;
const grossBalance_ctx = document.getElementById("grossBalanceChart");
grossBalance_ctx.onclick = grossBalanceChart_clickHandler;

async function grossBalanceChart_clickHandler(click) {
    let points = grossBalanceChart.getElementsAtEventForMode(click, 'nearest', {intersect: true}, true);

    if(grossBalance_count == 0){
        if (points.length) {
            let firstPoint = points[0];

            let year_clicked = grossBalanceChart.data.labels[firstPoint.index];

            await updateGrossNetCharts(year_clicked);
        }
    }
}

// Onclick for netBalanceChart
let netBalance_count = 0;
const netBalance_ctx = document.getElementById("netBalanceChart");
netBalance_ctx.onclick = netBalanceChart_clickHandler;

async function netBalanceChart_clickHandler(click) {
    let points = netBalanceChart.getElementsAtEventForMode(click, 'nearest', {intersect: true}, true);

    if(netBalance_count == 0){
        if (points.length) {
            let firstPoint = points[0];

            let year_clicked = netBalanceChart.data.labels[firstPoint.index];

            await updateGrossNetCharts(year_clicked);
        }
    }
}

// By default, catered for reset button case
// Insert a year for onclick case
async function updateGrossNetCharts(year_clicked = null) {
    let year = '';

    if (year_clicked == null) { // For reset button case
        await createGrossOrNetBalanceChart(true, 'update'); // Gross Balance Chart
        await createGrossOrNetBalanceChart(false, 'update'); // Net Balance Chart

        grossBalance_count = 0;
        netBalance_count = 0;

        year = 'Across Years';

    } else {
        // Updating count to prevent person from clicking on chart again
        // Until they pressed the reset button
        grossBalance_count++;
        netBalance_count++;

        await createGrossOrNetBalanceChart(true, 'update', 'month', year_clicked); // Gross Balance Chart
        await createGrossOrNetBalanceChart(false, 'update', 'month', year_clicked); // Net Balance Chart

        year = 'Year ' + year_clicked;
    }

    document.getElementById('grossNetReset').classList.toggle('d-none');

    document.getElementById('grossNetYear').innerText = year;
}

// Default arguments are for all years/reset button cases
// Insert 'month' and a year '2019' for onclick case
async function createPrimaryEnergySupplyChart(type, time = 'year', year = null) {
    let response = await makeAPIRequest(getPrimaryEnergySupply + '?selectParam=year');

    if (time == 'month') {
        response = await makeAPIRequest(getPrimaryEnergySupply + '?selectParam=month&year=' + year);
    }

    let timeArr = response['time'];
    let products = response['products'];

    // Sorting the product names in alphabetical order
    let sortedProducts = Object.keys(products).sort()

    // List of datasets for chart object
    const datasets = [];

    for (let i = 0; i < sortedProducts.length; i++) {
        let productArray = products[sortedProducts[i]];

        for (let x = 0; x < productArray.length; x++) {
            let rawValue = productArray[x];

            // Round the values to be displayed on the graph
            productArray[x] = Math.ceil(rawValue);
        }

        datasets.push({
            label: sortedProducts[i],
            backgroundColor: colorsRGBA[i],
            borderColor: colorsRGBA[i],
            data: productArray,
            pointBorderColor: 'rgb(0, 0, 0)'
        });
    }

    // If data is in months, convert int months to string
    let yearsOrMonths = [];

    if (time == 'year') {
        yearsOrMonths = timeArr;
    } else {
        for (let i = 0; i < timeArr.length; i++) {
            yearsOrMonths.push(months_conversion[timeArr[i]]);
        }
    }

    const data = {
        labels: yearsOrMonths,
        datasets: datasets
    };

    // Plugins block
    const legendMargin = {
        id: 'legendMargin',
        beforeInit(chart, legend, options) {
            const fitValue = chart.legend.fit;

            chart.legend.fit = function fit() {
                fitValue.bind(chart.legend)();
                return this.height += 23;
            }
        }
    };

    // Config
    const config = {
        // Type of chart
        type: 'line',

        // Data for the dataset - defined above
        data: data,

        // Configuration options
        options: {
            plugins: {
                legend: {
                    labels: {
                        usePointStyle: true
                    },
                },
                title: {
                    display: false
                }
            },
            scales: {
                y: {
                    title: {
                        display: true,
                        text: 'KBD',
                        padding: {top: 0, left: 0, right: 0, bottom: 0}
                    }
                }
            }
        },

        plugins: [legendMargin]
    };

    if (type == 'new') {
        primaryEnergySupplyChart = new Chart(
            // Indicate which Canvas Element to draw chart
            document.getElementById('primaryEnergySupplyChart'),
            config
        );

    } else {
        primaryEnergySupplyChart.data = data;
        primaryEnergySupplyChart.update();
    }
}

async function createSupplyProductDropdown() {
    let response = await makeAPIRequest(getDistinctProducts);
    let productsArr = response['products'];

    let dropdownOptions = '';

    for (let i = 0; i < productsArr.length; i++) {
        let selected = '';

        if (i == 0) {
            selected = 'selected';
        }

        dropdownOptions += `<option value="${productsArr[i]}" ${selected}>${productsArr[i]}</option>`;
    }

    document.getElementById('typesDropdown').innerHTML = dropdownOptions;
}

// Default arguments are for all years case
// Insert 'month' and a year '2019' for onclick case
async function createTypeBreakdownOfProductChart(type, product,  year = null) {
    let endpoint = getTypeBreakdownOfProduct + '?product=' + product;

    if (year !== null) {
        endpoint += '&year=' + year;
    }

    let response = await makeAPIRequest(endpoint);

    let typesArr = response['types'];
    let volumesArr = response['volumes'];

    // Removing "Inventory Changes"
    typesArr.splice(4,1);
    // Removing "Total Primary Energy Supply"
    typesArr.pop();

    let slicedVolumesArr = [];

    for (let i = 0; i < volumesArr.length; i++) {
        if (i == 4 || i == 6) {
            continue;
        }

        slicedVolumesArr.push(Math.ceil(volumesArr[i]));
    }

    const data = {
        labels: typesArr,
        datasets: [
            {
                label: product,
                backgroundColor: colorsRGBA[0],
                borderColor: colorsRGBA[0],
                data: slicedVolumesArr,
                pointBorderColor: 'rgb(0, 0, 0)'
            }
        ]
    };

    // Plugins block
    const legendMargin = {
        id: 'legendMargin',
        beforeInit(chart, legend, options) {
            const fitValue = chart.legend.fit;

            chart.legend.fit = function fit() {
                fitValue.bind(chart.legend)();
                return this.height += 23;
            }
        }
    };

    // Config
    const config = {
        // Type of chart
        type: 'bar',

        // Data for the dataset - defined above
        data: data,

        // Configuration options
        options: {
            indexAxis: 'y',

            interaction: {
                intersect: false
            },

            plugins: {
                title: {
                    display: false
                }
            }
        },

        plugins: [legendMargin]
    };

    if (type == 'new') {
        typeBreakdownOfProductChart = new Chart(
            // Indicate which Canvas Element to draw chart
            document.getElementById('typesChart'),
            config
        );

    } else {
        typeBreakdownOfProductChart.data = data;
        typeBreakdownOfProductChart.update();
    }
}

// Called when user selects a product from dropdown list in 'Supply Types'
async function selectedProduct(product) {
    let year = document.getElementById('supplyYear').innerText;
    let year_param = null;

    if (year != 'Across Years') {
        year_param = Number(year.slice(5));
    }

    await createTypeBreakdownOfProductChart('update', product, year_param);
}

// Onclick for primaryEnergySupplyChart
let supply_count = 0;
const primaryEnergySupply_ctx = document.getElementById("primaryEnergySupplyChart");
primaryEnergySupply_ctx.onclick = supplyChart_clickHandler;

async function supplyChart_clickHandler(click) {
    let points = primaryEnergySupplyChart.getElementsAtEventForMode(click, 'nearest', {intersect: true}, true);

    if(supply_count == 0){
        if (points.length) {
            let firstPoint = points[0];

            let year_clicked = primaryEnergySupplyChart.data.labels[firstPoint.index];

            await updateSupplyCharts(year_clicked);

            // Updating count to prevent person from clicking on chart again
            // Until they pressed the reset button
            supply_count++;
        }
    }
}

async function createProductVolumeLineChart(year = null){
    let response = null;

    if(year == null){
        response = await makeAPIRequest(getDomesticEnergyConsumption + 'selectParam=year');
    }else{
        response = await makeAPIRequest(getDomesticEnergyConsumption + `selectParam=month&year=${year}`);
        for(let i=0; i< response['time'].length;i++){
            if(response['time'][i] in months_conversion){
                response['time'][i] = months_conversion[response['time'][i]];
            }
        }
    }

    let products = response['products'];

    // Sorting the product names in alphabetical order
    let sortedProducts = Object.keys(products).sort()

    // List of datasets for chart object
    const datasets = [];

    for (let i = 0; i < sortedProducts.length; i++) {
        let productArray = products[sortedProducts[i]];

        for (let x = 0; x < productArray.length; x++) {
            let rawValue = productArray[x];

            // Round the values to be displayed on the graph
            productArray[x] = Math.ceil(rawValue);
        }

        datasets.push({
            label: sortedProducts[i],
            backgroundColor: colorsRGBA[i],
            borderColor: colorsRGBA[i],
            data: productArray,
            pointBorderColor: 'rgb(0, 0, 0)'
        });
    }

    const data = {
        labels: response['time'],
        datasets: datasets
    };

    // Plugins block
    const legendMargin = {
        id: 'legendMargin',
        beforeInit(chart, legend, options) {
            const fitValue = chart.legend.fit;

            chart.legend.fit = function fit() {
                fitValue.bind(chart.legend)();
                return this.height += 23;
            }
        }
    };

    // Config
    const config = {
        // Type of chart
        type: 'line',

        // Data for the dataset - defined above
        data: data,

        // Configuration options
        options: {
            plugins: {
                legend: {
                    labels: {
                        usePointStyle: true
                    },
                },
                title: {
                    display: false
                }
            },
            scales: {
                y: {
                    title: {
                        display: true,
                        text: 'KBD',
                        padding: {top: 0, left: 0, right: 0, bottom: 0}
                    }
                }
            }
        },

        plugins: [legendMargin]
    };

    productVolumeLineChart = new Chart(
        // Indicate which Canvas Element to draw chart
        document.getElementById('productVolumeLineChart'),
        config
    );
};

async function createProductBreakDownDoughnut(year = null){
    let product = '';
    let chartTitle = 'Sum ';

    if(year == null){
        product = await makeAPIRequest(getProductBreakdown);
        chartTitle += `(Across Years)`;
    }else{
        product = await makeAPIRequest(getProductBreakdown+`year=${year}`);
        chartTitle += `(Year ${year})`;
    }

    let labels = product['products'];
    let volumes = product['volumes'];

    for (let i = 0; i < volumes.length; i++) {
        volumes[i] = Math.ceil(volumes[i]);
    }

    let data = {
        labels: labels,
        datasets: [{
            label: 'Product Breakdown',
            data: volumes,
            backgroundColor: colorsRGBA,
            hoverOffset: 4
        }]
    };
    const config = {
        type: 'doughnut',
        data: data,
        options: {
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'left',
                },
                title: {
                    display: true,
                    text: chartTitle
                }
            }
        }
    };
    productDoughnutBreakdown = new Chart(
        document.getElementById("productBreakdownChart"), config
    )
}

// function to get the dropdown list
async function getProductBreakdownSector(status=null, year = null){
    if(year == null){
        if(status ==  1){
            let selected_product = document.getElementById("c_typesDropdown").value;
            // console.log("status 1");

            sectorVolumeBarChart.destroy();
            subSectorBarCharts.destroy();
            await createProductSectorBarChart(selected_product);
            await createSectorSubSectorBreakDown(selected_product);
        }else{
            let selected_product = document.getElementById("c_typesDropdown").value;
            await createProductSectorBarChart(selected_product);
            await createSectorSubSectorBreakDown(selected_product);
        }
    }else{
        // console.log("Status : new");
        let selected_product = document.getElementById("c_typesDropdown").value;
        await createProductSectorBarChart(selected_product, year);
        await createSectorSubSectorBreakDown(selected_product, year);
    }
}

function showNoDataFoundAlertIfAllValues0(noDataMessageElement, currentChartElement, valuesArr) {
    // Checks if all values are 0
    let allValues0 = true;

    for (let i = 0; i < valuesArr.length; i++) {
        if (valuesArr[i] !== 0) {
            allValues0 = false;
            break;
        }
    }

    // If all 0, show error message
    let noDataMessage = document.getElementById(noDataMessageElement);
    let noDataMessageTransparent = noDataMessage.classList.contains('d-none');

    let currentChart = document.getElementById(currentChartElement);
    let currentChartTransparent = currentChart.classList.contains('d-none');

    if (allValues0) {
        if (noDataMessageTransparent) {
            noDataMessage.classList.remove('d-none');
        }

        if (!currentChartTransparent) {
            currentChart.classList.add('d-none');
        }

    } else {
        if (!noDataMessageTransparent) {
            noDataMessage.classList.add('d-none');
        }

        if (currentChartTransparent) {
            currentChart.classList.remove('d-none');
        }
    }
}

async function createProductSectorBarChart(selected_product, year =null ) {
    let labels_product_sector= "";
    let volume_product_sector = "";
    let product_sector="";
    let rounded_product_sector = [];
    if(year == null){
        product_sector = await makeAPIRequest(getProductSectorBreakdown+`product=${selected_product}`);

        // Chart 1
        labels_product_sector = product_sector['sectors'];
        volume_product_sector = product_sector['volumes'];

        for(let i=0; i < volume_product_sector.length; i++){
            rounded_product_sector.push(Math.ceil(volume_product_sector[i]));
        }

    }else{
        product_sector = await makeAPIRequest(getProductSectorBreakdown+`product=${selected_product}&year=${year}`);

        // Chart 1
        labels_product_sector = product_sector['sectors'];
        volume_product_sector = product_sector['volumes'];

        for(let i=0; i < volume_product_sector.length; i++){
            rounded_product_sector.push(Math.ceil(volume_product_sector[i]));
        }
    }

    // Checks if all values are 0
    // If yes, show "Not data found" alert
    showNoDataFoundAlertIfAllValues0('productBySectorsNoData', 'sectorVolumeBarChart', rounded_product_sector);

    let data = {
        labels: labels_product_sector,
        datasets: [{
            data: rounded_product_sector,
            backgroundColor: colorsRGBA
        }]
    };

    // Plugins block
    const legendMargin = {
        id: 'legendMargin',
        beforeInit(chart, legend, options) {
            const fitValue = chart.legend.fit;

            chart.legend.fit = function fit() {
                fitValue.bind(chart.legend)();
                return this.height += 23;
            }
        }
    };

    // Config
    let config = {
        // Type of chart
        type: 'bar',
        // Data for the dataset - defined above
        data: data,

        // Configuration options
        options: {
            indexAxis: 'y',

            interaction: {
                intersect: false
            },

            plugins: {
                legend: {
                    display: false
                },
                title: {
                    display: false,
                    text: 'Import - Product Group Breakdown'
                }
            },
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        },
        plugins: [legendMargin]
    }

    // Chart object
    sectorVolumeBarChart = new Chart(
        // Indicate which Canvas Element to draw chart
        document.getElementById('product_sector_bar'),
        config
    );
}

async function createSectorSubSectorBreakDown(selected_product, year =null){
    let labels = [];
    let sub_sector_labels = [];
    let sub_sector_volumes = [];

    if(year == null) {
        let sector_sub_sector = await makeAPIRequest(getSectorsSubSectors + `product=${selected_product}`);
        let final_str = "";

        for (let keys in sector_sub_sector['sectors']) {
            labels.push(keys);
        }

        labels.sort();

        for (let i = 0; i < labels.length; i++) {
            final_str += `
        <option value="${labels[i]}">${labels[i]}</option>
        `;
        }
        document.getElementById("c_productSectorDropdown").innerHTML = final_str;


        let selected_sector = document.getElementById("c_productSectorDropdown").value;
        let sub_sectors = sector_sub_sector['sectors'][selected_sector];

        for (let sub in sub_sectors) {
            sub_sector_labels.push(sub);
            sub_sector_volumes.push(Math.ceil(sub_sectors[sub]));
        }
    }else {
        // console.log("hi");
        let sector_sub_sector = await makeAPIRequest(getSectorsSubSectors + `product=${selected_product}&year=${year}`);
        let final_str = "";
        // console.log(sector_sub_sector);

        for (let keys in sector_sub_sector['sectors']) {
            labels.push(keys);
        }

        labels.sort();

        for (let i = 0; i < labels.length; i++) {
            final_str += `
            <option value="${labels[i]}">${labels[i]}</option>
            `;
        }
        document.getElementById("c_productSectorDropdown").innerHTML = final_str;


        let selected_sector = document.getElementById("c_productSectorDropdown").value;
        let sub_sectors = sector_sub_sector['sectors'][selected_sector];

        // console.log("sub_sectors");
        // console.log(sub_sectors);

        for (let sub in sub_sectors) {
            sub_sector_labels.push(sub);
            sub_sector_volumes.push(Math.ceil(sub_sectors[sub]));
        }
    }

    // Checks if all values are 0
    // If yes, show "Not data found" alert
    showNoDataFoundAlertIfAllValues0('breakdownNoData', 'subSectorBarCharts', sub_sector_volumes);

    let data = {
        labels: sub_sector_labels,
        datasets: [{
            data: sub_sector_volumes,
            backgroundColor: colorsRGBA
        }]
    };
    // Plugins block
    const legendMargin = {
        id: 'legendMargin',
        beforeInit(chart, legend, options) {
            const fitValue = chart.legend.fit;

            chart.legend.fit = function fit() {
                fitValue.bind(chart.legend)();
                return this.height += 23;
            }
        }
    };

    // Config
    let config = {
        // Type of chart
        type: 'bar',
        // Data for the dataset - defined above
        data: data,

        // Configuration options
        options: {
            indexAxis: 'y',

            interaction: {
                intersect: false
            },

            plugins: {
                legend: {
                    display: false
                },
                title: {
                    display: false,
                    text: 'Import - Product Group Breakdown'
                }
            },
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        },
        plugins: [legendMargin]
    }
    // Chart object
    subSectorBarCharts = new Chart(
        // Indicate which Canvas Element to draw chart
        document.getElementById('sectorSubSector'),
        config
    );
};

async function changeSectorBreakdownChart(){
    let sub_sector_labels = [];
    let sub_sector_volumes = [];
    if(year_chosen == null){
        let selected_product = document.getElementById("c_typesDropdown").value;
        let sector_sub_sector =await makeAPIRequest(getSectorsSubSectors+ `product=${selected_product}`);

        subSectorBarCharts.destroy();
        let selected_sector = document.getElementById("c_productSectorDropdown").value;
        let sub_sectors = sector_sub_sector['sectors'][selected_sector];



        for(let sub in sub_sectors){
            if(sub_sectors[sub] != 0){
                sub_sector_labels.push(sub);
                sub_sector_volumes.push(Math.ceil(sub_sectors[sub]));
            }
        }

    }else{
        let selected_product = document.getElementById("c_typesDropdown").value;
        let sector_sub_sector =await makeAPIRequest(getSectorsSubSectors+ `product=${selected_product}&year=${year_chosen}`);

        subSectorBarCharts.destroy();
        let selected_sector = document.getElementById("c_productSectorDropdown").value;
        let sub_sectors = sector_sub_sector['sectors'][selected_sector];



        for(let sub in sub_sectors){
            if(sub_sectors[sub] != 0){
                sub_sector_labels.push(sub);
                sub_sector_volumes.push(Math.ceil(sub_sectors[sub]));
            }
        }

    }


    // Checks if all values are 0
    // If yes, show "Not data found" alert
    showNoDataFoundAlertIfAllValues0('breakdownNoData', 'subSectorBarCharts', sub_sector_volumes);

    let data = {
        labels: sub_sector_labels,
        datasets: [{
            data: sub_sector_volumes,
            backgroundColor: colorsRGBA
        }]
    };

    // Plugins block
    const legendMargin = {
        id: 'legendMargin',
        beforeInit(chart, legend, options) {
            const fitValue = chart.legend.fit;

            chart.legend.fit = function fit() {
                fitValue.bind(chart.legend)();
                return this.height += 23;
            }
        }
    };

    // Config
    let config = {
        // Type of chart
        type: 'bar',
        // Data for the dataset - defined above
        data: data,

        // Configuration options
        options: {
            indexAxis: 'y',

            interaction: {
                intersect: false
            },

            plugins: {
                legend: {
                    display: false
                },
                title: {
                    display: false,
                    text: 'Import - Product Group Breakdown'
                }
            },
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        },
        plugins: [legendMargin]
    }

    // Chart object
    subSectorBarCharts = new Chart(
        // Indicate which Canvas Element to draw chart
        document.getElementById('sectorSubSector'),
        config
    );
}

async function getSectorProductBreakdown(status = null, sub_status=null){
    if(year_chosen == null){
        if(status == null && sub_status == null){
            let sectors = await makeAPIRequest(getDistinctSectorsSubSectors);

            let distinct_sectors = [];
            let final_str = '';
            let products ='';
            for(let each_sector in sectors['sectors']) {
                distinct_sectors.push(each_sector);
            }
            distinct_sectors.sort();

            for(let i =0; i< distinct_sectors.length; i++){
                final_str +=`
                <option value="${distinct_sectors[i]}" >${distinct_sectors[i]}</option>
                `
            }
            document.getElementById('c_sectorDropdown').innerHTML = final_str;
            let selected_sector = document.getElementById('c_sectorDropdown').value;

            let sub_final_str = `
            <option value="all" >All</option>
            `;
            for(let sub_sec in sectors['sectors'][selected_sector]){
                sub_final_str += `
        <option value="${sectors['sectors'][selected_sector][sub_sec]}" >${sectors['sectors'][selected_sector][sub_sec]}</option>
        `
            }
            document.getElementById("c_subSecDropdown").innerHTML = sub_final_str;
            let selected_sub = document.getElementById("c_subSecDropdown").value;

            if(selected_sub == "all"){
                products = await  makeAPIRequest(getSectorProductBreakdown_endpoint+`sector=${selected_sector}`);
            }else{
                products = await  makeAPIRequest(getSectorProductBreakdown_endpoint+`sector=${selected_sector}&subSector=${selected_sub}`);
            }

            let labels = products['products'];
            let volumes = products['volumes'];
            let filtered_volumes = [];
            let filtered_labels = [];
            for(let i =0; i<volumes.length;i++){
                if(volumes[i] != 0){
                    filtered_volumes.push(Math.ceil(volumes[i]));
                    filtered_labels.push(labels[i]);
                }
            }

            // Checks if all values are 0
            // If yes, show "Not data found" alert
            showNoDataFoundAlertIfAllValues0('sectorByProductsNoData', 'sectorProductBarChart', filtered_volumes);

            let data = {
                labels: filtered_labels,
                datasets: [{
                    data: filtered_volumes,
                    backgroundColor: colorsRGBA
                }]
            };

            // Plugins block
            const legendMargin = {
                id: 'legendMargin',
                beforeInit(chart, legend, options) {
                    const fitValue = chart.legend.fit;

                    chart.legend.fit = function fit() {
                        fitValue.bind(chart.legend)();
                        return this.height += 23;
                    }
                }
            };

            // Config
            let config = {
                // Type of chart
                type: 'bar',
                // Data for the dataset - defined above
                data: data,

                // Configuration options
                options: {
                    indexAxis: 'y',

                    interaction: {
                        intersect: false
                    },

                    plugins: {
                        legend: {
                            display: false
                        },
                        title: {
                            display: false,
                            text: 'Import - Product Group Breakdown'
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true
                        }
                    }
                },
                plugins: [legendMargin]
            }
            // Chart object
            sectorProductBarChart = new Chart(
                // Indicate which Canvas Element to draw chart
                document.getElementById('sectorProductChart'),
                config
            );


        }else if(status==1 && sub_status ==0){
            // console.log("HIIIII");
            let products = "";
            sectorProductBarChart.destroy();
            let selected_sector = document.getElementById("c_sectorDropdown").value;
            let sectors = await makeAPIRequest(getDistinctSectorsSubSectors);
            let sub_final_str = `
            <option value="all" >All</option>
            `;
            for(let sub_sec in sectors['sectors'][selected_sector]){
                sub_final_str += `
            <option value="${sectors['sectors'][selected_sector][sub_sec]}" >${sectors['sectors'][selected_sector][sub_sec]}</option>
            `
            }
            document.getElementById("c_subSecDropdown").innerHTML = sub_final_str;
            products = await  makeAPIRequest(getSectorProductBreakdown_endpoint+`sector=${selected_sector}`);

            let labels = products['products'];
            let volumes = products['volumes'];
            let filtered_volumes = [];
            let filtered_labels = [];
            for(let i =0; i<volumes.length;i++){
                if(volumes[i] != 0){
                    filtered_volumes.push(Math.ceil(volumes[i]));
                    filtered_labels.push(labels[i]);
                }
            }

            // Checks if all values are 0
            // If yes, show "Not data found" alert
            showNoDataFoundAlertIfAllValues0('sectorByProductsNoData', 'sectorProductBarChart', filtered_volumes);

            let data = {
                labels: filtered_labels,
                datasets: [{
                    data: filtered_volumes,
                    backgroundColor: colorsRGBA
                }]
            };

            // Plugins block
            const legendMargin = {
                id: 'legendMargin',
                beforeInit(chart, legend, options) {
                    const fitValue = chart.legend.fit;

                    chart.legend.fit = function fit() {
                        fitValue.bind(chart.legend)();
                        return this.height += 23;
                    }
                }
            };

            // Config
            let config = {
                // Type of chart
                type: 'bar',
                // Data for the dataset - defined above
                data: data,

                // Configuration options
                options: {
                    indexAxis: 'y',

                    interaction: {
                        intersect: false
                    },

                    plugins: {
                        legend: {
                            display: false
                        },
                        title: {
                            display: false,
                            text: 'Import - Product Group Breakdown'
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true
                        }
                    }
                },
                plugins: [legendMargin]
            }
            // Chart object
            sectorProductBarChart = new Chart(
                // Indicate which Canvas Element to draw chart
                document.getElementById('sectorProductChart'),
                config
            );
        }else{
            let products = "";
            sectorProductBarChart.destroy();
            let selected_sector = document.getElementById("c_sectorDropdown").value;

            let hasChanged = true;
            let element1 = document.getElementById("c_subSecDropdown");

            if(hasChanged == true){
                let selected_sub = document.getElementById("c_subSecDropdown").value;

                if(selected_sub == "all"){
                    products = await  makeAPIRequest(getSectorProductBreakdown_endpoint+`sector=${selected_sector}`);
                }else{
                    products = await  makeAPIRequest(getSectorProductBreakdown_endpoint+`sector=${selected_sector}&subSector=${selected_sub}`);
                }
            }

            let labels = products['products'];
            let volumes = products['volumes'];
            let filtered_volumes = [];
            let filtered_labels = [];
            for(let i =0; i<volumes.length;i++){
                if(volumes[i] != 0){
                    filtered_volumes.push(Math.ceil(volumes[i]));
                    filtered_labels.push(labels[i]);
                }
            }

            // Checks if all values are 0
            // If yes, show "Not data found" alert
            showNoDataFoundAlertIfAllValues0('sectorByProductsNoData', 'sectorProductBarChart', filtered_volumes);

            let data = {
                labels: filtered_labels,
                datasets: [{
                    data: filtered_volumes,
                    backgroundColor: colorsRGBA
                }]
            };

            // Plugins block
            const legendMargin = {
                id: 'legendMargin',
                beforeInit(chart, legend, options) {
                    const fitValue = chart.legend.fit;

                    chart.legend.fit = function fit() {
                        fitValue.bind(chart.legend)();
                        return this.height += 23;
                    }
                }
            };

            // Config
            let config = {
                // Type of chart
                type: 'bar',
                // Data for the dataset - defined above
                data: data,

                // Configuration options
                options: {
                    indexAxis: 'y',

                    interaction: {
                        intersect: false
                    },

                    plugins: {
                        legend: {
                            display: false
                        },
                        title: {
                            display: false,
                            text: 'Import - Product Group Breakdown'
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true
                        }
                    }
                },
                plugins: [legendMargin]
            }
            // Chart object
            sectorProductBarChart = new Chart(
                // Indicate which Canvas Element to draw chart
                document.getElementById('sectorProductChart'),
                config
            );

        }

    }else{
        if(status == null && sub_status == null){
            let sectors = await makeAPIRequest(getDistinctSectorsSubSectors);

            let distinct_sectors = [];
            let final_str = '';
            let products ='';
            for(let each_sector in sectors['sectors']) {
                distinct_sectors.push(each_sector);
            }
            distinct_sectors.sort();
            for(let i =0; i< distinct_sectors.length; i++){
                final_str +=`
                <option value="${distinct_sectors[i]}" >${distinct_sectors[i]}</option>
                `;
            }
            document.getElementById('c_sectorDropdown').innerHTML = final_str;
            let selected_sector = document.getElementById('c_sectorDropdown').value;

            let sub_final_str = `
            <option value="all" >All</option>
            `;
            for(let sub_sec in sectors['sectors'][selected_sector]){
                sub_final_str += `
                <option value="${sectors['sectors'][selected_sector][sub_sec]}" >${sectors['sectors'][selected_sector][sub_sec]}</option>
                `
            }
            document.getElementById("c_subSecDropdown").innerHTML = sub_final_str;
            let selected_sub = document.getElementById("c_subSecDropdown").value;

            if(selected_sub == "all"){
                products = await  makeAPIRequest(getSectorProductBreakdown_endpoint+`sector=${selected_sector}&year=${year_chosen}`);
            }else{
                products = await  makeAPIRequest(getSectorProductBreakdown_endpoint+`sector=${selected_sector}&subSector=${selected_sub}&year=${year_chosen}`);
            }

            let labels = products['products'];
            let volumes = products['volumes'];
            let filtered_volumes = [];
            let filtered_labels = [];
            for(let i =0; i<volumes.length;i++){
                if(volumes[i] != 0){
                    filtered_volumes.push(Math.ceil(volumes[i]));
                    filtered_labels.push(labels[i]);
                }
            }

            // Checks if all values are 0
            // If yes, show "Not data found" alert
            showNoDataFoundAlertIfAllValues0('sectorByProductsNoData', 'sectorProductBarChart', filtered_volumes);

            let data = {
                labels: filtered_labels,
                datasets: [{
                    data: filtered_volumes,
                    backgroundColor: colorsRGBA
                }]
            };

            // Plugins block
            const legendMargin = {
                id: 'legendMargin',
                beforeInit(chart, legend, options) {
                    const fitValue = chart.legend.fit;

                    chart.legend.fit = function fit() {
                        fitValue.bind(chart.legend)();
                        return this.height += 23;
                    }
                }
            };

            // Config
            let config = {
                // Type of chart
                type: 'bar',
                // Data for the dataset - defined above
                data: data,

                // Configuration options
                options: {
                    indexAxis: 'y',

                    interaction: {
                        intersect: false
                    },

                    plugins: {
                        legend: {
                            display: false
                        },
                        title: {
                            display: false,
                            text: 'Import - Product Group Breakdown'
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true
                        }
                    }
                },
                plugins: [legendMargin]
            }
            // Chart object
            sectorProductBarChart = new Chart(
                // Indicate which Canvas Element to draw chart
                document.getElementById('sectorProductChart'),
                config
            );


        }else if(status==1 && sub_status ==0){
            let products = "";
            sectorProductBarChart.destroy();
            let selected_sector = document.getElementById("c_sectorDropdown").value;
            let sectors = await makeAPIRequest(getDistinctSectorsSubSectors);
            let sub_final_str = `
            <option value="all" >All</option>
            `;
            for(let sub_sec in sectors['sectors'][selected_sector]){
                sub_final_str += `
                <option value="${sectors['sectors'][selected_sector][sub_sec]}" >${sectors['sectors'][selected_sector][sub_sec]}</option>
                `
            }
            document.getElementById("c_subSecDropdown").innerHTML = sub_final_str;
            products = await  makeAPIRequest(getSectorProductBreakdown_endpoint+`sector=${selected_sector}&year=${year_chosen}`);

            let labels = products['products'];
            let volumes = products['volumes'];
            let filtered_volumes = [];
            let filtered_labels = [];
            for(let i =0; i<volumes.length;i++){
                if(volumes[i] != 0){
                    filtered_volumes.push(Math.ceil(volumes[i]));
                    filtered_labels.push(labels[i]);
                }
            }

            // Checks if all values are 0
            // If yes, show "Not data found" alert
            showNoDataFoundAlertIfAllValues0('sectorByProductsNoData', 'sectorProductBarChart', filtered_volumes);

            let data = {
                labels: filtered_labels,
                datasets: [{
                    data: filtered_volumes,
                    backgroundColor: colorsRGBA
                }]
            };

            // Plugins block
            const legendMargin = {
                id: 'legendMargin',
                beforeInit(chart, legend, options) {
                    const fitValue = chart.legend.fit;

                    chart.legend.fit = function fit() {
                        fitValue.bind(chart.legend)();
                        return this.height += 23;
                    }
                }
            };

            // Config
            let config = {
                // Type of chart
                type: 'bar',
                // Data for the dataset - defined above
                data: data,

                // Configuration options
                options: {
                    indexAxis: 'y',

                    interaction: {
                        intersect: false
                    },

                    plugins: {
                        legend: {
                            display: false
                        },
                        title: {
                            display: false,
                            text: 'Import - Product Group Breakdown'
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true
                        }
                    }
                },
                plugins: [legendMargin]
            }
            // Chart object
            sectorProductBarChart = new Chart(
                // Indicate which Canvas Element to draw chart
                document.getElementById('sectorProductChart'),
                config
            );
        }else{
            let products = "";
            sectorProductBarChart.destroy();
            let selected_sector = document.getElementById("c_sectorDropdown").value;

            let hasChanged = true;
            let element1 = document.getElementById("c_subSecDropdown");

            if(hasChanged){
                let selected_sub = document.getElementById("c_subSecDropdown").value;

                if(selected_sub == "all"){
                    products = await  makeAPIRequest(getSectorProductBreakdown_endpoint+`sector=${selected_sector}&year=${year}`);
                    console.log(products);
                }else{
                    products = await  makeAPIRequest(getSectorProductBreakdown_endpoint+`sector=${selected_sector}&subSector=${selected_sub}&year=${year_chosen}`);
                    console.log(products);
                }
            }
            console.log(products);

            let labels = products['products'];
            let volumes = products['volumes'];
            let filtered_volumes = [];
            let filtered_labels = [];
            for(let i =0; i<volumes.length;i++){
                if(volumes[i] != 0){
                    filtered_volumes.push(Math.ceil(volumes[i]));
                    filtered_labels.push(labels[i]);
                }
            }

            // Checks if all values are 0
            // If yes, show "Not data found" alert
            showNoDataFoundAlertIfAllValues0('sectorByProductsNoData', 'sectorProductBarChart', filtered_volumes);

            let data = {
                labels: filtered_labels,
                datasets: [{
                    data: filtered_volumes,
                    backgroundColor: colorsRGBA
                }]
            };
            // Plugins block
            const legendMargin = {
                id: 'legendMargin',
                beforeInit(chart, legend, options) {
                    const fitValue = chart.legend.fit;

                    chart.legend.fit = function fit() {
                        fitValue.bind(chart.legend)();
                        return this.height += 23;
                    }
                }
            };

            // Config
            let config = {
                // Type of chart
                type: 'bar',
                // Data for the dataset - defined above
                data: data,

                // Configuration options
                options: {
                    indexAxis: 'y',

                    interaction: {
                        intersect: false
                    },

                    plugins: {
                        legend: {
                            display: false
                        },
                        title: {
                            display: false,
                            text: 'Import - Product Group Breakdown'
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true
                        }
                    }
                },
                plugins: [legendMargin]
            }
            // Chart object
            sectorProductBarChart = new Chart(
                // Indicate which Canvas Element to draw chart
                document.getElementById('sectorProductChart'),
                config
            );
        }
    }
}

//capture changes in dropdown list changes
// let changed_dropdown_product = document.getElementById('c_typesDropdown');
// document.getElementById("c_typesDropdown").addEventListener("click",getProductBreakdownSector("change"));

// By default, catered for reset button case
// Insert a year for onclick case
async function updateSupplyCharts(year_clicked = null) {
    let selectedProduct = document.getElementById('typesDropdown').value;

    if (year_clicked == null) { // For reset button case
        await createPrimaryEnergySupplyChart('update');
        await createTypeBreakdownOfProductChart('update', selectedProduct);

        supply_count = 0;

        document.getElementById('supplyYear').innerText = 'Across Years';

    } else {
        await createPrimaryEnergySupplyChart('update','month', year_clicked);
        await createTypeBreakdownOfProductChart('update', selectedProduct, year_clicked);

        document.getElementById('supplyYear').innerText = 'Year ' + year_clicked;
    }

    document.getElementById('supplyYearReset').classList.toggle('d-none');
}

let consumption_count = 0;
const c_ctx = document.getElementById("productVolumeLineChart");
c_ctx.onclick = c_clickHandler;
async function c_clickHandler(click) {

    let points = productVolumeLineChart.getElementsAtEventForMode(click, 'nearest', {intersect: true}, true);
    if(consumption_count ==0){
        if (points.length) {
            let firstPoint = points[0];

            let column_name = productVolumeLineChart.data.labels[firstPoint.index];
            let value = productVolumeLineChart.data.datasets[firstPoint.datasetIndex].data[firstPoint.index];

            year_chosen = column_name;
            // await updateAllCharts("update","harbor", column_name);
            // remove all the previous chart
            productVolumeLineChart.destroy()
            productDoughnutBreakdown.destroy()
            sectorVolumeBarChart.destroy();
            subSectorBarCharts.destroy();
            sectorProductBarChart.destroy();
            // add in year param
            await createProductVolumeLineChart(column_name);
            await createProductBreakDownDoughnut(column_name);
            await getProductBreakdownSector(null,column_name);
            await getSectorProductBreakdown(null,null);


            document.getElementById('consumptionYearReset').classList.remove('d-none');

            document.getElementById('productsYear').innerText = `Year ${column_name}`;
            document.getElementById('productSectorYear').innerText = `Year ${column_name}`;
            document.getElementById('sectorProductYear').innerText = `Year ${column_name}`;

            consumption_count++
        }

    }
}
async function updateConsumptionChart(){
    // remove all the previous chart
    productVolumeLineChart.destroy()
    productDoughnutBreakdown.destroy()
    sectorVolumeBarChart.destroy();
    subSectorBarCharts.destroy();
    sectorProductBarChart.destroy();
    year_chosen = null;
    // add in year param
    await createProductVolumeLineChart();
    await createProductBreakDownDoughnut();
    await getProductBreakdownSector();
    await getSectorProductBreakdown();

    consumption_count = 0;

    document.getElementById('consumptionYearReset').classList.toggle('d-none');

    document.getElementById('productsYear').innerText = 'Across Years';
    document.getElementById('productSectorYear').innerText = 'Across Years';
    document.getElementById('sectorProductYear').innerText = 'Across Years';
}