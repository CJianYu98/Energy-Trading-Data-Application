// Endpoints
const getDistinct = '/indoData/getDistinctByCriteria?';
const getGroupBy = '/indoData/getByCriteriaGroupBy?';

// Product Breakdown
const getProductBreakdownImport ='/indoData/getByCriteriaGroupBy?selectParam1=category&selectParam2=product&isExport=0';
const getProductBreakdownExport ='/indoData/getByCriteriaGroupBy?selectParam1=category&selectParam2=product&isExport=1';

// Harbor
const getharborImport ='/indoData/getByCriteriaGroupBy?selectParam1=harbor&isExport=0';
const getharborExport ='/indoData/getByCriteriaGroupBy?selectParam1=harbor&isExport=1';

// Chart objects
let productImportChart = null;
let productExportChart = null;
let breakdownImportChart = null;
let breakdownExportChart = null;
let countriesChart = null;
let harborsChart = null;

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

const monthsArr = [
    'Jan',
    'Feb',
    'Mar',
    'Apr',
    'May',
    'Jun',
    'Jul',
    'Aug',
    'Sep',
    'Oct',
    'Nov',
    'Dec'
]

const monthsInteger = {
    'Jan': 1,
    'Feb': 2,
    'Mar': 3,
    'Apr': 4,
    'May': 5,
    'Jun': 6,
    'Jul': 7,
    'Aug': 8,
    'Sep': 9,
    'Oct': 10,
    'Nov': 11,
    'Dec': 12
}

const productTruncate = {
    'Condensates': 'Condensates',
    'Crude Petroleum Oils': 'Crude Petroleum Oils',
    'Other Crude Petroleum Oils Dan Condensates': 'Other Crude Petroleum Oils',
    'Automotive Diesel Fuel': 'Automotive Diesel Fuel',
    'Other Diesel Fuel': 'Other Diesel Fuel',
    'Other Fuel Oils': 'Other Fuel Oils',
    'Motor Spirit Of Ron 90 & Above But Below Ron 97 Blended With Ethanol': 'Ron 90 - 96, Blended with Ethanol',
    'Motor Spirit Of Ron 90 & Above But Below Ron 97 Unblended': 'Ron 90 - 96, Unblended',
    'Motor Spirit Of Ron 90 & Above, But Below Ron 97, Blended Besides Ethanol': 'Ron 90 - 96, Blended besides Ethanol',
    'Motor Spirit Of Ron 97 & Above, Blended Besides Ethanol': '>= Ron 97, Blended besides Ethanol',
    'Motor Spirit Of Ron 97 & Above, Unleaded, Blended With Ethanol': '>= Ron 97, Unleaded, Blended with Ethanol',
    'Motor Spirit Of Ron 97 & Above, Unleaded, Unblended': '>= Ron 97, Unleaded, Unblended',
    'Motor Spirit, Of Ron 90 And Above, But Below Ron 97, Leaded': 'Ron 90 - 96, Leaded',
    'Motor Spirit, Of Ron 97 And Above, Leaded': '>= Ron 97, Leaded',
    'Motor Spirit, Of Ron 97 And Above, Unleaded': '>= Ron 97, Unleaded',
    'Of Other Ron Blended Besides Ethanol': 'Other Ron, Blended besides Ethanol',
    'Of Other Ron Unblended': 'Other Ron, Unblended',
    'Aviation Turbine Fuel (jet Fuel) Having A Flash Point Of 23-c Or More': 'Flash Point of >= 23-c',
    'Aviation Turbine Fuel (jet Fuel) Having A Flash Point Of Less Than 23-c': 'Flash Point of < 23-c',
    'Other Kerosene': 'Other Kerosene',
    'Naphtha, Reformate And Oth Preparations Of A Kind Used For Bleding Into Motor Sp': 'For Blending into Motor Spirits',
    'Naphtha, Reformates & Other Preparations Of A Kind Used For Blending Into Motor Spirits': 'For Blending into Motor Spirits'
}

async function makeAPIRequest(endpoint, method) {
    let properties = {};

    if (method == 'POST') {
        properties = {
            method: 'POST',
            body: JSON.stringify({}),
            headers: {'Content-type': 'application/json;'}
        }
    }

    try {
        // Response object
        const response = await fetch(endpoint, properties);
        // Response object's JSON body --> JavaScript
        const parsedJson = await response.json();

        return parsedJson;

    } catch(error) {
        console.log(error);
        return error;
    }
}

async function createAllCharts() {
    await createProductCharts('create', 0);
    await createProductCharts('create', 1);
    await createBreakdownImportChart()
    await createBreakdownExportChart();
    await createCountriesChart('create');
    await createHarborsChart();

    await loadFilterDropdown(['year', 'category', 'country', 'harbor']);
}

async function createProductCharts(type, isExport, weightOrValue = null, yearsOrMonthsEndpoint = null, productsEndpoint = null, valueWeightEndpoint = null) {
    let yearsOrMonths = null;
    let products = null;
    let productValueWeightList = null;

    if (type == 'create') {
        yearsOrMonths = await makeAPIRequest(getDistinct + 'selectParam=year&isExport=' + isExport,'GET');
        products = await makeAPIRequest(getDistinct + 'selectParam=category&isExport=' + isExport,'GET');

        // Get list of [Product group, Year/Month, Value, KG, Tonnes]
        productValueWeightList = await makeAPIRequest(getGroupBy + 'selectParam1=category&selectParam2=year&isExport=' + isExport, 'GET');

    } else {
        yearsOrMonths = await makeAPIRequest(yearsOrMonthsEndpoint + '&isExport=' + isExport,'GET');
        products = await makeAPIRequest(productsEndpoint + '&isExport=' + isExport,'GET');

        // Get list of [Product group, Year/Month, Value, KG, Tonnes]
        productValueWeightList = await makeAPIRequest(valueWeightEndpoint + '&isExport=' + isExport, 'GET');
    }

    // To render 'No data found' if no data retrieved from endpoint
    if (isExport) {
        let totalExport = document.getElementById('productExportTotal');
        let totalExportTransparent = totalExport.classList.contains('d-none');

        let noDataMessageExport = document.getElementById('productExportNoData');
        let noDataMessageExportTransparent = noDataMessageExport.classList.contains('d-none');

        let productExportDiv = document.getElementById('productExportChartDiv');
        let productExportDivTransparent = productExportDiv.classList.contains('d-none');

        if (yearsOrMonths.status === 404 || products.status === 404 || productValueWeightList.status === 404) {

            if (!totalExportTransparent) {
                totalExport.classList.add('d-none');
            }

            if (noDataMessageExportTransparent) {
                noDataMessageExport.classList.remove('d-none');
            }

            if (!productExportDivTransparent) {
                productExportDiv.classList.add('d-none');
            }

            return;

        } else {

            if (totalExportTransparent) {
                totalExport.classList.remove('d-none');
            }

            if (!noDataMessageExportTransparent) {
                noDataMessageExport.classList.add('d-none');
            }

            if (productExportDivTransparent) {
                productExportDiv.classList.remove('d-none');
            }
        }

    } else {
        let totalImport = document.getElementById('productImportTotal');
        let totalImportTransparent = totalImport.classList.contains('d-none');

        let noDataMessageImport = document.getElementById('productImportNoData');
        let noDataMessageImportTransparent = noDataMessageImport.classList.contains('d-none');

        let productImportDiv = document.getElementById('productImportChartDiv');
        let productImportDivTransparent = productImportDiv.classList.contains('d-none');

        if (yearsOrMonths.status === 404 || products.status === 404 || productValueWeightList.status === 404) {

            if (!totalImportTransparent) {
                totalImport.classList.add('d-none');
            }

            if (noDataMessageImportTransparent) {
                noDataMessageImport.classList.remove('d-none');
            }

            if (!productImportDivTransparent) {
                productImportDiv.classList.add('d-none');
            }

            return;

        } else {

            if (totalImportTransparent) {
                totalImport.classList.remove('d-none');
            }

            if (!noDataMessageImportTransparent) {
                noDataMessageImport.classList.add('d-none');
            }

            if (productImportDivTransparent) {
                productImportDiv.classList.remove('d-none');
            }
        }

    }

    // Total Indicator value
    let total = 0;

    // List of datasets for chart object
    const datasets = [];

    for (let x = 0; x < products.length; x++) {
        let datasetsArr = new Array(yearsOrMonths.length).fill(0);

        for (let y = 0; y < yearsOrMonths.length; y++) {
            for (let z = 0; z < productValueWeightList.length; z++) {
                let tempArr = productValueWeightList[z];

                if (tempArr[0] == products[x] && tempArr[1] == yearsOrMonths[y]) {
                    let weightValue = null;

                    if (weightOrValue === null || weightOrValue == 'weight_tonne') {
                        weightValue = tempArr[4];
                    } else if (weightOrValue == 'weight_kg') {
                        weightValue = tempArr[3];
                    } else {
                            weightValue = tempArr[2];
                    }

                    // Round up value
                    datasetsArr[y] = Math.ceil(weightValue);

                    total += Math.ceil(weightValue);

                    break;
                }
            }
        }

        datasets.push({
            label: products[x],
            backgroundColor: colorsRGBA[x],
            borderColor: colorsRGBA[x],
            data: datasetsArr,
            pointBorderColor: 'rgb(0, 0, 0)'
        });
    }

    total = Math.ceil(total);
    total = total.toLocaleString("en-US");

    let indicatorValue = '';

    if (weightOrValue === null || weightOrValue == 'weight_tonne') {
        indicatorValue = `${total} <i>MT</i>`;
    } else if (weightOrValue == 'weight_kg') {
        indicatorValue = `${total} <i>KG</i>`;
    } else {
        indicatorValue = `<i>USD $</i> ${total}`;
    }

    if (isExport) {
        document.getElementById('productExportChartTotal').innerHTML = indicatorValue;
    } else {
        document.getElementById('productImportChartTotal').innerHTML = indicatorValue;
    }

    // Converting Months from Integer to Words
    if (yearsOrMonths.length > 0) {
        if (yearsOrMonths[0].toString().length != 4) {
            let convertedMonths = [];

            for (let i = 0; i < yearsOrMonths.length; i++) {
                convertedMonths.push(monthsArr[yearsOrMonths[i] - 1]);
            }

            yearsOrMonths = convertedMonths;
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
            }
        },

        plugins: [legendMargin]
    };

    // Chart object
    if (isExport) {
        productExportChart = new Chart(
            // Indicate which Canvas Element to draw chart
            document.getElementById('productExportChart'),
            config
        );
    } else {
        productImportChart = new Chart(
            // Indicate which Canvas Element to draw chart
            document.getElementById('productImportChart'),
            config
        );
    }
}

