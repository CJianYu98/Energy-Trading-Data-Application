// Indo Endpoints
const getDistinctMonth = "/indoData/getDistinctByCriteria?selectParam=month&year=";

const exportGetWeightByYear = "/indoData/getByCriteriaGroupBy?selectParam1=year&isExport=1";
const importGetWeightByYear = "/indoData/getByCriteriaGroupBy?selectParam1=year&isExport=0";

const exportGetWeightByMonth = "/indoData/getByCriteriaGroupBy?selectParam1=month&isExport=1&year="
const importGetWeightByMonth = "/indoData/getByCriteriaGroupBy?selectParam1=month&isExport=0&year="

const exportGetCategory = "/indoData/getByCriteriaGroupBy?selectParam1=category&isExport=1";
const importGetCategory= "/indoData/getByCriteriaGroupBy?selectParam1=category&isExport=0";

// Taiwan Endpoints
const getGrossBalance = '/twOverall/getGrossBalance';
const getNetBalance = '/twSupply/getNetBalance';
const getNetImport = '/twSupply/getNetImport';

const getGrossBalanceOverTime = '/twOverall/getGrossBalanceOverTime';
const getNetBalanceOverTime = '/twSupply/getNetBalanceOverTime';
const getNetImportOverTime = '/twSupply/getNetImportOverTime';

const getImportExportOverTime = '/twSupply/getSupplyTypeOverTime';

// Chart objects
let indo_lineChart = null;
let indo_netLineChart = null;

// Taiwan chart objects
let tripleChart = null;
let importExportChart = null;

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

const integerMonths = {
    1: 'Jan',
    2: 'Feb',
    3: 'Mar',
    4: 'Apr',
    5: 'May',
    6: 'Jun',
    7: 'Jul',
    8: 'Aug',
    9: 'Sep',
    10: 'Oct',
    11: 'Nov',
    12: 'Dec'
}

async function makeAPIRequest(endpoint) {
    try {
        const response = await fetch(endpoint);

        return await response.json();

    } catch(error) {
        console.log(error);
        return error;
    }
}

async function getAll(type) {
    // INDO
    // Get weight over the years for export & import
    const exportData = await makeAPIRequest(exportGetWeightByYear);
    const importData = await makeAPIRequest(importGetWeightByYear);

    let all_years =[];
    // getting the weights per year for import and export
    let annual_export_weight = [];
    let annual_import_weight = [];
    for(let j in exportData){
        annual_export_weight.push(Math.ceil(exportData[j][3]))
        annual_import_weight.push(Math.ceil(importData[j][3]))
        all_years.push(exportData[j][0])
    }

    //calculating net value of import vs export
    let annual_net_weight = [];

    for(let i =0;  i< annual_export_weight.length; i++){
        annual_net_weight.push(annual_export_weight[i] - annual_import_weight[i])
    }

    // making indicator value dynamic
    let total_export=0;
    let total_import = 0
    for(let i = 0; i< annual_import_weight.length; i++){
        total_export += annual_export_weight[i];
        total_import += annual_import_weight[i];
    }
    let overall_net = total_export - total_import;
    overall_net = overall_net.toLocaleString("en-US");

    document.getElementById("indicator_value").innerHTML = overall_net;
    //getting best performing and worst perforing product group

    let exportCategory =await makeAPIRequest(exportGetCategory);
    let importCategory = await makeAPIRequest(importGetCategory);

    let export_max =0;
    let import_max = 0;
    let export_best_category ="";
    let import_best_category ="";

    for(let category in exportCategory){
        if(exportCategory[category][3] >= export_max){
            export_max= exportCategory[category][3];
            export_best_category =exportCategory[category][0] ;
        }
    }
    for(let category in importCategory){
        if(importCategory[category][3] >= import_max){
            import_max= importCategory[category][3];
            import_best_category =importCategory[category][0] ;
        }
    }

    document.getElementById("import_best").innerHTML = import_best_category;
    document.getElementById("export_best").innerHTML = export_best_category;

    createLineChart(type, annual_export_weight, annual_import_weight, all_years);
    createNetLineChart(type, annual_net_weight, all_years);
}

