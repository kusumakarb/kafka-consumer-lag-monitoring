var executorForm = document.getElementById("spawnConsumersForm");

executorForm.onsubmit = function (e) {

    e.preventDefault();

    var data = {};
    for (var i = 0, ii = executorForm.length; i < ii; ++i) {
        var input = executorForm[i];
        if (input.name) {
            data[input.name] = input.value;
        }
    }
    addData(data)
}

function addData(data){
    $.ajax({
        type: "POST",
        async: true,
        url: "http://localhost:9000/increaseExecutors",
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