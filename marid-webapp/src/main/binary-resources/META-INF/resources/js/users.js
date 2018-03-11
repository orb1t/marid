const userContainer = $("#userContainer");
const users = $("#users");

function showUser(name) {
    userContainer.load("/users/user.html?" + $.param({name: name}) + " form > *");
}

users.find("a").click(e => {
    users.find("a.active").removeClass("active");
    $(e.target).addClass("active");
    showUser(e.target.textContent);
});

users.find("a").first().click();

initSubmit(userContainer);