function createLineChart(type, export_weight, import_weight, labels){
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

    const data = {
        labels: labels,
        datasets: [
            {
                backgroundColor: '#D4ECDD',
                borderColor: '#D4ECDD',
                type: 'line',
                label: 'Import',
                data: import_weight,
                pointBorderColor: 'rgb(0, 0, 0)'
            },
            {
                backgroundColor: '#345B63',
                borderColor: '#345B63',
                type: 'line',
                label: 'Export',
                data: export_weight,
                pointBorderColor: 'rgb(0, 0, 0)'
            }
        ]
    };

    if (type == 'new') {
        indo_lineChart = new Chart(document.getElementById('lineChart'), {
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
                            text: 'MT',
                            padding: {top: 0, left: 0, right: 0, bottom: 0}
                        }
                    }
                }
            },

            plugins: [legendMargin]
        });

    } else {
        indo_lineChart.data = data;
        indo_lineChart.update();
    }

}
function createNetLineChart(type, net_weight, labels){
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

    const data = {
        labels: labels,
        datasets: [{
            backgroundColor: '#548CA8',
            borderColor: '#548CA8',
            type: 'line',
            label: 'Net',
            data: net_weight,
            pointBorderColor: 'rgb(0, 0, 0)'
        }]
    };

    if (type == 'new') {
        indo_netLineChart = new Chart(document.getElementById('netLineChart'), {
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
                            text: 'MT',
                            padding: {top: 0, left: 0, right: 0, bottom: 0}
                        }
                    }
                }
            },

            plugins: [legendMargin]
        });

    } else {
        indo_netLineChart.data = data;
        indo_netLineChart.update();
    }
}

//initialize count to limit num of clicks
let count = 0;
const ctx = document.getElementById("lineChart");
ctx.onclick = clickHandler;

async function clickHandler(click) {
    const points = indo_lineChart.getElementsAtEventForMode(click, 'nearest', {intersect: true}, true);
    if (points.length) {
        const firstPoint = points[0];

        const column_name = indo_lineChart.data.labels[firstPoint.index];

        //  after click change the graph,the returned value :
        // getting the months in the year
        if(count < 1){
            let distinctMonths = getDistinctMonth + column_name;
            let distinct_months = await makeAPIRequest(distinctMonths);

            let new_labels = [];
            for(let i =0; i<  distinct_months.length ; i++ ){
                new_labels.push(integerMonths[distinct_months[i]]);
            }

            // get new weight of both import export by their months
            let export_monthly_weight = exportGetWeightByMonth+column_name;
            let import_monthly_weight = importGetWeightByMonth+column_name;
            let monthly_export_weight = await makeAPIRequest(export_monthly_weight);
            let monthly_import_weight = await makeAPIRequest(import_monthly_weight);

            let monthly_export_weightList=[];
            let monthly_import_weightList=[];
            for(let i =0; i< distinct_months.length; i++){
                // appending to months_list according to each month
                for(let j=0; j< monthly_export_weight.length;j++){
                    if(monthly_export_weight[j][0] == distinct_months[i]){
                        monthly_export_weightList.push(Math.ceil(monthly_export_weight[j][3]));
                    }
                    if(monthly_import_weight[j][0] == distinct_months[i]){
                        monthly_import_weightList.push(Math.ceil(monthly_import_weight[j][3]));
                    }

                }
            }

            // calculating net value for each month
            //calculating net value of import vs export
            let monthly_net_weight = [];

            for(let i =0;  i< monthly_export_weightList.length; i++){
                monthly_net_weight.push(monthly_export_weightList[i] - monthly_import_weightList[i])
            }

            createLineChart('update', monthly_export_weightList, monthly_import_weightList, new_labels);
            createNetLineChart('update', monthly_net_weight, new_labels)

            //    update indicator value
            let total_export=0;
            let total_import = 0
            for(let i = 0; i< monthly_export_weightList.length; i++){
                total_export += monthly_export_weightList[i];
                total_import += monthly_import_weightList[i];
            }
            let overall_net = total_export - total_import;
            overall_net = overall_net.toLocaleString("en-US");
            //
            document.getElementById("indicator_value").innerHTML = overall_net;

            //getting best performing and worst performing product group

            let exportCategory =await makeAPIRequest(exportGetCategory+"&year="+column_name);
            let importCategory = await makeAPIRequest(importGetCategory+"&year="+column_name);

            let export_max =0;
            let import_max = 0;
            let export_best_category ="";
            let import_best_category ="";
            //
            for(let category in exportCategory){
                if(exportCategory[category][3] >= export_max){
                    export_max= exportCategory[category][3];
                    export_best_category =exportCategory[category][0] ;
                }
            }
            for(let category in importCategory){
                if(importCategory[category][3] >= import_max){
                    import_max= importCategory[category][3];
                    import_best_category =importCategory[category][0] ;
                }
            }
            document.getElementById("import_best").innerHTML = import_best_category;
            document.getElementById("export_best").innerHTML = export_best_category;

            count++;
            net_count++;

            // Make reset button visible
            document.getElementById('resetBtn').classList.toggle("d-none");

            // Change the Years of Indicators
            document.getElementById('import_best_years').innerText = "Year " + column_name;
            document.getElementById('export_best_years').innerText = "Year " + column_name;
            document.getElementById('indicator_years').innerText = "Year " + column_name;
        }
    }
}

