const listener = new EventSource("/events");
listener.onmessage = e => {
};

function initSubmit(form) {
    form.submit(e => {
        try {
            $.ajax({
                url: form.attr("action"),
                type: form.attr("method"),
                data: form.serialize(),
                success: data => {
                    form.find(".ui.red.message").remove();
                    $.each(data, (k, v) => {
                        const selector = ".field [name=" + k + "]";
                        form.find(selector).each((i, el) => {
                            const field = $(el).closest(".field");

                            field.addClass("error");
                            field.append(`<div class="ui red message">${v}</div>`);
                        });
                    });
                }
            });
        } catch (x) {
            console.error("Form submission error: " + x);
        }
        return false;
    });
}