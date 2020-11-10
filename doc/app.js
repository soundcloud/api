
const express = require('express');
const app = express();
const swaggerUi = require('swagger-ui-express');
const YAML = require('yamljs');
const swaggerDocument = YAML.load('./api.yaml');
const path = require('path');

var options = {
  explorer: false,
  customCssUrl: '/assets/custom.css',
  customfavIcon: '/assets/favicon.ico',
  customSiteTitle: 'SoundCloud Public API Specification'
}

app.set('port', process.env.PORT || 3000);

app.use('/assets', express.static(path.join(__dirname, 'assets')));
app.use('/', swaggerUi.serve, swaggerUi.setup(swaggerDocument, options));

const server = app.listen(app.get('port'),
  function(){
    console.log("Express server listening on port " + app.get('port'));
});

process.on('SIGINT', () => {
  console.log('Closing http server.');
  server.close(() => {
    console.log('Http server closed.');
  });
});