//initialize count to limit num of clicks
let net_count = 0;
const netChart_ctx = document.getElementById("netLineChart");
netChart_ctx.onclick = netChart_clickHandler;

async function netChart_clickHandler(click) {
    const points = indo_netLineChart.getElementsAtEventForMode(click, 'nearest', {intersect: true}, true);

    if (points.length) {
        const firstPoint = points[0];

        const column_name = indo_netLineChart.data.labels[firstPoint.index];

        //  after click change the graph,the returned value :
        // getting the months in the year
        if(net_count < 1) {
            let distinctMonths = getDistinctMonth + column_name;
            let distinct_months = await makeAPIRequest(distinctMonths);

            let new_labels = [];
            for(let i =0; i<  distinct_months.length ; i++ ){
                new_labels.push(integerMonths[distinct_months[i]]);
            }

            // get new weight of both import export by their months
            let export_monthly_weight = exportGetWeightByMonth+column_name;
            let import_monthly_weight = importGetWeightByMonth+column_name;
            let monthly_export_weight = await makeAPIRequest(export_monthly_weight);
            let monthly_import_weight = await makeAPIRequest(import_monthly_weight);

            let monthly_export_weightList=[];
            let monthly_import_weightList=[];
            for(let i =0; i< distinct_months.length; i++){
                // appending to months_list according to each month
                for(let j=0; j< monthly_export_weight.length;j++){
                    if(monthly_export_weight[j][0] == distinct_months[i]){
                        monthly_export_weightList.push(Math.ceil(monthly_export_weight[j][3]));
                    }
                    if(monthly_import_weight[j][0] == distinct_months[i]){
                        monthly_import_weightList.push(Math.ceil(monthly_import_weight[j][3]));
                    }

                }
            }

            // calculating net value for each month
            //calculating net value of import vs export
            let monthly_net_weight = [];

            for(let i =0;  i< monthly_export_weightList.length; i++){
                monthly_net_weight.push(monthly_export_weightList[i] - monthly_import_weightList[i])
            }

            createLineChart('update', monthly_export_weightList, monthly_import_weightList, new_labels);
            createNetLineChart('update', monthly_net_weight, new_labels)

            //    update indicator value
            let total_export=0;
            let total_import = 0
            for(let i = 0; i< monthly_export_weightList.length; i++){
                total_export += monthly_export_weightList[i];
                total_import += monthly_import_weightList[i];
            }
            let overall_net = total_export - total_import;
            overall_net = overall_net.toLocaleString("en-US");
            //
            document.getElementById("indicator_value").innerHTML = overall_net;

            //getting best performing and worst performing product group

            let exportCategory =await makeAPIRequest(exportGetCategory+"&year="+column_name);
            let importCategory = await makeAPIRequest(importGetCategory+"&year="+column_name);

            let export_max =0;
            let import_max = 0;
            let export_best_category ="";
            let import_best_category ="";
            //
            for(let category in exportCategory){
                if(exportCategory[category][3] >= export_max){
                    export_max= exportCategory[category][3];
                    export_best_category =exportCategory[category][0] ;
                }
            }
            for(let category in importCategory){
                if(importCategory[category][3] >= import_max){
                    import_max= importCategory[category][3];
                    import_best_category =importCategory[category][0] ;
                }
            }
            document.getElementById("import_best").innerHTML = import_best_category;
            document.getElementById("export_best").innerHTML = export_best_category;

            count++;
            net_count++;

            // Make reset button visible
            document.getElementById('resetBtn').classList.toggle("d-none");

            // Change the Years of Indicators
            document.getElementById('import_best_years').innerText = "Year " + column_name;
            document.getElementById('export_best_years').innerText = "Year " + column_name;
            document.getElementById('indicator_years').innerText = "Year " + column_name;
        }
    }
}

