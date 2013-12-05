#!/usr/bin/env node

var net = require('net');
var client = net.connect({port: 8888},
    function() { //'connect' listener
  console.log('client connected');
  client.write('l');
});
client.on('data', function(data) {
  console.log(data.toString());
});
client.on('end', function() {
  console.log('client disconnected');
});
