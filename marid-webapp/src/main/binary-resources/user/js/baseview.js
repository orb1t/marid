$(window).on('beforeunload', e => {
    console.info("Cleaning " + window.location.pathname);
    $.ajax('?clean');
});