function addCellar() {
  $.get("add.html", data => $(data).modal('show'));
}

function removeCellar() {
  $("#list").find("div.active:first").each((i, e) => {
    const name = e.textContent;
    window.location = "/view/cellars/delete.html?" + $.param({name: name});
  });
}

$("#list > div").click(e => {
  $("#list div.active").each((i, e) => {
    $(e).removeClass("active");
  });
  $(e.target).addClass("active");
});