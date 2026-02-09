const hooks = require('hooks');
const Multipart = require('multi-part');
const fs = require('fs');

var responseStash = {};
var skipDeprecatedTransactionIds = [
    "POST (200) /sign-out", // this is a virtual endpoint mapped through Tyk
    "GET (200) /users/soundcloud%3Ausers%3A948745750/followings/soundcloud%3Ausers%3A25219981",
    "GET (200) /users/soundcloud%3Ausers%3A948745750/followers/soundcloud%3Ausers%3A743372812",
    "GET (200) /me/followers/soundcloud%3Ausers%3A743372812",
    "GET (200) /me/followings/soundcloud%3Ausers%3A948745750"
];
var skipTransactionIds = [
    "POST (200) /oauth2/token",
    "POST (401) /oauth2/token",
    "PUT (200) /tracks/soundcloud%3Atracks%3A308946187",
    "POST (201) /tracks/soundcloud%3Atracks%3A308946187/comments"
];
var replacePlaylistIdTransactionIds = [
    "PUT (200) /playlists/soundcloud%3Aplaylists%3A10",
    "DELETE (200) /playlists/soundcloud%3Aplaylists%3A10"
];
var skippedStatuses = ["400", "403", "404", "422", "429", "500"];

function addOAuthHeader(transaction) {
    const accessToken = process.env.OAUTH_TOKEN;

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
        addOAuthHeader(transaction);
    }

    if (replacePlaylistIdTransactionIds.includes(transaction.id)) {
        replaceId(transaction, '10', responseStash.playlist_id);
    }

    if (transaction.id == "PUT (200) /me/followings/soundcloud%3Ausers%3A743372812") {
        replaceId(transaction, '743372812', '25219981');
    }

    if (transaction. id == "PUT (200) /tracks/308946187" || transaction.id == "DELETE (200) /tracks/308946187" || transaction. id == "PUT (200) /tracks/soundcloud%3Atracks%3A308946187"|| transaction.id == "DELETE (200) /tracks/soundcloud%3Atracks%3A308946187") {
        replaceId(transaction, '308946187', responseStash.track_id);
    }
    done();
});

hooks.before("/tracks > Uploads a new track. > 201 > application/json; charset=utf-8", async (transaction, done) => {
    transaction.host = "asset-uploads";
    transaction.port = "5005";

    const form = new Multipart();
    form.append('track[title]', 'Test sample track');
    form.append('track[asset_data]', fs.createReadStream('./test/test-sample.mp3'), {filename: 'test-sample.mp3', contentType: 'audio/mpeg'});
    form.append('track[sharing]', 'private');

    transaction.request.body = (await form.buffer()).toString('base64');
    transaction.request.bodyEncoding = 'base64';
    transaction.request.headers['Content-Type'] = form.getHeaders()['content-type'];
    done();
});

hooks.before("/playlists > Creates a playlist. > 201 > application/json; charset=utf-8", async (transaction, done) => {
    const form = new Multipart();
    form.append('playlist[title]', 'Test title');
    form.append('playlist[sharing]', 'private');
    form.append('playlist[tracks][][urn]', [`soundcloud:tracks:219787221`,`soundcloud:tracks:783019264`,`soundcloud:tracks:870073492`]);

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