async function createBreakdownImportChart(get_import_breakdown = null, get_label =null, unit=null) {
    let datasets = [];
    let colorsCounter = 0;
    let labels = [];
    let import_product_breakdown =[];
    let units = unit;

    if (get_import_breakdown === null && get_label === null){
        labels = await makeAPIRequest(getDistinct+"selectParam=category&isExport=0", 'GET');
        import_product_breakdown =await makeAPIRequest(getProductBreakdownImport, 'GET');

    } else {
        labels = get_label;
        import_product_breakdown = get_import_breakdown;
    }

    // To render 'No data found' if no data retrieved from endpoint
    let noDataMessageImport = document.getElementById('breakdownImportNoData');
    let noDataMessageImportTransparent = noDataMessageImport.classList.contains('d-none');

    let breakdownImportDiv = document.getElementById('breakdownImportChartDiv');
    let breakdownImportDivTransparent = breakdownImportDiv.classList.contains('d-none');

    if (import_product_breakdown.status === 404 || labels.status === 404) {

        if (noDataMessageImportTransparent) {
            noDataMessageImport.classList.remove('d-none');
        }

        if (!breakdownImportDivTransparent) {
            breakdownImportDiv.classList.add('d-none');
        }

        return;

    } else {

        if (!noDataMessageImportTransparent) {
            noDataMessageImport.classList.add('d-none');
        }

        if (breakdownImportDivTransparent) {
            breakdownImportDiv.classList.remove('d-none');
        }

    }

    //convert import_product_breakdown into json
    let import_breakdown ={};

    for(let i=0; i< import_product_breakdown.length; i++){
        if(import_product_breakdown[i][0] in import_breakdown){
            if (units == null || units == "weight_tonne") {
                import_breakdown[import_product_breakdown[i][0]][import_product_breakdown[i][1]] = import_product_breakdown[i][4]

            } else if (units == "weight_kg") {
                import_breakdown[import_product_breakdown[i][0]][import_product_breakdown[i][1]] = import_product_breakdown[i][3]

            } else {
                import_breakdown[import_product_breakdown[i][0]][import_product_breakdown[i][1]] = import_product_breakdown[i][2]
            }

        }else{
            let product ={};
            if (units == null || units == "weight_tonne") {
                // import_breakdown[import_product_breakdown[i][0]][import_product_breakdown[i][1]] = import_product_breakdown[i][4]
                product[import_product_breakdown[i][1]]= import_product_breakdown[i][4];
                import_breakdown[import_product_breakdown[i][0]]  = product;
            } else if (units == "weight_kg") {
                // import_breakdown[import_product_breakdown[i][0]][import_product_breakdown[i][1]] = import_product_breakdown[i][3]
                product[import_product_breakdown[i][1]]= import_product_breakdown[i][3];
                import_breakdown[import_product_breakdown[i][0]]  = product;
            } else {
                // import_breakdown[import_product_breakdown[i][0]][import_product_breakdown[i][1]] = import_product_breakdown[i][2]
                product[import_product_breakdown[i][1]]= import_product_breakdown[i][2];
                import_breakdown[import_product_breakdown[i][0]]  = product;
            }
        }
    }

    // getting the relative value
    let relative_import_dict ={};

    for(let each in import_breakdown) {
        let total = 0;
        for (let ind in import_breakdown[each]) {
            let value = import_breakdown[each][ind];
            total += value;
        }

        let product = {}

        for (let ind in import_breakdown[each]) {

            let relative_val = (import_breakdown[each][ind] / total) *100;

            product[ind] = relative_val
        }

        relative_import_dict[each]= product;
    }

    let sorted_relative_import_dict= {};
    for(let each in relative_import_dict){
        // each is product group
        let product_value_list =[];
        let product_name_list =[];
        for(let product in relative_import_dict[each]){
        //    product is ind product
            product_value_list.push(relative_import_dict[each][product])
            product_name_list.push(product)
        }
        // sort the product value_list:
        let sorted_product_value = product_value_list.sort((a,b) => b-a);
        let index_list =[]
        for(let i=0; i< sorted_product_value.length; i++){
            index_list.push(product_value_list.indexOf(sorted_product_value[i]));
        }
        for(let i = 0;i<index_list.length;i++){
            if(each in sorted_relative_import_dict){
                sorted_relative_import_dict[each][product_name_list[index_list[i]]]= product_value_list[index_list[i]];

            }else{
                let product_dict = {};
                product_dict[product_name_list[index_list[i]]]=product_value_list[index_list[i]];
                sorted_relative_import_dict[each]= product_dict;
            }

        }
    }

    for(let i = 0; i< labels.length; i++){
        let shade_editor = 1.0
        for(let ind_products in sorted_relative_import_dict[labels[i]]){
            // if ((colorsCounter + 1) == colorsArr.length) {
            //     colorsCounter = 0;
            // }
            let tempArr = new Array(labels.length).fill(0);
            tempArr[i] = sorted_relative_import_dict[labels[i]][ind_products];

            if (shade_editor < 0) {
                shade_editor = 1.0;
            }

            let color = colorsArr[colorsCounter];

            let colorRGBA = `rgba(${color[0]}, ${color[1]}, ${color[2]}, ${shade_editor})`;

            let borderRGBA = `rgb(119,136,153)`;

            datasets.push({
                label: productTruncate[ind_products],
                backgroundColor: colorRGBA,
                data: tempArr,
                borderColor: borderRGBA,
                borderWidth: 1
            });
            shade_editor -= 0.3;
        }
        colorsCounter++;

    }
    const data = {
        labels: labels,
        datasets: datasets
    };

    // Config
    const config = {
        // Type of chart
        type: 'bar',
        // Data for the dataset - defined above
        data: data,

        // Configuration options
        options: {
            maintainAspectRatio: false,

            interaction: {
                mode: 'nearest'
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
                x: {
                    stacked: true
                },
                y: {
                    min: 0,
                    max:100,
                    stacked: true,
                    title: {
                        display: true,
                        text: '% percent',
                        // color: '#191',
                        // font: {
                        //     size: 12,
                        //     // weight: 'bold'
                        //     // lineHeight: 1.2
                        // },
                        padding: {top: 0, left: 0, right: 0, bottom: 0}
                    }
                }
            }
        }
    };
    // Chart object
    breakdownImportChart = new Chart(
        // Indicate which Canvas Element to draw chart
        document.getElementById('breakdownImportChart'),
        config
    );
}

