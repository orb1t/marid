function selectTab(element) {
    $("nav .nav-item.active").removeClass("active");
    const div = $(element).parent();
    div.addClass('active');
    $("main").load("/" + div.attr('id'));
}