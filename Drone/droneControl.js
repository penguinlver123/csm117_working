#!/usr/bin/env node

var arDrone = require('ar-drone');
var client  = arDrone.createClient();

var moveSpeed = 0.2;
var rotSpeed = 0.2;
var stdin = process.openStdin();
stdin.resume();
stdin.setRawMode(true);
stdin.setEncoding('utf8');

stdin.on( 'data', function( key ){
  if ( key === '\u0003' ) {
    process.exit();
  }
  if(key){
    switch(key){
      case 't': process.stdout.write('TAKE OFF'+'\n');
		client.takeoff();
                break;
      case 'l': process.stdout.write('LAND'+'\n');
		client.land();
                break;
      case 'w': process.stdout.write('FRONT'+'\n');
		client.front(moveSpeed);
                break;
      case 's': process.stdout.write('BACK'+'\n');
		client.back(moveSpeed);
                break;
      case 'a': process.stdout.write('LEFT'+'\n');
		client.left(moveSpeed);
                break;
      case 'd': process.stdout.write('RIGHT'+'\n');
		client.right(moveSpeed);
                break;
      case 'h': process.stdout.write('CounterClockWise'+'\n');
		client.counterClockwise(rotSpeed);
                break;
      case 'k': process.stdout.write('ClockWise'+'\n');
		client.clockwise(rotSpeed);
                break;
      case 'u': process.stdout.write('UP'+'\n');
		client.up(moveSpeed);
                break;
      case 'j': process.stdout.write('DOWN'+'\n');
		client.down(moveSpeed);
                break;
      case ' ': process.stdout.write('STOP'+'\n');
		client.stop();
                break;

    }  
  }
});
