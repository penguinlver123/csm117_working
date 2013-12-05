#!/usr/bin/env node
var net = require('net');
var arDrone = require('ar-drone');
var client  = arDrone.createClient();
var moveSpeed = 0.2;
var rotSpeed = 0.2;

var server = net.createServer(function(socket) { //'connection' listener
  console.log('server connected');
  socket.on('connection',function(socket){
        console.log('socket connection...');
    });

  socket.on('data',function(message){
      console.log('socket message:'+message+'\n');

    switch(message.toString()){
      case 't\r\n': console.log('TAKE OFF'+'\n');
		client.takeoff();
                break;
      case 'l\r\n': process.stdout.write('LAND'+'\n');
		client.land();
                break;
      case 'w\r\n': process.stdout.write('FORWARD'+'\n');
		client.front(moveSpeed);
                break;
      case 's\r\n': process.stdout.write('BACKWARD'+'\n');
		client.back(moveSpeed);
                break;
      case 'a\r\n': process.stdout.write('LEFT'+'\n');
		client.left(moveSpeed);
                break;
      case 'd\r\n': process.stdout.write('RIGHT'+'\n');
		client.right(moveSpeed);
                break;
      case 'h\r\n': process.stdout.write('CounterClockWise'+'\n');
		client.counterClockwise(rotSpeed);
                break;
      case 'k\r\n': process.stdout.write('ClockWise'+'\n');
		client.clockwise(rotSpeed);
                break;
      case 'u\r\n': process.stdout.write('UP'+'\n');
		client.up(moveSpeed);
                break;
      case 'j\r\n': process.stdout.write('DOWN'+'\n');
		client.down(moveSpeed);
                break;
      case 'f\r\n': process.stdout.write('FLIP'+'\n');
		client.animate('flipLeft', 15);
                break;
      case 'p\r\n': process.stdout.write('STOP'+'\n');
		client.stop();
                break;
      default: console.log('Nothing Received, message is:\"' +message+ '\"\n');
    }  
  });

  socket.on('end', function() {
    console.log('server disconnected');
  });
});

server.listen(8888, function() { //'listening' listener
 
  console.log('server bound');
});