async function resetToYear(){
    // Make reset button disappear
    document.getElementById('resetBtn').classList.toggle("d-none");

    // Change the Years of Indicators
    document.getElementById('import_best_years').innerText = "across Years";
    document.getElementById('export_best_years').innerText = "across Years";
    document.getElementById('indicator_years').innerText = "across Years";

    await getAll('update');

    count = 0;
    net_count = 0;
}

async function tw_getAll() {
    await createGrossBalanceIndicator();
    await createNetBalanceIndicator();
    await createNetImportIndicator();

    await createTripleChart('new');
    await createImportExportChart('new');
}

async function createGrossBalanceIndicator(year = null) {
    let queryParams = '';

    if (year !== null) {
        queryParams = '?year=' + year;
    }

    let grossBalance = await makeAPIRequest(getGrossBalance + queryParams);

    document.getElementById('grossBalanceIndicator').innerText = Math.ceil(grossBalance).toLocaleString("en-US");
}

async function createNetBalanceIndicator(year = null) {
    let queryParams = '';

    if (year !== null) {
        queryParams = '?year=' + year;
    }

    let netBalance = await makeAPIRequest(getNetBalance + queryParams);

    document.getElementById('netBalanceIndicator').innerText = Math.ceil(netBalance).toLocaleString("en-US");
}

async function createNetImportIndicator(year = null) {
    let queryParams = '';

    if (year !== null) {
        queryParams = '?year=' + year;
    }

    let netBalance = await makeAPIRequest(getNetImport + queryParams);

    document.getElementById('netImportIndicator').innerText = Math.ceil(netBalance).toLocaleString("en-US");
}

/* type == 'new' --> if building new chart (onload)
   type == 'update' --> if updating chart (onclick or reset button)
   Optional default arguments are for all years OR reset button cases
   Insert time == 'month' and year == 2019 for onclick case */
