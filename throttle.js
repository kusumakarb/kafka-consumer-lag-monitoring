var throttleform = document.getElementById("throttleform");

throttleform.onsubmit = function (e) {

    e.preventDefault();

    var data = {};
    for (var i = 0, ii = throttleform.length; i < ii; ++i) {
        var input = throttleform[i];
        if (input.name) {
            data[input.name] = input.value;
        }
    }
    increaseThrottle(data)
}

function increaseThrottle(data){
    $.ajax({
        type: "POST",
        async: true,
        url: "http://localhost:9000/increaseThrottle",
        data: JSON.stringify(data),
        crossDomain: true,
        success: function (data, status, jqXHR) {
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