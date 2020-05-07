$(document).ready(function() {
    "use strict";

    $("#searchArtist").on("click", searchArtist);
    $("#artistSelect").on("change", displayLyricInfo);

    function searchArtist() {

        let searchTerm = $("#artistSearchTerm").val();
        if (searchTerm === "") {
            $("#artistSelectSection").hide();
            $("#results").text("");
            return;
        }

        var url = "http://localhost:8080/api/search/" + searchTerm;
        $.get(url, function(res) {
            $("#artistSelect").html("<option value=\"\"></option>");
            $.each(res, function(index, artist) {
                let disambiguation = artist.disambiguation !== null ? ` (${artist.disambiguation})` : "";
                let optionText = `<option value=${artist.id} id=${artist.id}>${artist.name}${disambiguation}</option>`;
                $("#artistSelect").append(optionText);
            });

            $("#artistSelectSection").show();
        });
    }

    function displayLyricInfo() {
        let artistId = $("#artistSelect").val();
        if (artistId === "") {
            $("#results").text("");
            return;
        }

        $("#results").text("Please wait...");
        let url = "http://localhost:8080/api/lyrics/" + artistId;
        $.get(url, function(res) {
            let artistName = $("#" + artistId).text();
            let resultStr
            if (parseInt(res) === 0) {
                resultStr = `Failed to find recordings by ${artistName}.`
            } else {
                resultStr = `The artist ${artistName} has an average of ${res.toFixed(2)} words in all of their songs.`;
            }

            $("#results").text(resultStr);
        });
    }

});