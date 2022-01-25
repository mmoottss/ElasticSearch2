<%@page import="java.sql.DriverManager"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.Statement"%>
<%@page import="java.sql.Connection"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Map"%>
<%@page import="com.pcu.dao.*"%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%! 
/* 	ArrayList<String> alCity = new ArrayList<>();
	ArrayList<Long> alCnt = new ArrayList<>(); */
	long[] count = new long[12];
	String[] city = new String[12];
	
%>
<!DOCTYPE html>
<html> 
<head>   
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Canvas</title>
<style type="text/css">
        canvas {
            border: 1px solid black;
        }
        
        body {
            text-align: center;
        }
        
        #checkbox {
            background-color: rgb(197, 197, 197);
            font-size: 20px;
        }
        
        select {
            width: 60px;
        }
        
        option {
            text-align: center;
        }
    </style>
</head>
<body onload='getdb()'>
    <div id='checkbox' style="visibility: hidden; position: absolute;"></div>

    <canvas id="chart" width="1000" height="800"></canvas><br><br>
    <button id='button' onclick='button();' disabled='true'>원형차트</button>
    <select id="city" onchange="choice_city()" disabled='true'>
        <option value="none">선택</option>
    </select>
</body>
<%	
	Map<String,Long> m=EsDAO.getEsStat();
	int i=0;
	for(Map.Entry<String, Long> e: m.entrySet() ){
    	/* alCity.add(e.getKey());
    	alCnt.add(e.getValue()); */
    	count[i]=e.getValue();
    	city[i]=e.getKey();
    	i++;
	}
	for(int j = 0;j<12;j++){
		System.out.print("count = "+count[j]);
		System.out.println("  city = "+city[j]);
	}
%>


</html>
<script language=javascript>
var canvas = document.getElementById('chart');
var ctx = canvas.getContext('2d');
const width = canvas.clientWidth;
const height = canvas.clientHeight;
const bar_width = 40;
const radius = height / 3;
var chart = 'bar';
var value = "a";
var value_sum = 0;
var cityname = "<%=TrafficDAO.getCity()%>";
var mainText = [];
var value_array = "a";
var value_color = ['rgb(176, 91, 59)', 'rgb(202, 150, 92)', 'rgb(238, 195, 115)', 'rgb(244, 223, 186)', 'rgb(228, 205, 167)', 'rgb(195, 176, 145)', 'rgb(142, 128, 106)'];
var value_text = "a";
var position = { //여백
    min_x: width * (1 - 0.6) / 2,
    max_x: width * 0.6,
    min_y: height * (1 - 0.75) / 2,
    max_y: height * 0.75
}   

function buttoncontrol(bool) {
    var button = document.getElementById('button');
    button.disabled = bool;
    var select = document.getElementById("city");
    select.disabled = bool;
}

