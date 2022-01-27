var canvas = document.getElementById('chart');
var ctx = canvas.getContext('2d');
const width = canvas.clientWidth;
const height = canvas.clientHeight;
const bar_width = 30;
var chart = 'bar';
var value = [];
var value_sum = 0;
var city = [];
var mainText;
var value_array;
var value_text;
var city_text;
var position = { //여백
    min_x: width * (1 - 0.6) / 2,
    max_x: width * 0.6,
    min_y: height * (1 - 0.75) / 2,
    max_y: height * 0.75
}   

function choice() {
    ctx.clearRect(0, 0, width, height);
    mainText = "전국 10대(청소년) 저축 현황";
    if (chart == "bar") {
        bar_draw();
    }
}

function bar_draw() {
    chart = 'bar';
    var maxval = maxvalue();
    var virval = 0;
    maxval = maxval - (maxval % 10) + 10;
    for (var i = 0; i <= maxval / 10; i++) {
        //==========================================================================================================
        ctx.strokeStyle = 'black';  // 10단위 눈금선
        ctx.beginPath();
        ctx.lineTo(position.min_x * 0.75, position.min_y + position.max_y * i / (maxval / 10));
        ctx.lineTo(position.min_x * 1.25 + position.max_x, position.min_y + position.max_y * i / (maxval / 10));
        ctx.stroke();
        //==========================================================================================================
        ctx.strokeStyle = 'gray';  // 5단위 눈금선
        if (i != maxval / 10) {
            ctx.beginPath();
            ctx.lineTo(position.min_x * 0.75,
                position.min_y + position.max_y * i / (maxval / 10) + position.max_y * 1 / (maxval / 10) * 0.5);
            ctx.lineTo(position.min_x * 1.25 + position.max_x,
                position.min_y + position.max_y * i / (maxval / 10) + position.max_y * 1 / (maxval / 10) * 0.5);
            ctx.stroke();
        }
        //==========================================================================================================
        ctx.font = '20px san-serif';  // y축 범례 (텍스트)
        ctx.fillStyle = 'black';
        ctx.textAlign = 'end';
        ctx.fillText((maxval - i * 10), position.min_x * 0.75 - 10, position.min_y + position.max_y * i / (maxval / 10) + 5);
    }
    //==========================================================================================================
    ctx.strokeStyle = 'black';  //  표 테두리
    ctx.strokeRect(position.min_x * 0.75, position.min_y, position.max_x + position.min_x * 0.5, position.max_y);
    drawrect(0);
    
    function drawrect(index) {
        ctx.fillStyle = '#f04f98';
        virval = 0;
        var interval = setInterval(() => {
            virval++;
            if (virval > value[index]) {
                virval = value[index];
                ctx.fillRect(position.min_x + position.max_x * ((index) / (value.length - 1)) - (bar_width / 2),
                    position.min_y + position.max_y * ((maxval - virval) / maxval),
                    bar_width,
                    position.max_y * virval / maxval);
                clearInterval(interval);
                if (index < value.length - 1) {
                    drawrect(index + 1);
                } else {
                    bar_text();
                }
            } else {
                ctx.fillRect(position.min_x + position.max_x * ((index) / (value.length - 1)) - (bar_width / 2),
                    position.min_y + position.max_y * ((maxval - virval) / maxval),
                    bar_width,
                    position.max_y * virval / maxval);
            }
        }, 3);

        function bar_text() {
            value.forEach((val, index) => {
                ctx.font = '20px san-serif'; // x축 범례 (종류)
                ctx.fillStyle = 'black';
                ctx.textAlign = 'center';
                ctx.fillText(city[index],
                    position.min_x + position.max_x * ((index) / (city.length - 1)),
                    position.max_y + position.min_y + 30);
            })
            ctx.font = 'bold 40px san-serif'; // 그래프 타이틀
            ctx.fillStyle = '#353842';
            ctx.textAlign = 'center';
            ctx.fillText(mainText, width / 2, position.min_y - 25);
            ctx.beginPath();
            ctx.lineTo(position.min_x - width * 0.05, position.min_y + position.max_y);
            ctx.lineTo(position.min_x + position.max_x + width * 0.05, position.min_y + position.max_y);
            ctx.stroke();
        }
    }
}

function maxvalue() {
    var max = 0
    
    for (var i = 0; i < value.length; i++) {
        if (value[i] > max) {
            max = value[i];
        }
    }
    return max;
}

function fun(){
	var request = new XMLHttpRequest();
	request.open("POST","Servlet",true);
	request.setRequestHeader("Content-Type", "application/json");
	request.send();
	request.onload = function(){
		if (request.status === 200) {
		
		var stringData = request.response;
		var jsonData = JSON.parse(stringData);
	
		value = Object.values(jsonData);
		city =  Object.keys(jsonData);
		
		choice();
		
		}	
	}	
}	

window.onload = fun();