async function createBreakdownExportChart(get_updated_export = null, get_updated_label = null , unit=null) {
    let datasets = [];
    let colorsCounter = 0;
    let labels = [];
    let export_product_breakdown = [];
    let units = unit

    if (get_updated_export === null && get_updated_label === null){
        labels = await makeAPIRequest(getDistinct+"selectParam=category&isExport=1", 'GET');
        export_product_breakdown = await makeAPIRequest(getProductBreakdownExport, 'GET');
    } else {
        labels = get_updated_label;
        export_product_breakdown = get_updated_export;
    }

    // To render 'No data found' if no data retrieved from endpoint
    let noDataMessageExport = document.getElementById('breakdownExportNoData');
    let noDataMessageExportTransparent = noDataMessageExport.classList.contains('d-none');

    let breakdownExportDiv = document.getElementById('breakdownExportChartDiv');
    let breakdownExportDivTransparent = breakdownExportDiv.classList.contains('d-none');

    if (export_product_breakdown.status === 404 || labels.status === 404) {

        if (noDataMessageExportTransparent) {
            noDataMessageExport.classList.remove('d-none');
        }

        if (!breakdownExportDivTransparent) {
            breakdownExportDiv.classList.add('d-none');
        }

        return;

    } else {

        if (!noDataMessageExportTransparent) {
            noDataMessageExport.classList.add('d-none');
        }

        if (breakdownExportDivTransparent) {
            breakdownExportDiv.classList.remove('d-none');
        }

    }


    // convert import_product_breakdown into json
    let export_breakdown ={}
    for(let i=0; i< export_product_breakdown.length; i++){
        if(export_product_breakdown[i][0] in export_breakdown){
            if (units == null || units == "weight_tonne") {
                export_breakdown[export_product_breakdown[i][0]][export_product_breakdown[i][1]] = export_product_breakdown[i][4]

            } else if (units == "weight_kg") {
                export_breakdown[export_product_breakdown[i][0]][export_product_breakdown[i][1]] = export_product_breakdown[i][3]

            } else {
                export_breakdown[export_product_breakdown[i][0]][export_product_breakdown[i][1]] = export_product_breakdown[i][2]
            }

        }else{
            let product ={};
            if (units == null || units == "weight_tonne") {
                // export_breakdown[export_product_breakdown[i][0]][export_product_breakdown[i][1]] = export_product_breakdown[i][4]
                product[export_product_breakdown[i][1]]= export_product_breakdown[i][4];
                export_breakdown[export_product_breakdown[i][0]]  = product;
            } else if (units == "weight_kg") {
                // export_breakdown[export_product_breakdown[i][0]][export_product_breakdown[i][1]] = export_product_breakdown[i][3]
                product[export_product_breakdown[i][1]]= export_product_breakdown[i][3];
                export_breakdown[export_product_breakdown[i][0]]  = product;
            } else {
                // export_breakdown[export_product_breakdown[i][0]][export_product_breakdown[i][1]] = export_product_breakdown[i][2]
                product[export_product_breakdown[i][1]]= export_product_breakdown[i][2];
                export_breakdown[export_product_breakdown[i][0]]  = product;
            }
        }
    }

    // getting the relative value
    let relative_export_dict ={};

    for(let each in export_breakdown) {
        let total = 0;
        for (let ind in export_breakdown[each]) {
            let value = export_breakdown[each][ind];
            total += value;
        }
        let product = {}

        for (let ind in export_breakdown[each]) {
            let relative_val =(export_breakdown[each][ind] / total) *100;
            product[ind] = relative_val
        }

        relative_export_dict[each]= product;
    }

    let sorted_relative_export_dict= {};
    for(let each in relative_export_dict){
        // each is product group
        let product_value_list =[];
        let product_name_list =[];
        for(let product in relative_export_dict[each]){
            //    product is ind product
            product_value_list.push(relative_export_dict[each][product])
            product_name_list.push(product)
        }
        // sort the product value_list:
        let sorted_product_value = product_value_list.sort((a,b) => b-a);
        let index_list =[]
        for(let i=0; i< sorted_product_value.length; i++){
            index_list.push(product_value_list.indexOf(sorted_product_value[i]));
        }
        for(let i = 0;i<index_list.length;i++){
            if(each in sorted_relative_export_dict){
                sorted_relative_export_dict[each][product_name_list[index_list[i]]]= product_value_list[index_list[i]];

            }else{
                let product_dict = {};
                product_dict[product_name_list[index_list[i]]]=product_value_list[index_list[i]];
                sorted_relative_export_dict[each]= product_dict;
            }

        }
    }

    for(let i = 0; i< labels.length; i++){
        let shade_editor = 1.0
        for(let ind_products in sorted_relative_export_dict[labels[i]]){
            // if ((colorsCounter + 1) == colorsRGBA.length) {
            //     colorsCounter = 0;
            // }
            let tempArr = new Array(labels.length).fill(0);
            tempArr[i] = sorted_relative_export_dict[labels[i]][ind_products];

            if (shade_editor < 0) {
                shade_editor = 1.0;
            }

            let color = colorsArr[colorsCounter];

            let colorRGBA = `rgba(${color[0]}, ${color[1]}, ${color[2]}, ${shade_editor})`;

            let borderRGBA = `rgb(119,136,153)`;

            datasets.push({
                label: productTruncate[ind_products],
                backgroundColor: colorRGBA,
                data: tempArr,
                borderColor: borderRGBA,
                borderWidth: 1,
                type:'bar',
                stack: "Stack 0"

            });
            shade_editor -= 0.3;
        }
        colorsCounter++;

    }
    const data = {
        labels: labels,
        datasets: datasets
    };

    // Config
    const config = {
        // Type of chart
        type: 'bar',
        // Data for the dataset - defined above
        data: data,

        // Configuration options
        options: {
            maintainAspectRatio: false,

            interaction: {
                mode: 'nearest'
            },

            plugins: {
                legend: {
                    display: false
                },
                title: {
                    display: false,
                    text: 'Export - Product Group Breakdown'
                }
            },
            scales: {
                x: {
                    stacked: true
                },
                y: {
                    min: 0,
                    max:100,
                    stacked: true,
                    title: {
                        display: true,
                        text: '% percent',
                        padding: {top: 0, left: 0, right: 0, bottom: 0}
                    }
                }
            }
        }
    };
    // Chart object
    breakdownExportChart = new Chart(
        // Indicate which Canvas Element to draw chart
        document.getElementById('breakdownExportChart'),
        config
    );
}

