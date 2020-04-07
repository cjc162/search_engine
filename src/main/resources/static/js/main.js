$(document).ready(function () {
    $("#btnSubmit").click(function (event) {

        //stop submit the form, we will post it manually.
        event.preventDefault();
		$("#result").text("Uploading files, please wait...");
        fire_ajax_submit();
    });

});

function fire_ajax_submit() {
    var form = $('#fileUploadForm')[0];

    var data = new FormData(form);

    data.append("CustomField", "This is some extra data, testing");

    $("#btnSubmit").prop("disabled", true);

    $.ajax({
        type: "POST",
        enctype: 'multipart/form-data',
        url: "/api/upload/multi",
        data: data,
        processData: false,
        contentType: false,
        cache: false,
        timeout: 240000,
        success: function (data) {

            console.log("SUCCESS : ", data);
            
            $("#result").text("Building inverted index, please wait...");
			
			const jobId = data;
			setTimeout(() => {
				fire_ajax_job_check(jobId);
			}, 15000);
        },
        error: function (e) {

            $("#result").text(e.responseText);
            console.log("ERROR : ", e);
            $("#btnSubmit").prop("disabled", false);
			
        }
    });

}

function fire_ajax_job_check(jobId) {
    $.ajax({
        type: "GET",
        url: "/api/job?jobId=" + jobId,
        success: function (data) {
			if (data) {
				setTimeout(() => {
					fire_ajax_job_check(jobId);
				}, 15000);
			} else {
				$("#btnSubmit").prop("disabled", false);
	            $(location).attr('href', location.origin + '/actions');
			}
        },
        error: function (e) {

            $("#result").text(e.responseText);
            console.log("ERROR : ", e);
            $("#btnSubmit").prop("disabled", false);
			
        }
    });
}