function getdb() {//JSP=>JavaScript 파싱
    cityname= cityname.replace("[", "").replace("]", "").replace(/\ /g,'');
    cityname = cityname.split(",");
    var ops = document.getElementById("city").options;

    ops.remove(0);
    cityname.forEach((val,index)=>{
        var op = new Option();
        op.value = cityname[index];
        op.text = cityname[index];
        ops.add(op);
    });
    value_text = value_text.replace("[", "").replace("]", "");
    value_text = value_text.split(",");
    value_array = value_array.substr(1);
    value_array = value_array.substr(0,value.length-3);
    value_array =value_array.replace(/\[/g, '');
    value_array = value_array.split("],");
    console.log(value_array);
    choice_city();
}

function choice_city() {
    ctx.clearRect(0, 0, width, height);
    var select = document.getElementById("city");
    var selectValue = select.options[select.selectedIndex].value;
    value = value_array[cityname.indexOf(selectValue)];
    value = JSON.parse("[" + value + "]") //value String=>Number
    value_sum = 0;
    value.forEach((val) => {
        value_sum += val;
    })
    var text = select.options[select.selectedIndex].value;
    text = text.replace("\"", "").replace("\"", "");
    mainText = text + " 대중교통 이용 목적";

    if (chart == "bar") {
        bar_draw();
    } else if (chart == "circle") {
        circle_draw();
    }

}

function bar_draw() {
    buttoncontrol(true);
    chart = 'bar';
    var maxval = maxvalue();
    var virval = 0;
    maxval = maxval - (maxval % 10) + 10;
    for (var i = 0; i <= maxval / 10; i++) {
        ctx.strokeStyle = 'black';
        ctx.beginPath();
        ctx.lineTo(position.min_x * 0.75, position.min_y + position.max_y * i / (maxval / 10));
        ctx.lineTo(position.min_x * 1.25 + position.max_x, position.min_y + position.max_y * i / (maxval / 10));
        ctx.stroke();
        ctx.strokeStyle = 'gray';
        if (i != maxval / 10) {
            ctx.beginPath();
            ctx.lineTo(position.min_x * 0.75,
                position.min_y + position.max_y * i / (maxval / 10) + position.max_y * 1 / (maxval / 10) * 0.5);
            ctx.lineTo(position.min_x * 1.25 + position.max_x,
                position.min_y + position.max_y * i / (maxval / 10) + position.max_y * 1 / (maxval / 10) * 0.5);
            ctx.stroke();
        }
        ctx.font = '16px san-serif';
        ctx.fillStyle = 'black';
        ctx.textAlign = 'end';
        ctx.fillText((maxval - i * 10) + "%", position.min_x * 0.75 - 10, position.min_y + position.max_y * i / (maxval / 10) + 5);
    }
    ctx.strokeStyle = 'black';
    ctx.strokeRect(position.min_x * 0.75, position.min_y, position.max_x + position.min_x * 0.5, position.max_y);
    drawrect(0);

     
    function drawrect(index) {
        ctx.fillStyle = 'rgba(199,211,237,1)';
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
        }, 8);

        function bar_text() {
            value.forEach((val, index) => {
                ctx.font = '16px san-serif';
                ctx.fillStyle = 'black';
                ctx.textAlign = 'center';
                ctx.fillText(value_text[index],
                    position.min_x + position.max_x * ((index) / (value.length - 1)),
                    position.max_y + position.min_y + 30);
            })
            ctx.font = '26px san-serif';
            ctx.fillStyle = 'black';
            ctx.textAlign = 'center';
            ctx.fillText(mainText, width / 2, position.min_y / 2);
            buttoncontrol(false);
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

function button() { //바, 파이

    var button = document.getElementById('button');
    if (chart == 'bar') {
        button.textContent = "바차트";
        ctx.clearRect(0, 0, width, height);
        circle_draw();
    } else {
        button.textContent = "원형차트";
        ctx.clearRect(0, 0, width, height);
        bar_draw();
    }
}

function circle_draw() {
    buttoncontrol(true);
    chart = 'circle';
    var startAngle = (Math.PI / 180) * (-90); //라디안 각도 변환 0도
    var endAngle;
    var sum = 0;
    loop(0);
    value.forEach((_val, index) => {
        ctx.fillStyle = value_color[index];
        ctx.fillRect(position.max_x + position.min_x, index * 25 + 40, 15, 15);
        ctx.font = '20px san-serif';
        ctx.fillStyle = "black";
        ctx.textAlign = 'start';
        ctx.fillText(value_text[index], position.max_x + position.min_x + 15, index * 25 + 55);
    });

    function loop(index) {
        var virval = 0;
        var interval = setInterval(function() {
            startAngle = (Math.PI / 180) * (-90) + sum;
            ctx.beginPath();
            ctx.fillStyle = value_color[index];
            virval++;
            if (virval > value[index]) {
                //그려지는 변수가 실제 변수보다 커질때 마무리
                virval = value[index];
                ctx.lineTo(width / 2, height / 2);
                endAngle = (Math.PI / 180) * ((-90) + 360 * virval / value_sum) + sum;
                ctx.arc(width / 2, height / 2, radius, startAngle, endAngle, false);
                ctx.fill();
                sum += (Math.PI / 180) * (360 * value[index] / value_sum);
                clearInterval(interval);
                if (index < value.length - 1) {
                    //인터벌 배열끝까지 루프
                    loop(index + 1);
                } else {
                    circle_text();
                }
            } else {
                endAngle = (Math.PI / 180) * ((-90) + 360 * virval / value_sum) + sum;
                ctx.arc(width / 2, height / 2, radius, startAngle, endAngle, false);
                ctx.lineTo(width / 2, height / 2);
                ctx.fill();
            }
        }, 5);
    }
}

function circle_text() { //원형 차트 글씨 삽입
    var sum = 0;
    ctx.font = '26px san-serif';
    ctx.fillStyle = 'black';
    ctx.textAlign = 'center';
    ctx.fillText(mainText, width / 2, position.min_y - 10);
    value.forEach((val) => {
        if (val > 5) { // 5%이하는 표기하지 않음.
            var x = Math.cos(((val / 2 + sum) * (360 / value_sum) - 90) * (Math.PI / 180)) * radius * 0.7 + width / 2;
            var y = Math.sin(((val / 2 + sum) * (360 / value_sum) - 90) * (Math.PI / 180)) * radius * 0.7 + height / 2;
            ctx.font = "normal 26px san-serif";
            ctx.fillStyle = 'black';
            ctx.textAlign = 'center';
            ctx.fillText(val + "%", x, y);
            sum += (val);
        }
    })
    buttoncontrol(false);
}

canvas.addEventListener('mousemove', (event) => { // 캔버스 마우스이벤트
    var box = document.getElementById('checkbox');
    var x1 = event.clientX - canvas.offsetLeft;
    var y1 = event.clientY - canvas.offsetTop;
    if (chart == 'circle') {
        var inC = insideCircle(x1, y1);
        if (inC.result) {
            box.style.visibility = 'visible';
            box.textContent = value_text[inC.index] + value[inC.index] + "%";
            box.style.left = event.clientX - box.offsetWidth - 20 + "px";
            box.style.top = event.clientY - 40 + "px";
        } else {
            box.style.visibility = 'hidden'
        }
    } else if (chart == 'bbar') {
        var inB = insideBar(x1, y1);
        if (inB.result) {
            ox.style.visibility = 'visible';
            box.textContent = value_text[inC.index] + value[inC.index] + "%";
            box.style.left = event.clientX - box.offsetWidth - 20 + "px";
            box.style.top = event.clientY - 40 + "px";
        } else {
            box.style.visibility = 'hidden'
        }
    }
});

function insideCircle(x1, y1) {
    var value_degree = [];
    var distance = Math.sqrt(Math.abs(Math.pow(width / 2 - x1, 2) + Math.pow(height / 2 - y1, 2))); //마우스에서 원점까지의거리
    if (distance < radius) {
        var degree = Math.atan2(y1 - height / 2, x1 - width / 2); //커서위치 각도

        var sum = 0;
        var pre_degree = 0;

        value.forEach((val, index) => {
            sum += val / value_sum * 360;
            value_degree[index] = sum;
        });
        degree = (degree * 180) / Math.PI + 90;
        if (degree < 0) { // 0~360
            degree += 360;
        }
        for (var i = 0; i < value.length; i++) {
            if (degree >= pre_degree && degree < value_degree[i]) {
                return { index: i, result: true };
            }
            pre_degree = value_degree[i];
        }
        return { result: false };
    } else {
        return { result: false };
    }
}
</script>