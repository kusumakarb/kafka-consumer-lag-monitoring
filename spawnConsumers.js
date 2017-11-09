var throttleform = document.getElementById("spawnConsumersForm");

throttleform.onsubmit = function (e) {

    e.preventDefault();

    var data = {};
    for (var i = 0, ii = throttleform.length; i < ii; ++i) {
        var input = throttleform[i];
        if (input.name) {
            data[input.name] = input.value;
        }
    }
    addData(data)
}

function addData(data){
    $.ajax({
        type: "GET",
        async: true,
        url: "http://localhost:9000/spawnConsumer",
        data: JSON.stringify(data),
        crossDomain: true,
        success: function (data, status, jqXHR) {
            var div = document.getElementById("textDiv");
            div.textContent = data;
            var text = div.textContent;
            console.log(data)
            alert("success");
        },

        error: function (jqXHR, status) {
            // error handler
            console.log(jqXHR);
            console.log(status)
            alert('fail' + jqXHR);
        }
    });
}