async function createTripleChart(type, time = 'year', year = null) {
    let params = '';

    if (time === 'year') {
        params = '?selectParam=year';
    } else {
        params = '?selectParam=month&year=' + year;
    }

    let grossBalanceResponse = await makeAPIRequest(getGrossBalanceOverTime + params);
    let netBalanceResponse = await makeAPIRequest(getNetBalanceOverTime + params);
    let netImportResponse = await makeAPIRequest(getNetImportOverTime + params);

    let grossBalances = grossBalanceResponse['volumes'];
    let netBalances = netBalanceResponse['volumes'];
    let netImports = netImportResponse['volumes'];

    let grossBalancesRounded = grossBalances.map(function(each_element) { return Math.ceil(each_element) });
    let netBalancesRounded = netBalances.map(function(each_element) { return Math.ceil(each_element) });
    let netImportsRounded = netImports.map(function(each_element) { return Math.ceil(each_element)} );

    const datasets = [
        {
            type: 'line',
            label: 'Gross Balance',
            backgroundColor: colorsRGBA[0],
            borderColor: colorsRGBA[0],
            data: grossBalancesRounded,
            pointBorderColor: 'rgb(0, 0, 0)'
        },
        {
            type: 'line',
            label: 'Net Import',
            backgroundColor: '#548CA8',
            borderColor: '#548CA8',
            data: netImportsRounded,
            pointBorderColor: 'rgb(0, 0, 0)'
        },
        {
            label: 'Net Balance',
            backgroundColor: colorsRGBA[2],
            borderColor: colorsRGBA[2],
            data: netBalancesRounded,
            pointBorderColor: 'rgb(0, 0, 0)'
        }
    ];

    let timeArr = grossBalanceResponse['time'];

    // If data is in months, convert int months to string
    let yearsOrMonths = [];

    if (time == 'year') {
        yearsOrMonths = timeArr;
    } else {
        for (let i = 0; i < timeArr.length; i++) {
            yearsOrMonths.push(integerMonths[timeArr[i]]);
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
        type: 'bar',

        // Data for the dataset - defined above
        data: data,

        // Configuration options
        options: {
            interaction: {
                intersect: false,
                mode: 'index'
            },

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
        tripleChart = new Chart(
            // Indicate which Canvas Element to draw chart
            document.getElementById('tripleChart'),
            config
        );
    } else {
        tripleChart.data = data;
        tripleChart.update();
    }
}

/* type == 'new' --> if building new chart (onload)
   type == 'update' --> if updating chart (onclick or reset button)
   Optional default arguments are for all years OR reset button cases
   Insert time == 'month' and year == 2019 for onclick case */
async function createImportExportChart(type, time = 'year', year = null) {
    let params = '';

    if (time === 'year') {
        params = '?selectParam=year';
    } else {
        params = '?selectParam=month&year=' + year;
    }

    let importResponse = await makeAPIRequest(getImportExportOverTime + params + '&type=Import');
    let exportResponse = await makeAPIRequest(getImportExportOverTime + params + '&type=Export');

    let imports = importResponse['volumes'];
    let exports = exportResponse['volumes'];

    let importsRounded = imports.map(function(each_element) { return Math.ceil(each_element) });
    let exportsRounded = exports.map(function(each_element) { return Math.ceil(each_element) });

    const datasets = [
        {
            label: 'Import',
            backgroundColor: '#D4ECDD',
            borderColor: '#D4ECDD',
            data: importsRounded,
            pointBorderColor: 'rgb(0, 0, 0)'
        },
        {
            label: 'Export',
            backgroundColor: '#345B63',
            borderColor: '#345B63',
            data: exportsRounded,
            pointBorderColor: 'rgb(0, 0, 0)'
        }
    ];

    let timeArr = importResponse['time'];

    // If data is in months, convert int months to string
    let yearsOrMonths = [];

    if (time == 'year') {
        yearsOrMonths = timeArr;
    } else {
        for (let i = 0; i < timeArr.length; i++) {
            yearsOrMonths.push(integerMonths[timeArr[i]]);
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
        importExportChart = new Chart(
            // Indicate which Canvas Element to draw chart
            document.getElementById('importExportChart'),
            config
        );
    } else {
        importExportChart.data = data;
        importExportChart.update();
    }
}

// Onclick for tripleChart
let triple_count = 0;
const triple_ctx = document.getElementById("tripleChart");
triple_ctx.onclick = tripleChart_clickHandler;

async function tripleChart_clickHandler(click) {
    let points = tripleChart.getElementsAtEventForMode(click, 'nearest', {intersect: true}, true);

    if(triple_count == 0){
        if (points.length) {
            let firstPoint = points[0];

            let year_clicked = tripleChart.data.labels[firstPoint.index];

            await updateTaiwanCharts(year_clicked);
        }
    }
}

// Onclick for importExportChart
let importExport_count = 0;
const importExport_ctx = document.getElementById("importExportChart");
importExport_ctx.onclick = importExportChart_clickHandler;

async function importExportChart_clickHandler(click) {
    let points = importExportChart.getElementsAtEventForMode(click, 'nearest', {intersect: true}, true);

    if(importExport_count == 0){
        if (points.length) {
            let firstPoint = points[0];

            let year_clicked = importExportChart.data.labels[firstPoint.index];

            await updateTaiwanCharts(year_clicked);
        }
    }
}

// By default, catered for reset button case
// Insert a year for onclick case
async function updateTaiwanCharts(year_clicked = null) {
    let year = '';

    if (year_clicked == null) { // For reset button case
        await createTripleChart('update');
        await createImportExportChart('update');

        triple_count = 0;
        importExport_count = 0;

        year = 'across Years';

    } else {
        // Updating count to prevent person from clicking on chart again
        // Until they pressed the reset button
        triple_count++;
        importExport_count++;

        await createTripleChart('update', 'month', year_clicked);
        await createImportExportChart('update', 'month', year_clicked);

        year = 'Year ' + year_clicked;
    }

    document.getElementById('resetBtnTw').classList.toggle('d-none');

    await createGrossBalanceIndicator(year_clicked);
    await createNetBalanceIndicator(year_clicked);
    await createNetImportIndicator(year_clicked);

    document.getElementById('grossBalanceYears').innerText = year;
    document.getElementById('netBalanceYears').innerText = year;
    document.getElementById('netImportYears').innerText = year;
}