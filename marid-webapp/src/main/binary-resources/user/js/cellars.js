function addCellar() {
    $.get("add.html", data => $(data).modal("show"));
}