async function createCountriesChart(type, weightOrValue = null, newEndpoint = null) {
    let importCountry = null;
    let exportCountry = null;

    if (type == 'create') {
        importCountry = await makeAPIRequest(getGroupBy + 'selectParam1=country&isExport=0','GET');
        exportCountry = await makeAPIRequest(getGroupBy + 'selectParam1=country&isExport=1','GET');
    } else {
        importCountry = await makeAPIRequest(newEndpoint + '&isExport=0','GET');
        exportCountry = await makeAPIRequest(newEndpoint + '&isExport=1','GET');
    }

    // To render 'No data found' if no data retrieved from endpoint
    let noDataMessageCountries = document.getElementById('countriesNoData');
    let noDataMessageCountriesTransparent = noDataMessageCountries.classList.contains('d-none');

    let countriesDiv = document.getElementById('countriesChartDiv');
    let countriesDivTransparent = countriesDiv.classList.contains('d-none');

    let countriesTableParent = document.getElementById('countriesTableParent');
    let countriesTableParentTransparent = countriesTableParent.classList.contains('d-none');

    if (importCountry.status === 404 && exportCountry.status === 404) {

        if (noDataMessageCountriesTransparent) {
            noDataMessageCountries.classList.remove('d-none');
        }

        if (!countriesDivTransparent) {
            countriesDiv.classList.add('d-none');
        }

        if (!countriesTableParentTransparent) {
            countriesTableParent.classList.add('d-none');
        }

        return;

    } else {

        if (!noDataMessageCountriesTransparent) {
            noDataMessageCountries.classList.add('d-none');
        }

        if (countriesDivTransparent) {
            countriesDiv.classList.remove('d-none');
        }

        if (countriesTableParentTransparent) {
            countriesTableParent.classList.remove('d-none');
        }

    }

    let mergedData = {};

    for (let i = 0; i < importCountry.length; i++) {
        let row = importCountry[i];
        let country = row[0];
        let weightValue = null;

        if (weightOrValue === null || weightOrValue == 'weight_tonne') {
            weightValue = Math.ceil(row[3]);
        } else if (weightOrValue == 'weight_kg') {
            weightValue = Math.ceil(row[2]);
        } else {
            weightValue = Math.ceil(row[1]);
        }

        if (!mergedData.hasOwnProperty(country)) {
            mergedData[country] = {
                import: weightValue,
                export: 0,
                total: weightValue
            };
        } else {
            mergedData[country].import += weightValue;
            mergedData[country].total += weightValue;
        }
    }

    for (let i = 0; i < exportCountry.length; i++) {
        let row = exportCountry[i];
        let country = row[0];
        let weightValue = null;

        if (weightOrValue === null || weightOrValue == 'weight_tonne') {
            weightValue = Math.ceil(row[3]);
        } else if (weightOrValue == 'weight_kg') {
            weightValue = Math.ceil(row[2]);
        } else {
            weightValue = Math.ceil(row[1]);
        }

        if (!mergedData.hasOwnProperty(country)) {
            mergedData[country] = {
                import: 0,
                export: weightValue,
                total: weightValue
            };
        } else {
            mergedData[country].export += weightValue;
            mergedData[country].total += weightValue;
        }
    }

    let toBeSortedArr = [];

    for (const [country, countryObj] of Object.entries(mergedData)) {
        // Each country will be represented by an Array --> [total, net, countryName]
        toBeSortedArr.push([countryObj.total, (countryObj.export - countryObj.import), country])
    }

    // Clone array and Sort by Net
    let sortedByNet = toBeSortedArr.slice();
    sortedByNet.sort(function(a, b){return b[1] - a[1]});

    // Records Top 10 Countries - comparing net
    let top10Countries = [];

    let length = 10;

    if (sortedByNet.length < 10) {
        length = sortedByNet.length;
    }

    for (let i = 0; i < length; i++) {
        top10Countries.push(sortedByNet[i][2]);
    }

    const datasets = [];

    let importDataset = {
        label: 'Import',
        backgroundColor: '#D4ECDD',
        borderColor: '#D4ECDD',
        data: new Array(length).fill(0)
    }

    let exportDataset = {
        label: 'Export',
        backgroundColor: '#345B63',
        borderColor: '#345B63',
        data: new Array(length).fill(0)
    }

    for (let i = 0; i < length; i++) {
        // Make import -negative first
        importDataset.data[i] = Math.ceil(-mergedData[top10Countries[i]].import);
        exportDataset.data[i] = Math.ceil(mergedData[top10Countries[i]].export);
    }

    datasets.push(importDataset);
    datasets.push(exportDataset);

    const data = {
        labels: top10Countries,
        datasets: datasets
    };

    // Tooltip
    const tooltip = {
        yAlign: 'bottom',
        callbacks: {
            label: function(context) {
                return `${context.dataset.label}: ${Math.abs(context.raw).toLocaleString("en-US")}`;
            }
        }
    };

    // Plugins block
    const legendMargin = {
        id: 'legendMargin',
        beforeInit(chart, legend, options) {
            const fitValue = chart.legend.fit;

            chart.legend.fit = function fit() {
                fitValue.bind(chart.legend)();
                return this.height += 20;
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
            maintainAspectRatio: false,

            interaction: {
                intersect: false
            },

            indexAxis: 'y',
            // Elements options apply to all of the options unless overridden in a dataset
            // In this case, we are setting the border of each horizontal bar to be 2px wide
            elements: {
                bar: {
                    borderWidth: 0,
                }
            },
            plugins: {
                title: {
                    display: false
                },
                tooltip,
            },
            scales: {
                x: {
                    stacked: true,
                    ticks: {
                        callback: function(value, index, values) {
                            return Math.abs(value).toLocaleString("en-US");
                        }
                    }
                },
                y: {
                    beginAtZero: true,
                    stacked: true
                }
            }
        },

        plugins: [legendMargin]
    };

    // Chart object
    countriesChart = new Chart(
        // Indicate which Canvas Element to draw chart
        document.getElementById('countriesChart'),
        config
    );

    // Table
    let tableRows = "";

    for (let i = 0; i < sortedByNet.length; i++) {
        let country = sortedByNet[i][2];

        let net = Math.ceil(sortedByNet[i][1]);

        let netStyle = 'text-success';

        if (net < 0) {
            netStyle = 'text-danger';
        }

        if (net === -0) {
            net = 0;
        }

        tableRows += `<tr>
                           <th scope='row'>${i + 1}</th>
                           <td>${country}</td>
                           <td class='${netStyle}'>${net.toLocaleString("en-US")}</td>
                       </tr>`;
    }

    document.getElementById('countryTableHeader').innerText = 'Country';
    document.getElementById('countriesTable').innerHTML = tableRows;
}

async function createCountriesProductsChart(weightOrValue = null, newEndpoint = null) {
    let importProducts = await makeAPIRequest(newEndpoint + '&isExport=0','GET');
    let exportProducts = await makeAPIRequest(newEndpoint + '&isExport=1','GET');

    // To render 'No data found' if no data retrieved from endpoint
    let noDataMessageCountries = document.getElementById('countriesNoData');
    let noDataMessageCountriesTransparent = noDataMessageCountries.classList.contains('d-none');

    let countriesDiv = document.getElementById('countriesChartDiv');
    let countriesDivTransparent = countriesDiv.classList.contains('d-none');

    let countriesTableParent = document.getElementById('countriesTableParent');
    let countriesTableParentTransparent = countriesTableParent.classList.contains('d-none');

    if (importProducts.status === 404 && exportProducts.status === 404) {

        if (noDataMessageCountriesTransparent) {
            noDataMessageCountries.classList.remove('d-none');
        }

        if (!countriesDivTransparent) {
            countriesDiv.classList.add('d-none');
        }

        if (!countriesTableParentTransparent) {
            countriesTableParent.classList.add('d-none');
        }

        return;

    } else {

        if (!noDataMessageCountriesTransparent) {
            noDataMessageCountries.classList.add('d-none');
        }

        if (countriesDivTransparent) {
            countriesDiv.classList.remove('d-none');
        }

        if (countriesTableParentTransparent) {
            countriesTableParent.classList.remove('d-none');
        }

    }

    let mergedData = {};

    for (let i = 0; i < importProducts.length; i++) {
        let row = importProducts[i];
        let productGroup = row[0];
        let weightValue = null;

        if (weightOrValue === null || weightOrValue == 'weight_tonne') {
            weightValue = Math.ceil(row[3]);
        } else if (weightOrValue == 'weight_kg') {
            weightValue = Math.ceil(row[2]);
        } else {
            weightValue = Math.ceil(row[1]);
        }

        if (!mergedData.hasOwnProperty(productGroup)) {
            mergedData[productGroup] = {
                import: weightValue,
                export: 0,
                total: weightValue
            };
        } else {
            mergedData[productGroup].import += weightValue;
            mergedData[productGroup].total += weightValue;
        }
    }

    for (let i = 0; i < exportProducts.length; i++) {
        let row = exportProducts[i];
        let productGroup = row[0];
        let weightValue = null;

        if (weightOrValue === null || weightOrValue == 'weight_tonne') {
            weightValue = Math.ceil(row[3]);
        } else if (weightOrValue == 'weight_kg') {
            weightValue = Math.ceil(row[2]);
        } else {
            weightValue = Math.ceil(row[1]);
        }

        if (!mergedData.hasOwnProperty(productGroup)) {
            mergedData[productGroup] = {
                import: 0,
                export: weightValue,
                total: weightValue
            };
        } else {
            mergedData[productGroup].export += weightValue;
            mergedData[productGroup].total += weightValue;
        }
    }

    let toBeSortedArr = [];

    for (const [productGroup, productGroupObj] of Object.entries(mergedData)) {
        // Each productGroup will be represented by an Array --> [total, net, productGroup]
        toBeSortedArr.push([productGroupObj.total, (productGroupObj.export - productGroupObj.import), productGroup])
    }

    // Clone array and Sort by Net
    let sortedByNet = toBeSortedArr.slice();
    sortedByNet.sort(function(a, b){return b[1] - a[1]});

    // Records Top 10 productGroups - comparing Net
    let top10ProductGroup = [];

    let length = 10;

    if (sortedByNet.length < 10) {
        length = sortedByNet.length;
    }

    for (let i = 0; i < length; i++) {
        top10ProductGroup.push(sortedByNet[i][2]);
    }

    const datasets = [];

    let importDataset = {
        label: 'Import',
        backgroundColor: '#D4ECDD',
        borderColor: '#D4ECDD',
        data: new Array(length).fill(0)
    }

    let exportDataset = {
        label: 'Export',
        backgroundColor: '#345B63',
        borderColor: '#345B63',
        data: new Array(length).fill(0)
    }

    for (let i = 0; i < length; i++) {
        // Change import values to negative first
        importDataset.data[i] = Math.ceil(-mergedData[top10ProductGroup[i]].import);
        exportDataset.data[i] = Math.ceil(mergedData[top10ProductGroup[i]].export);
    }

    datasets.push(importDataset);
    datasets.push(exportDataset);

    const data = {
        labels: top10ProductGroup,
        datasets: datasets
    };

    // Tooltip
    const tooltip = {
        yAlign: 'bottom',
        callbacks: {
            label: function(context) {
                return `${context.dataset.label}: ${Math.abs(context.raw).toLocaleString("en-US")}`;
            }
        }
    };

    // Plugins block
    const legendMargin = {
        id: 'legendMargin',
        beforeInit(chart, legend, options) {
            const fitValue = chart.legend.fit;

            chart.legend.fit = function fit() {
                fitValue.bind(chart.legend)();
                return this.height += 20;
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
            maintainAspectRatio: false,

            interaction: {
                intersect: false
            },

            indexAxis: 'y',
            // Elements options apply to all of the options unless overridden in a dataset
            // In this case, we are setting the border of each horizontal bar to be 2px wide
            elements: {
                bar: {
                    borderWidth: 0,
                }
            },
            plugins: {
                title: {
                    display: false
                },
                tooltip,
            },
            scales: {
                x: {
                    stacked: true,
                    ticks: {
                        callback: function(value, index, values) {
                            return Math.abs(value).toLocaleString("en-US");
                        }
                    }
                },
                y: {
                    beginAtZero: true,
                    stacked: true
                }
            }
        },

        plugins: [legendMargin]
    };

    // Chart object
    countriesChart = new Chart(
        // Indicate which Canvas Element to draw chart
        document.getElementById('countriesChart'),
        config
    );

    // Table
    let tableRows = "";

    for (let i = 0; i < sortedByNet.length; i++) {
        let productGroup = sortedByNet[i][2];

        let net = Math.ceil(sortedByNet[i][1]);

        let netStyle = 'text-success';

        if (net < 0) {
            netStyle = 'text-danger';
        }

        if (net === -0) {
            net = 0;
        }

        tableRows += `<tr>
                           <th scope='row'>${i + 1}</th>
                           <td>${productGroup}</td>
                           <td class='${netStyle}'>${net.toLocaleString("en-US")}</td>
                       </tr>`;
    }

    document.getElementById('countryTableHeader').innerText = 'Product Group';
    document.getElementById('countriesTable').innerHTML = tableRows;
}

async function createHarborsChart(importHarbor = null, exportHarbor= null,newLabels =null, get_units =null) {
    let labels = null;
    let import_harbor = null;
    let export_harbor= null;
    let units = null;

    if ((importHarbor === null) && (exportHarbor === null) && (newLabels === null) && (get_units ===null)) {

        labels = await makeAPIRequest(getDistinct + "selectParam=harbor", 'GET');
        import_harbor = await makeAPIRequest(getharborImport, 'GET');
        export_harbor = await makeAPIRequest(getharborExport, 'GET');
        units = "weight_tonne";

    } else {

        labels = newLabels;
        import_harbor = importHarbor;
        export_harbor = exportHarbor;
        units = get_units;

    }

    // To render 'No data found' if no data retrieved from endpoint
    let noDataMessageHarbors = document.getElementById('harborsNoData');
    let noDataMessageHarborsTransparent = noDataMessageHarbors.classList.contains('d-none');

    let harborsDiv = document.getElementById('harborsChartDiv');
    let harborsDivTransparent = harborsDiv.classList.contains('d-none');

    let harborsTableParent = document.getElementById('harborsTableParent');
    let harborsTableParentTransparent = harborsTableParent.classList.contains('d-none');

    if (labels.status === 404 && import_harbor.status === 404 && export_harbor.status === 404) {

        if (noDataMessageHarborsTransparent) {
            noDataMessageHarbors.classList.remove('d-none');
        }

        if (!harborsDivTransparent) {
            harborsDiv.classList.add('d-none');
        }

        if (!harborsTableParentTransparent) {
            harborsTableParent.classList.add('d-none');
        }

        return;

    } else {

        if (!noDataMessageHarborsTransparent) {
            noDataMessageHarbors.classList.add('d-none');
        }

        if (harborsDivTransparent) {
            harborsDiv.classList.remove('d-none');
        }

        if (harborsTableParentTransparent) {
            harborsTableParent.classList.remove('d-none');
        }

        if (labels.status === 404) {
            labels = [];
        }

        if (import_harbor.status === 404) {
            import_harbor = [];
        }

        if (export_harbor.status === 404) {
            export_harbor = [];
        }
    }

    let import_harbor_weight = [];
    let export_harbor_weight = [];

    for(let i =0 ; i< labels.length ; i++){

        let im_count = 0;

        for(let j=0; j< import_harbor.length; j++) {

            if (import_harbor[j].indexOf(labels[i]) != -1) {
                if (units == null || units == "weight_tonne") {
                    import_harbor_weight.push(Math.ceil(import_harbor[j][3]));
                    break;
                } else if (units == "weight_kg") {
                    import_harbor_weight.push(Math.ceil(import_harbor[j][2]));
                    break;
                } else {
                    import_harbor_weight.push(Math.ceil(import_harbor[j][1]));
                    break;
                }

            }
            im_count++
        }

        if(im_count== import_harbor.length){
            import_harbor_weight.push(0);
        }

        let ex_count = 0;

        for(let k=0; k< export_harbor.length; k++) {
            if(export_harbor[k].indexOf(labels[i]) != -1){
                if (units == null || units == "weight_tonne") {
                    export_harbor_weight.push(Math.ceil(export_harbor[k][3]));
                    break

                } else if (units == "weight_kg") {
                    export_harbor_weight.push(Math.ceil(export_harbor[k][2]));
                    break

                } else {
                    export_harbor_weight.push(Math.ceil(export_harbor[k][1]));
                    break
                }
            }
            ex_count++
        }

        if(ex_count == export_harbor.length){
            export_harbor_weight.push(0);
        }
    }

    //calculating net value of import vs export
    let annual_net_harbor_weight = [];

    for(let i =0;  i< export_harbor_weight.length; i++){
        annual_net_harbor_weight.push(export_harbor_weight[i] - import_harbor_weight[i])
    }

    let overall_harbor = [];
    let overall_net_value = [];
    for(let i=0; i< export_harbor_weight.length; i++){
        overall_harbor[i] = export_harbor_weight[i] + import_harbor_weight[i];
        overall_net_value[i] =  export_harbor_weight[i] - import_harbor_weight[i];
    }

    let mergedData = {};

    for (let i = 0; i < import_harbor.length; i++) {
        let row = import_harbor[i];
        let productGroup = row[0];
        let weightValue = null;

        if (units == null || units == "weight_tonne") {
            weightValue = Math.ceil(row[3]);
        } else if (units == "weight_kg") {
            weightValue = Math.ceil(row[2]);

        } else {
            weightValue = Math.ceil(row[1]);
        }

        if (!mergedData.hasOwnProperty(productGroup)) {
            mergedData[productGroup] = {
                import: weightValue,
                export: 0,
                total: weightValue
            };
        } else {
            mergedData[productGroup].import += weightValue;
            mergedData[productGroup].total += weightValue;
        }
    }

    for (let i = 0; i < export_harbor.length; i++) {
        let row = export_harbor[i];
        let productGroup = row[0];
        let weightValue = null;

        if (units == null || units == "weight_tonne") {
            weightValue = Math.ceil(row[3]);
        } else if (units == "weight_kg") {
            weightValue = Math.ceil(row[2]);

        } else {
            weightValue = Math.ceil(row[1]);
        }

        if (!mergedData.hasOwnProperty(productGroup)) {
            mergedData[productGroup] = {
                import: 0,
                export: weightValue,
                total: weightValue
            };
        } else {
            mergedData[productGroup].export += weightValue;
            mergedData[productGroup].total += weightValue;
        }
    }

    let toBeSortedArr = [];

    for (const [productGroup, productGroupObj] of Object.entries(mergedData)) {
        // Each productGroup will be represented by an Array --> [total, net, productGroup]
        toBeSortedArr.push([productGroupObj.total, (productGroupObj.export - productGroupObj.import), productGroup])
    }

    // Clone array and Sort by Net
    let sortedByNet = toBeSortedArr.slice();
    sortedByNet.sort(function(a, b){return b[1] - a[1]});

    // Records Top 10 productGroups - comparing Net
    let top10ProductGroup = [];

    let length = 10;

    if (sortedByNet.length < 10) {
        length = sortedByNet.length;
    }

    for (let i = 0; i < length; i++) {
        top10ProductGroup.push(sortedByNet[i][2]);
    }

    //get the top 10 harbor label
    let top_labels = [];
    let top_export = [];
    let top_import = [];

    for(let i=0; i< top10ProductGroup.length; i++){
        top_labels.push(top10ProductGroup[i]);
        top_export.push(Math.ceil(mergedData[top10ProductGroup[i]]['export']));
        top_import.push(Math.ceil(-mergedData[top10ProductGroup[i]]['import']));
    }

    let dataset = []
    if(top_import != []){
        dataset.push({
            label:'Import',
            data: top_import,
            borderColor: '#D4ECDD',
            backgroundColor: '#D4ECDD',
        })
    }
    if(top_export != []){
        dataset.push({
            label:'Export',
            data: top_export,
            borderColor: '#345B63',
            backgroundColor: '#345B63',
        })
    }

    const data = {
        labels: top_labels ,
        datasets :dataset
    };

    // Tooltip
    const tooltip = {
        yAlign: 'bottom',
        callbacks: {
            label: function(context) {
                return `${context.dataset.label}: ${Math.abs(context.raw).toLocaleString("en-US")}`;
            }
        }
    };

    // Plugins block
    const legendMargin = {
        id: 'legendMargin',
        beforeInit(chart, legend, options) {
            const fitValue = chart.legend.fit;

            chart.legend.fit = function fit() {
                fitValue.bind(chart.legend)();
                return this.height += 20;
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
            maintainAspectRatio: false,

            interaction: {
                intersect: false
            },

            indexAxis: 'y',
            // Elements options apply to all of the options unless overridden in a dataset
            // In this case, we are setting the border of each horizontal bar to be 2px wide
            elements: {
                bar: {
                    borderWidth: 0,
                }
            },
            plugins: {
                title: {
                    display: false
                },
                tooltip,
            },
            scales: {
                x: {
                    stacked: true,
                    ticks: {
                        callback: function(value, index, values) {
                            return Math.abs(value).toLocaleString("en-US");
                        }
                    }
                },
                y: {
                    beginAtZero: true,
                    stacked: true
                }
            }
        },

        plugins: [legendMargin]
    };

    // Chart object
    harborsChart = new Chart(
        // Indicate which Canvas Element to draw chart
        document.getElementById('harborsChart'),
        config
    );

    // Table
    let tableRows = "";

    for (let i = 0; i < sortedByNet.length; i++) {
        let productGroup = sortedByNet[i][2];

        let net = Math.ceil(sortedByNet[i][1]);

        let netStyle = 'text-success';

        if (net < 0) {
            netStyle = 'text-danger';
        }
        if(net === -0){
            net = 0;
        }
        tableRows += `<tr>
                           <th scope='row'>${i + 1}</th>
                           <td>${productGroup}</td>
                           <td class='${netStyle}'>${net.toLocaleString("en-US")}</td>
                       </tr>`;
    }


    document.getElementById('harborsTable').innerHTML = tableRows;
    document.getElementById('harborTableHeader').innerText = 'Harbor';
}

function createProductHarborChart(im_harborCategory, ex_harborCategory, label, unit ){

    let labels = label;
    let units = unit;

    let import_category_dict = {};
    let export_category_dict = {};
    let toBeSortedArr= [];

    for(let i=0; i< labels.length; i++) {

        for(let j=0; j<im_harborCategory.length;j++) {
            if(labels[i] == im_harborCategory[j][0]){
                if (units == null || units == "weight_tonne") {
                    import_category_dict[labels[i]] = Math.ceil(im_harborCategory[j][3]);

                } else if (units == "weight_kg") {
                    import_category_dict[labels[i]] = Math.ceil(im_harborCategory[j][2]);

                } else {
                    import_category_dict[labels[i]] = Math.ceil(im_harborCategory[j][1]);
                }

            }
        }
        if(import_category_dict[labels[i]] == undefined){
            import_category_dict[labels[i]] = 0
        }

        for(let k=0; k< ex_harborCategory.length; k++) {
            if(labels[i] == ex_harborCategory[k][0]){
                if (units == null || units == "weight_tonne") {
                    export_category_dict[labels[i]] = Math.ceil(ex_harborCategory[k][3]);

                } else if (units == "weight_kg") {
                    export_category_dict[labels[i]] = Math.ceil(ex_harborCategory[k][2]);

                } else {
                    export_category_dict[labels[i]] = Math.ceil(ex_harborCategory[k][1]);
                }

            }
        }
        if(export_category_dict[labels[i]] == undefined){
            export_category_dict[labels[i]] = 0
        }
    }

    for(let i =0; i< labels.length;i++){
        toBeSortedArr.push([import_category_dict[labels[i]] , export_category_dict[labels[i]], import_category_dict[labels[i]]+export_category_dict[labels[i]], export_category_dict[labels[i]]- import_category_dict[labels[i]], labels[i]]);
    }

    // Clone array and Sort by Net
    let sortedByNet = toBeSortedArr.slice();
    sortedByNet.sort(function(a, b){return b[3] - a[3]});

    let import_category_weight = [];
    let export_category_weight = [];
    let sortedLabels = [];

    for(let i=0; i< sortedByNet.length; i++) {
        // Change import to negative values first
        import_category_weight.push(Math.ceil(-sortedByNet[i][0]));
        export_category_weight.push(Math.ceil(sortedByNet[i][1]));

        sortedLabels.push(sortedByNet[i][4]);
    }

    const data = {
        labels: sortedLabels,
        datasets :[
            {
                label:'Import',
                data: import_category_weight,
                borderColor: '#D4ECDD',
                backgroundColor: '#D4ECDD',
            },
            {
                label:'Export',
                data: export_category_weight,
                borderColor: '#345B63',
                backgroundColor: '#345B63',
            },
        ]
    };

    // Tooltip
    const tooltip = {
        yAlign: 'bottom',
        callbacks: {
            label: function(context) {
                return `${context.dataset.label}: ${Math.abs(context.raw).toLocaleString("en-US")}`;
            }
        }
    };

    // Plugins block
    const legendMargin = {
        id: 'legendMargin',
        beforeInit(chart, legend, options) {
            const fitValue = chart.legend.fit;

            chart.legend.fit = function fit() {
                fitValue.bind(chart.legend)();
                return this.height += 20;
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
            maintainAspectRatio: false,

            interaction: {
                intersect: false
            },

            indexAxis: 'y',
            // Elements options apply to all of the options unless overridden in a dataset
            // In this case, we are setting the border of each horizontal bar to be 2px wide
            elements: {
                bar: {
                    borderWidth: 0,
                }
            },
            plugins: {
                title: {
                    display: false
                },
                tooltip,
            },
            scales: {
                x: {
                    stacked: true,
                    ticks: {
                        callback: function(value, index, values) {
                            return Math.abs(value).toLocaleString("en-US");
                        }
                    }
                },
                y: {
                    beginAtZero: true,
                    stacked: true
                }
            }
        },

        plugins: [legendMargin]
    };

    harborsChart = new Chart(
        // Indicate which Canvas Element to draw chart
        document.getElementById('harborsChart'),
        config
    );

    // Table
    let tableRows = "";

    for (let i = 0; i < sortedByNet.length; i++) {
        let productGroup = sortedByNet[i][4];

        let net = Math.ceil(sortedByNet[i][3]);

        let netStyle = 'text-success';

        if (net < 0) {
            netStyle = 'text-danger';
        }
        if(net === -0){
            net = 0;
        }
        tableRows += `<tr>
                           <th scope='row'>${i + 1}</th>
                           <td>${productGroup}</td>
                           <td class='${netStyle}'>${net.toLocaleString("en-US")}</td>
                       </tr>`;
    }

    document.getElementById('harborTableHeader').innerText = 'Product Group';
    document.getElementById('harborsTable').innerHTML = tableRows;
}

// Retrieves list of distinct values for each criteria (e.g. Year, Product Group, etc.)
// Populates dropdown list with retrieved values
async function loadFilterDropdown(criteriaList) {
    for (let i = 0; i < criteriaList.length; i++) {
        let criteria = criteriaList[i];

        const distinctList = await makeAPIRequest(getDistinct + 'selectParam=' + criteria, 'GET');

        let dropdownElement = document.getElementById(criteria + 'Filter');

        for (let x = 0; x < distinctList.length; x++) {
            dropdownElement.innerHTML += `<option value="${distinctList[x]}">${distinctList[x]}</option>`;
        }
    }
}

function changeToUpperOrLowerIfWordIsAll(word) {
    if (word != 'all' && word != 'All') {
        return word;
    }

    if (word == 'all') {
        return 'All';
    } else {
        return word.toLowerCase();
    }
}

// onclickCriteria = (e.g. Year, Country, Harbor)
// onclickValue = (e.g. 2019, 'Singapore', 'Balikpapan')
async function updateAllCharts(type = null, onclickCriteria = null, onClickValue = null) {
    // on click criteria is a list

    let year = null;
    let month = null;
    let category = null;
    let country = null;
    let harbor = null;
    let units = null;

    if (onclickCriteria === null & onclickCriteria === null) { // Case: Pressed Filter button

        // Retrieving values from filter dropdown list
        year = document.getElementById('yearFilter').value;
        month = document.getElementById('monthFilter').value;
        category = document.getElementById('categoryFilter').value;
        country = document.getElementById('countryFilter').value;
        harbor = document.getElementById('harborFilter').value;
        units = document.getElementById('unitsFilter').value;

    } else { // Case: On-click

        // Retrieving values from current filter section
        year = document.getElementById('currentYear').innerText;
        month = document.getElementById('currentMonth').innerText;
        category = document.getElementById('currentCategory').innerText;
        country = document.getElementById('currentCountry').innerText;
        harbor = document.getElementById('currentHarbor').innerText;
        units = document.getElementById('currentUnits').innerText;

        // Change to onclick value
        if (onclickCriteria == 'year') {
            year = onClickValue;
        } else if (onclickCriteria =='month'){
            month = onClickValue;
        } else if (onclickCriteria == 'country') {
            country = onClickValue;
        } else if (onclickCriteria == 'harbor') {
            harbor = onClickValue;
        }

        year = changeToUpperOrLowerIfWordIsAll(year);
        month = changeToUpperOrLowerIfWordIsAll(month);
        category = changeToUpperOrLowerIfWordIsAll(category);
        country = changeToUpperOrLowerIfWordIsAll(country);
        harbor = changeToUpperOrLowerIfWordIsAll(harbor);

        if (month != 'all') {
            month = monthsInteger[month];
        }

        if (units == 'Weight (MT)') {
            units = 'weight_tonne';
        } else if (units == 'Weight (KG)') {
            units = 'weight_kg';
        } else {
            units = 'value';
        }
    }

    await updateProductImportExportChart(year, month, category, country, harbor, units);
    await updateBreakdownImportChart(year, month,category, country, harbor, units);
    await updateBreakdownExportChart(year, month,category, country, harbor, units);
    await updateCountriesChart(year, month, category, country, harbor, units);
    await updateHarborsChart(year, month,category, country, harbor, units);

    year = changeToUpperOrLowerIfWordIsAll(year);
    month = changeToUpperOrLowerIfWordIsAll(month);
    category = changeToUpperOrLowerIfWordIsAll(category);
    country = changeToUpperOrLowerIfWordIsAll(country);
    harbor = changeToUpperOrLowerIfWordIsAll(harbor);

    if (month != 'All') {
        month = monthsArr[month - 1];
    }

    if (units == 'weight_tonne') {
        units = 'Weight (MT)';
    } else if (units == 'weight_kg') {
        units = 'Weight (KG)';
    } else {
        units = 'Value ($ USD)';
    }

    document.getElementById('currentYear').innerText = year;
    document.getElementById('currentMonth').innerText = month;
    document.getElementById('currentCategory').innerText = category;
    document.getElementById('currentCountry').innerText = country;
    document.getElementById('currentHarbor').innerText = harbor;
    document.getElementById('currentUnits').innerText = units;

    let yearsBtn = document.getElementById('goBackToYears');
    let yearsBtnTransparent = yearsBtn.classList.contains('d-none');

    let monthsBtn = document.getElementById('goBackToMonths');
    let monthsBtnTransparent = monthsBtn.classList.contains('d-none');

    if (year != 'All') {

        if (month == 'All') {
            if (yearsBtnTransparent) {
                yearsBtn.classList.remove('d-none');
            }

            if(!monthsBtnTransparent) {
                monthsBtn.classList.add('d-none');
            }

            year_count = 1;
            export_year_count = 1;

        } else {
            if (!yearsBtnTransparent) {
                yearsBtn.classList.add('d-none');
            }

            if (monthsBtnTransparent) {
                monthsBtn.classList.remove('d-none');
            }

            year_count = 2;
            export_year_count = 2;
        }
    } else {

        if (!yearsBtnTransparent) {
            yearsBtn.classList.add('d-none');
        }

        if (!monthsBtnTransparent) {
            monthsBtn.classList.add('d-none');
        }

        year_count = 0;
        export_year_count = 0;
    }

    let countryBtn = document.getElementById('goBackToCountries');
    let countryBtnTransparent = countryBtn.classList.contains('d-none');

    if (country == 'All') {
        if (!countryBtnTransparent) {
            countryBtn.classList.add('d-none');
        }

        country_count = 0;

    } else {
        if (countryBtnTransparent) {
            countryBtn.classList.remove('d-none');
        }

        country_count = 1;
    }

    let harborBtn = document.getElementById('goBackToHarbors');
    let harborBtnTransparent = harborBtn.classList.contains('d-none');

    if (harbor == 'All') {
        if (!harborBtnTransparent) {
            harborBtn.classList.add('d-none');
        }

        harbor_count = 0;

    } else {
        if (harborBtnTransparent) {
            harborBtn.classList.remove('d-none');
        }

        harbor_count = 1;
    }
}

async function updateProductImportExportChart(year, month, category, country, harbor, units) {
    // type, isExport, weightOrValue = null, yearsOrMonthsEndpoint = null, productsEndpoint = null, valueWeightEndpoint = null

    let yearsOrMonthsEndpoint = getDistinct + 'selectParam=year';
    let productsEndpoint = getDistinct + 'selectParam=category';
    let valueWeightEndpoint = getGroupBy + 'selectParam1=category&selectParam2=year';

    if (year != 'all') {
        yearsOrMonthsEndpoint = getDistinct + 'selectParam=month';
        valueWeightEndpoint = getGroupBy + 'selectParam1=category&selectParam2=month';
    }

    let filterParameters = '';

    if (year != 'all') {
        filterParameters += '&year=' + year;
    }

    if (month != 'all') {
        filterParameters += '&month=' + month;
    }

    if (category != 'all') {
        filterParameters += '&category=' + category;
    }

    if (country != 'all') {
        filterParameters += '&country=' + country;
    }

    if (harbor != 'all') {
        filterParameters += '&harbor=' + harbor;
    }

    yearsOrMonthsEndpoint += filterParameters;
    productsEndpoint += filterParameters;
    valueWeightEndpoint += filterParameters;

    productImportChart.destroy();
    await createProductCharts('update', 0, units, yearsOrMonthsEndpoint, productsEndpoint, valueWeightEndpoint);

    productExportChart.destroy();
    await createProductCharts('update', 1, units, yearsOrMonthsEndpoint, productsEndpoint, valueWeightEndpoint);
}

async function updateBreakdownImportChart(year, month, category, country, harbor, units) {
    let labels = getDistinct+"selectParam=category&isExport=0";
    let import_product_breakdown =getProductBreakdownImport;
    let added_param = "";

    if(year != "all"){
        added_param+=`&year=${year}`;
    }
    if(month !="all"){
        added_param +=`&month=${month}`;
    }
    if(category != "all"){
        added_param +=`&category=${category}`;
    }
    if(country!= "all"){
        added_param +=`&country=${country}`;
    }
    if(harbor != "all"){

        added_param +=`&harbor=${harbor}`;
    }

    let get_updated_label =  await makeAPIRequest(labels+added_param, 'GET');
    let get_updated_import = await makeAPIRequest(import_product_breakdown+added_param, 'GET');

    breakdownImportChart.destroy()
    await createBreakdownImportChart(get_updated_import, get_updated_label, units);
}

async function updateBreakdownExportChart(year, month,category, country,harbor, units) {
    let labels = getDistinct+"selectParam=category&isExport=1";
    let export_product_breakdown = getProductBreakdownExport;
    let added_param = "";

    if(year != "all"){
        added_param+=`&year=${year}`;
    }
    if(month !="all"){
        added_param +=`&month=${month}`;
    }
    if(category != "all"){
        added_param +=`&category=${category}`;
    }
    if(country!= "all"){
        added_param +=`&country=${country}`;
    }
    if(harbor != "all"){
        added_param +=`&harbor=${harbor}`;
    }

    let get_updated_label =  await makeAPIRequest(labels+added_param, 'GET');
    let get_updated_export = await makeAPIRequest(export_product_breakdown+added_param, 'GET');

    breakdownExportChart.destroy()
    await createBreakdownExportChart(get_updated_export, get_updated_label, units);
}

async function updateCountriesChart(year, month, category, country, harbor, units) {
    // type, weightOrValue = null, newEndpoint = null

    let filterParameters = '';

    if (year != 'all') {
        filterParameters += '&year=' + year;
    }

    if (month != 'all') {
        filterParameters += '&month=' + month;
    }

    if (category != 'all') {
        filterParameters += '&category=' + category;
    }

    if (country != 'all') {
        filterParameters += '&country=' + country;
    }

    if (harbor != 'all') {
        filterParameters += '&harbor=' + harbor;
    }

    if (country == 'all') {
        let newEndpoint = getGroupBy + 'selectParam1=country' + filterParameters;

        countriesChart.destroy();
        await createCountriesChart('update', units, newEndpoint);

        document.getElementById('countriesIndicator').innerText = 'Top 10';
    } else {
        let newEndpoint = getGroupBy + 'selectParam1=category' + filterParameters;

        countriesChart.destroy();
        await createCountriesProductsChart(units, newEndpoint);

        document.getElementById('countriesIndicator').innerText = country;
    }

}

async function updateHarborsChart(year, month, category, country, harbor, units) {

    let updated_labels = getDistinct+"selectParam=harbor"
    let updated_import_harbor = getharborImport;
    let updated_export_harbor = getharborExport;

    let added_param = "";

    if(year != "all"){
        added_param+=`&year=${year}`;
    }

    if(month !="all"){
        added_param +=`&month=${month}`;
    }

    if(category != "all"){
        added_param +=`&category=${category}`;
    }

    if(country!= "all"){
        added_param +=`&country=${country}`;
    }

    if(harbor != "all"){
        added_param +=`&harbor=${harbor}`

        let new_label = getDistinct +`selectParam=category${added_param}`;
        let import_harbor_product_group =getGroupBy+`selectParam1=category${added_param}&isExport=0`;
        let export_harbor_product_group =getGroupBy+`selectParam1=category${added_param}&isExport=1`;

        let get_new_label = await makeAPIRequest(new_label, 'GET');
        let im_harbor_product_group = await makeAPIRequest(import_harbor_product_group, 'GET');
        let ex_harbor_product_group = await makeAPIRequest(export_harbor_product_group, 'GET');

        harborsChart.destroy();
        document.getElementById('harborsIndicator').innerText = harbor;

        // To render 'No data found' if no data retrieved from endpoint
        let noDataMessageHarbors = document.getElementById('harborsNoData');
        let noDataMessageHarborsTransparent = noDataMessageHarbors.classList.contains('d-none');

        let harborsDiv = document.getElementById('harborsChartDiv');
        let harborsDivTransparent = harborsDiv.classList.contains('d-none');

        let harborsTableParent = document.getElementById('harborsTableParent');
        let harborsTableParentTransparent = harborsTableParent.classList.contains('d-none');

        if (get_new_label.status === 404 && im_harbor_product_group.status === 404 && ex_harbor_product_group.status === 404) {

            if (noDataMessageHarborsTransparent) {
                noDataMessageHarbors.classList.remove('d-none');
            }

            if (!harborsDivTransparent) {
                harborsDiv.classList.add('d-none');
            }

            if (!harborsTableParentTransparent) {
                harborsTableParent.classList.add('d-none');
            }

            return;

        } else {

            if (!noDataMessageHarborsTransparent) {
                noDataMessageHarbors.classList.add('d-none');
            }

            if (harborsDivTransparent) {
                harborsDiv.classList.remove('d-none');
            }

            if (harborsTableParentTransparent) {
                harborsTableParent.classList.remove('d-none');
            }

            createProductHarborChart(im_harbor_product_group, ex_harbor_product_group, get_new_label, units);
        }

    } else {

        let get_updated_label =  await makeAPIRequest(updated_labels + added_param, 'GET');
        let import_getUpdatedHarbor = await makeAPIRequest(updated_import_harbor + added_param, 'GET');
        let export_getUpdatedHarbor = await makeAPIRequest(updated_export_harbor + added_param, 'GET');

        harborsChart.destroy();
        await createHarborsChart(import_getUpdatedHarbor, export_getUpdatedHarbor, get_updated_label, units);

        document.getElementById('harborsIndicator').innerText = 'Top 10';

    }
}

var year_count = 0;
const product_import_ctx = document.getElementById("productImportChart");
product_import_ctx.onclick = annual_clickHandler;
async function annual_clickHandler(click) {

    let points = productImportChart.getElementsAtEventForMode(click, 'nearest', {intersect: true}, true);
    if(year_count <=1){
        if (points.length) {
            let firstPoint = points[0];

            let column_name = productImportChart.data.labels[firstPoint.index];
            let value = productImportChart.data.datasets[firstPoint.datasetIndex].data[firstPoint.index];

            if(year_count == 0){
                year_count++
                await updateAllCharts("update","year", column_name);

            } else {
                year_count++
                await updateAllCharts("update","month", column_name);
            }
        }
    }
}

var export_year_count = 0;
const product_export_ctx = document.getElementById("productExportChart");
product_export_ctx.onclick = export_annual_clickHandler;
async function export_annual_clickHandler(click) {

    let points = productExportChart.getElementsAtEventForMode(click, 'nearest', {intersect: true}, true);
    if(export_year_count <=1){
        if (points.length) {
            let firstPoint = points[0];

            let column_name = productExportChart.data.labels[firstPoint.index];
            let value = productExportChart.data.datasets[firstPoint.datasetIndex].data[firstPoint.index];

            if(export_year_count ==0){
                export_year_count++
                await updateAllCharts("update","year", column_name);

            }else{
                export_year_count++
                await updateAllCharts("update","month", column_name);
            }
        }
    }
}

var country_count = 0;
const country_ctx = document.getElementById("countriesChart");
country_ctx.onclick = country_clickHandler;
async function country_clickHandler(click) {

    let points = countriesChart.getElementsAtEventForMode(click, 'nearest', {intersect: true}, true);
    if(country_count ==0){
        if (points.length) {
            let firstPoint = points[0];

            let column_name = countriesChart.data.labels[firstPoint.index];
            let value = countriesChart.data.datasets[firstPoint.datasetIndex].data[firstPoint.index];

            await updateAllCharts("update", "country", column_name);

            country_count++
        }
    }
}

var harbor_count = 0;
const harbor_ctx = document.getElementById("harborsChart");
harbor_ctx.onclick = harbor_clickHandler;
async function harbor_clickHandler(click) {

    let points = harborsChart.getElementsAtEventForMode(click, 'nearest', {intersect: true}, true);
    if(harbor_count ==0){
        if (points.length) {
            let firstPoint = points[0];

            let column_name = harborsChart.data.labels[firstPoint.index];
            let value = harborsChart.data.datasets[firstPoint.datasetIndex].data[firstPoint.index];

            await updateAllCharts("update","harbor", column_name);

            harbor_count++
        }
    }

}

async function resetYears(){
    year_count = 0;
    export_year_count = 0;

    await updateAllCharts('reset', 'year', 'All');
}

async function resetMonths(){
    year_count = 1;
    export_year_count = 1;

    await updateAllCharts('reset', 'month', 'All');
}

async function resetCountry(){
    country_count = 0;

    updateAllCharts('reset','country', 'All');
}

async function resetHarbor(){
    harbor_count = 0;

    updateAllCharts('reset', 'harbor', 'All');
}