const hooks = require('hooks');
const Multipart = require('multi-part');
const fs = require('fs');

var responseStash = {};
var skipDeprecatedTransactionIds = [
    "GET (200) /users/948745750/followings/25219981",
    "GET (200) /users/948745750/followers/743372812",
    "GET (200) /me/followers/743372812",
    "GET (200) /me/followings/948745750"
];
var skipTransactionIds = [
    "POST (200) /oauth2/token",
    "POST (401) /oauth2/token",
    "GET (200) /me/connections/123456",
    "PUT (200) /tracks/308946187",
    "POST (201) /tracks/308946187/comments"
];
var replacePlaylistIdTransactionIds = [
    "PUT (200) /playlists/10",
    "DELETE (200) /playlists/10"
];
var skippedStatuses = ["400", "403", "404", "422", "429", "500"];

function addCredentials(transaction) {
    const clientId = process.env.CLIENT_ID;
    const accessToken = process.env.OAUTH_TOKEN;

    var paramToAdd = "client_id=" + clientId;
    if (transaction.fullPath.indexOf('?') > -1) {
       transaction.fullPath += "&" + paramToAdd;
    } else {
      transaction.fullPath += "?" + paramToAdd;
    }

    transaction.request.headers.Authorization = "OAuth " + accessToken;
    return transaction;
}

function replaceId(transaction, actual, updated) {
    var url = transaction.fullPath;
    transaction.fullPath = url.replace(actual, updated);
    return transaction;
}

hooks.beforeEach((transaction, done) => {
    if (skippedStatuses.includes(transaction.expected.statusCode)
        || skipDeprecatedTransactionIds.includes(transaction.id)
        || skipTransactionIds.includes(transaction.id)
        || transaction.request.headers["Content-Type"] == 'multipart/x-www-form-urlencoded') {
        transaction.skip = true;
    }

    if (transaction.expected.statusCode != "401") {
        addCredentials(transaction);
    }

    if (replacePlaylistIdTransactionIds.includes(transaction.id)) {
        replaceId(transaction, '10', responseStash.playlist_id);
    }

    if (transaction.id == "PUT (200) /me/followings/743372812") {
        replaceId(transaction, '743372812', '25219981');
    }

    if (transaction. id == "PUT (200) /tracks/308946187" || transaction.id == "DELETE (200) /tracks/308946187") {
        replaceId(transaction, '308946187', responseStash.track_id);
    }
    done();
});

hooks.before("/tracks > Uploads a new track. > 201 > application/json; charset=utf-8", async (transaction, done) => {
    transaction.host = "asset-uploads";
    transaction.port = "5005";

    const form = new Multipart();
    form.append('track[title]', 'Test sample track');
    form.append('track[asset_data]', fs.createReadStream('./test/test-sample.wav'), {filename: 'test-sample.wav', contentType: 'audio/wav'});
    form.append('track[sharing]', 'private');

    transaction.request.body = (await form.buffer()).toString('base64');
    transaction.request.bodyEncoding = 'base64';
    transaction.request.headers['Content-Type'] = form.getHeaders()['content-type'];
    done();
});

hooks.before("/connect > The OAuth2 authorization endpoint. Your app redirects a user to this endpoint, allowing them to delegate access to their account. > 200", (transaction, done) => {
    var newPath = transaction.fullPath.replace("?client_id=some%20client&", "?");
    transaction.fullPath = newPath;
    done();
});

hooks.after("/playlists > Creates a playlist. > 201 > application/json; charset=utf-8", (transaction, done) => {
    var responseBody = JSON.parse(transaction.real.body);
    responseStash.playlist_id = responseBody.id;
    done();
});

hooks.after("/tracks > Uploads a new track. > 201 > application/json; charset=utf-8", (transaction, done) => {
    if (typeof transaction.real !== 'undefined' && typeof transaction.real.body === 'string') {
      var responseBody = JSON.parse(transaction.real.body);
      responseStash.track_id = responseBody.id;
    }
    done();
});
