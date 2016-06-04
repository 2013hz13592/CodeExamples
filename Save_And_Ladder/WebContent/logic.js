var c=document.getElementById("blob");
var b=c.getContext("2d");
c.width=378;
c.height=378;

var noOftasks=0;
var x=0;
var speed=2;
//var curr_x=c.width - 30, curr_y=c.height-30;
var curr_x=c.width - 368, curr_y=c.height-28;
var o_x=curr_x, o_y=curr_y;
var background=new Image();

background.src="./GameBoard.png";
background.onload=function(){
    b.drawImage(background,0,0);
}
var dist = 0;
var Draw = function(){
    b.beginPath();
    b.lineJoin = 'round';
    b.fillStyle = "#b30000";
    b.rect(curr_x,curr_y,20,20);
    b.strokeRect(curr_x-2,curr_y-2,20+3,20+3);
    b.fill();
    b.closePath();
    b.globalCompositeOperation = "source-over";
} 
update();
//Animation function
function update()
{
	b.drawImage(background,0,0);
	Draw();
	if(noOftasks<=0){
		
	};  
	
	if(noOftasks==1){
		if(Math.abs(curr_x - o_x) <= 185 ){
			curr_x=curr_x+speed;
		}
	}
	
	if(noOftasks==2){
		if(Math.abs(curr_x - o_x) <= 338){
		   curr_x=curr_x+speed;
		   curr_y=curr_y;
		}
		if(Math.abs(curr_x - o_x) > 338){
			if(Math.abs(curr_y - o_y) <= 35 ){
				curr_y=curr_y-speed;
			}
		}
    } 	
   
	if(noOftasks==3){     
		if(Math.abs(curr_x - o_x) >= 78){
			   curr_x=curr_x-speed;
			   curr_y=curr_y;
		}
	}
   
	if(noOftasks==4){     
		if(dist < 75){
			curr_y = curr_y - 1;
			curr_x = curr_x +0.5;
		}
		dist++;
	}
	
	if(noOftasks==5){     
		if(Math.abs(curr_x - o_x) >= 1){
			   curr_x=curr_x-speed;
			   curr_y=curr_y;
		}
		
		if(Math.abs(curr_x - o_x) <= 1){
			if(Math.abs(curr_y - o_y) <= 150 ){
				curr_x=curr_x;
				curr_y=curr_y-speed;
			}
		}
	}
	//update function will be called recursively
	//When the if conditions above are false the animation effect will no longer
	//be visible
	window.requestAnimationFrame(update);
}