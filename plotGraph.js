/**
 * Created by kusumakar on 14/9/17.
 */
var WEBSOCKET_HOST = "localhost"
var WEBSOCKET_PORT = "9000"
var WEBSOCKET = "ws://" + WEBSOCKET_HOST + ":" + WEBSOCKET_PORT + "/ws"
ws = new WebSocket(WEBSOCKET);

var request_data_interval;
var UPPERWARNING = 120;
var UPPERACTION = 130;
var TRANSACTION_BURST = 50;

var layout = {
    title: "Kafka Consumer Lag Monitor",
    xaxis: {title: 'Time'},
    yaxis: {title: 'Consumer Lag'}
};


function rand() {
    return Math.random();
}

ws.onopen = function() {

    var time = new Date().getTime();

    var lag = [{
        x: [time],
        y: [lag],
        mode: 'lines',
        line: {color: 'red'},
        name: 'Consumer Lag'
    }];

    Plotly.plot('graph', lag, layout);

    ws.send("getData");

};

var timeToInt = function (x) {
    return (new Date(parseInt(x)));
}

ws.onmessage = function (evt) {


    var received_msg = evt.data;

    var time = new Date();
    const data = JSON.parse(evt.data);
    console.log(data);
    var timeX = data.time;
    //var graphDataX = timeX.map(timeToInt);
    var graphDataX = timeX
    var graphDataY = data.lag;
    //time = graphDataX;

    var lag = {
        x:  [[graphDataX]],
        y: [[graphDataY]]
    };


    var olderTime = time.setSeconds(time.getSeconds() - 30);
    var futureTime = time.setSeconds(time.getSeconds() + 30);

    var minuteView = {
        xaxis: {
            type: 'date',
            range: [olderTime,futureTime]
        }
    };


    var layoutPromise =  Plotly.relayout('graph', minuteView, layout);

    var lagPromise = Plotly.extendTraces('graph', lag, [0]);

    var promisesList = [layoutPromise, lagPromise]

    Promise.all(promisesList).then(() => {
        ws.send("getData");
    });

    var processeddiv = document.getElementById("processedRowsPerSecond");
    processeddiv.textContent = data.processedRowsPerSecond;
    //var text = div.textContent;

    var inputdiv = document.getElementById("inputRowsPerSecond")
    inputdiv.textContent = data.inputRowsPerSecond;
};

ws.onclose = function() {
    // websocket is closed.
    console.log("Websocket Closed!")
};

function requestData() {
    ws.send("get-data");
}

