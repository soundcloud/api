const hooks = require('hooks');
var skipDeprecatedTransactionIds = [
    "GET (200) /users/948745750/followings/743372812",
    "GET (200) /users/948745750/followers/743372812",
    "GET (200) /tracks/463532259/favoriters/948745750",
    "GET (200) /me/followers/743372812",
    "GET (200) /me/tracks/463532259",
    "GET (200) /me/followings/948745750"
];
var skipTransactionIds = [
    "GET (200) /me/connections/123456"
];

function addCredentials(transaction) {
    const clientId = process.env.CLIENT_ID;
    const accessToken = process.env.ACCESS_TOKEN;

    var paramToAdd = "client_id=u1aX7EnUd90ul1sbwLwj7cN6fqytmrcV";
    if (transaction.fullPath.indexOf('?') > -1) {
       transaction.fullPath += "&" + paramToAdd;
    } else {
      transaction.fullPath += "?" + paramToAdd;
    }

    transaction.request.headers.Authorization = "OAuth 1-292145-948745750-f92d4f20a8c56";
    return transaction;
}

hooks.beforeEach((transaction, done) => {
    if (transaction.expected.statusCode > "302" || transaction.request.method != "GET"
    || skipDeprecatedTransactionIds.includes(transaction.id) || skipTransactionIds.includes(transaction.id)) {
        transaction.skip = true;
    }

    if (transaction.expected.statusCode != "401") {
            addCredentials(transaction);
    }

    done();
});

hooks.before("/connect > The OAuth2 authorization endpoint. Your app redirects a user to this endpoint, allowing them to delegate access to their account. > 200", (transaction, done) => {
    var newPath = transaction.fullPath.replace("?client_id=some%20client&", "?");
    transaction.fullPath = newPath;
    done();
});
